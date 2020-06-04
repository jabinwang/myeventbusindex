package com.jabin.eventbus.annotation;

public interface SubscriberInfoIndex {
    /**
     * 通过订阅对象获取所有订阅方法
     * @param subscriberClass
     * @return
     */
    SubscriberInfo getSubscriberInfo(Class<?> subscriberClass);
}
