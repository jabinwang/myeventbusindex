package com.jabin.eventbus.compiler;

import com.google.auto.service.AutoService;
import com.jabin.eventbus.annotation.EventBean;
import com.jabin.eventbus.annotation.Subscribe;
import com.jabin.eventbus.annotation.SubscriberInfo;
import com.jabin.eventbus.annotation.SubscriberMethod;
import com.jabin.eventbus.annotation.ThreadMode;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import java.io.IOError;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedOptions;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;

// 用来生成 META-INF/services/javax.annotation.processing.Processor 文件
@AutoService(Processor.class)
//支持的注解类型
@SupportedAnnotationTypes(Constants.SUBSCRIBE_ANNOTATION_TYPES)
//sdk编译版本
@SupportedSourceVersion(SourceVersion.RELEASE_7)
//注解处理器接收的参数
@SupportedOptions({Constants.PACKAGE_NAME, Constants.CLASS_NAME})
public class SubscribeProcessor extends AbstractProcessor {

    /**
     * 操作Element工具类(类，接口，函数，属性)
     */
    private Elements elementUtils;
    /**
     * 用于操作TypeMirror 的工具方法
     */
    private Types typeUtils;
    /**
     * 打印信息
     */
    private Messager messager;
    /**
     * 用于生成文件
     */
    private Filer filer;

    /**
     * APT 包名
     */
    private String packageName;
    /**
     * APT  类名
     */
    private String className;
    /**
     * key:MainActivity, val:订阅的方法集合
     */
    private final Map<TypeElement, List<ExecutableElement>> methodsByClass = new HashMap<>();

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        elementUtils = processingEnv.getElementUtils();
        typeUtils = processingEnv.getTypeUtils();
        messager = processingEnv.getMessager();
        filer = processingEnv.getFiler();

        Map<String, String> options = processingEnv.getOptions();
        if (!EmptyUtil.isEmpty(options)) {
            packageName = options.get(Constants.PACKAGE_NAME);
            className = options.get(Constants.CLASS_NAME);
            messager.printMessage(Diagnostic.Kind.NOTE, "packagename---->" + packageName
                    + " classname --->" + className);
        }

