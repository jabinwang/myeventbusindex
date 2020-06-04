package com.jabin.eventbus.annotation;

import java.lang.reflect.Method;

public class SubscriberMethod {
    /**
     * 订阅方法名
     */
    private String methodName;
    /**
     * 订阅方法
     */
    private Method method;
    private ThreadMode threadMode;
    /**
     * 事件类型,UserInfo.class
     */
    private Class<?> eventType;


    public SubscriberMethod(Class<?> subscriberClass, String methodName,  ThreadMode threadMode,
                            Class<?> eventType) {
        this.methodName = methodName;
        this.threadMode = threadMode;
        this.eventType = eventType;
        try {
            this.method = subscriberClass.getDeclaredMethod(methodName, eventType);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public Method getMethod() {
        return method;
    }

    public void setMethod(Method method) {
        this.method = method;
    }

    public ThreadMode getThreadMode() {
        return threadMode;
    }

    public void setThreadMode(ThreadMode threadMode) {
        this.threadMode = threadMode;
    }

    public Class<?> getEventType() {
        return eventType;
    }

    public void setEventType(Class<?> eventType) {
        this.eventType = eventType;
    }
}
