package com.jabin.eventbus.annotation;

public class EventBean implements SubscriberInfo {


    private final Class<?> subscriberClass;
    private final SubscriberMethod[] methods;

    public EventBean(Class<?> subscriberClass, SubscriberMethod[] methods) {
        this.subscriberClass = subscriberClass;
        this.methods = methods;
    }

    @Override
    public Class<?> subscriberClass() {
        return subscriberClass;
    }

    @Override
    public SubscriberMethod[] subscriberMethods() {
        return methods;
    }
}
