package com.jabin.eventbus.annotation;

public interface SubscriberInfo {

    /**
     * 订阅所属的类，MainActivity
     */
    Class<?> subscriberClass();

    /**
     * 获取订阅所属类的中所有订阅事件的方法
     */
    SubscriberMethod[] subscriberMethods();
}
