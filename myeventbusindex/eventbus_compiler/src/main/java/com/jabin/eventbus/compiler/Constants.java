package com.jabin.eventbus.compiler;

public class Constants {
    /**
     * 注解处理器支持的注解类型
     */
    public static final String SUBSCRIBE_ANNOTATION_TYPES = "com.jabin.eventbus.annotation.Subscribe";

    /**
     * apt生成的类文件所属的包名
     */
    public static final String PACKAGE_NAME = "packageName";
    /**
     * apt生成的类文件名
     */
    public static final String CLASS_NAME = "className";

    public static final String SUBSCRIBERINFO_INDEX = "com.jabin.eventbus.annotation.SubscriberInfoIndex";

    public static final String FIELD_NAME = "SUBSCRIBER_INDEX";

    public static final String PUTINDEX_PARAMETER_NAME = "info";

    public static final String PUTINDEX_METHOD_NAME = "putIndex";

    public static final String SUBSCRIBERINFO_PARAMETER_NAME = "subscriberClass";

    public static final String SUBSCRIBERINFO_METHOD_NAME = "getSubscriberInfo";
}