        if (EmptyUtil.isEmpty(packageName) || EmptyUtil.isEmpty(className)) {
            messager.printMessage(Diagnostic.Kind.ERROR, "参数为空");
        }
    }

    /**
     * @param annotations 注解集合
     * @param roundEnv    当前运行环境
     * @return
     */

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        if (!EmptyUtil.isEmpty(annotations)) {
            //获取被subsicrbe注解的元素集合
            Set<? extends Element> elements = roundEnv.getElementsAnnotatedWith(Subscribe.class);
            if (!EmptyUtil.isEmpty(elements)) {

                try {
                    parseElements(elements);
                    return true;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return true;
        }
        return false;
    }

    private void parseElements(Set<? extends Element> elements) throws IOException {
        for (Element e :
                elements) {

            if (e.getKind() != ElementKind.METHOD) {
                messager.printMessage(Diagnostic.Kind.ERROR, "只能注解方法");
                return;
            }

            ExecutableElement method = (ExecutableElement) e;
            if (check(method)) {
                TypeElement classElement = (TypeElement) e.getEnclosingElement();
                List<ExecutableElement> methods = methodsByClass.get(classElement);
                if (methods == null) {
                    methods = new ArrayList<>();
                    methodsByClass.put(classElement, methods);
                }
                methods.add(method);
            }
            messager.printMessage(Diagnostic.Kind.NOTE,
                    "遍历方法" + method.getSimpleName() + "当前类： " + e.getEnclosingElement().getSimpleName());
        }
        TypeElement subscriberIndexType = elementUtils.getTypeElement(Constants.SUBSCRIBERINFO_INDEX);
        createFile(subscriberIndexType);
    }

    private void createFile(TypeElement subscriberIndexType) throws IOException {
        //添加静态块代码：SUBSCRIBER_INDEX = new HashMap<Class, SubscriberInfo>();
        CodeBlock.Builder codeBlock = CodeBlock.builder();
        codeBlock.addStatement("$N = new $T<$T,$T>()",
                Constants.FIELD_NAME,
                HashMap.class,
                Class.class,
                SubscriberInfo.class);

        //new SubscriberMethod(Class<?> subscriberClass, String methodName,  ThreadMode threadMode,
        //                            Class<?> eventType)
        //new SubscriberMethod(MainActivity.class, "abc",ThreadMode.POSTING, UserInfo.class)

        for (Map.Entry<TypeElement, List<ExecutableElement>> entry :
                methodsByClass.entrySet()) {
            CodeBlock.Builder contentBlock = CodeBlock.builder();
            CodeBlock contentCode = null;
            String format;
            for (int i = 0; i < entry.getValue().size(); i++) {
                ExecutableElement executableElement = entry.getValue().get(i);
                Subscribe subscribe = executableElement.getAnnotation(Subscribe.class);
                //获取订阅事件方法的所有参数
                List<? extends VariableElement> parameters = executableElement.getParameters();
                String methodName = executableElement.getSimpleName().toString();
                //参数类型
                TypeElement parameterElement = (TypeElement) typeUtils.asElement(parameters.get(0).asType());
                if ( i == entry.getValue().size() -1){
                    format = "new $T($T.class, $S, $T.$L, $T.class)";
                }else {
                    format = "new $T($T.class, $S, $T.$L, $T.class),\n";
                }
                contentCode = contentBlock.add(format,
                        SubscriberMethod.class,
                        ClassName.get(entry.getKey()),
                        methodName,
                        ThreadMode.class,
                        subscribe.threadMode(),
                        ClassName.get(parameterElement))
                        .build();
            }
            if (contentCode != null) {
                //putIndex(new EventBean(MainActivity.class, new SubscirberMethod[]{})
                codeBlock.beginControlFlow("putIndex(new $T($T.class, new $T[]",
                        EventBean.class,
                        ClassName.get(entry.getKey()),
                        SubscriberMethod.class).add(contentCode)
                        .endControlFlow("))");
            }else {
                messager.printMessage(Diagnostic.Kind.ERROR, "错误");
            }

            //全局属性 Map<Class<?>,SubscriberInfo>
            TypeName fieldType = ParameterizedTypeName.get(ClassName.get(Map.class),
                    ClassName.get(Class.class),
                    ClassName.get(SubscriberInfo.class));

            //putIndex 方法参数 putIndex(SubscriberIndex info)
            ParameterSpec putIndexParameter = ParameterSpec.builder(
                    ClassName.get(SubscriberInfo.class),
                    Constants.PUTINDEX_PARAMETER_NAME).build();

            //putIndex : private static void putIndex(SubscriberInfo info)
            MethodSpec.Builder putIndexMethodBuilder = MethodSpec.methodBuilder(Constants.PUTINDEX_METHOD_NAME)
                    .addModifiers(Modifier.PRIVATE, Modifier.STATIC)
                    .addParameter(putIndexParameter);
            //SUBSCRIBER_INDEX.put(info.subscriberClass(), info)
            putIndexMethodBuilder.addStatement("$N.put($N.subscriberClass(), $N)",
                    Constants.FIELD_NAME,
                    Constants.PUTINDEX_PARAMETER_NAME,
                    Constants.PUTINDEX_PARAMETER_NAME);

            //getSubscriberInfo方法参数: Class subscriberClass
            ParameterSpec subscriberInfoParameter = ParameterSpec.builder(
                    ClassName.get(Class.class),
                    Constants.SUBSCRIBERINFO_PARAMETER_NAME).build();
            //getSubscriberInfo方法：public SubscriberInfo getSubscriberInfo(Class<?> subscriberClass)
            MethodSpec.Builder subscriberInfoMethodBuilder = MethodSpec.methodBuilder(Constants.SUBSCRIBERINFO_METHOD_NAME)
                    .addAnnotation(Override.class)
                    .addModifiers(Modifier.PUBLIC)
                    .addParameter(subscriberInfoParameter)
                    .returns(SubscriberInfo.class);
            //return SUBSCRIBER_INDEX.get(subscriberClass)
            subscriberInfoMethodBuilder.addStatement("return $N.get($N)",
                    Constants.FIELD_NAME,
                    Constants.SUBSCRIBERINFO_PARAMETER_NAME);

            //构建类
            TypeSpec typeSpec = TypeSpec.classBuilder(className)
                    .addSuperinterface(ClassName.get(subscriberIndexType))
                    .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                    .addStaticBlock(codeBlock.build())
                    .addField(fieldType, Constants.FIELD_NAME, Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL)
                    .addMethod(putIndexMethodBuilder.build())
                    .addMethod(subscriberInfoMethodBuilder.build())
                    .build();

            JavaFile.builder(packageName,
                    typeSpec)
                    .build()
                    .writeTo(filer);



        }
    }

    private boolean check(ExecutableElement method) {
        //不能为static
        if (method.getModifiers().contains(Modifier.STATIC)) {
            return false;
        }
        //必须是public
        if (!method.getModifiers().contains(Modifier.PUBLIC)) {
            return false;
        }
        //参数只有一个
        List<? extends VariableElement> paramenters = method.getParameters();
        return paramenters.size() == 1;
    }
}
