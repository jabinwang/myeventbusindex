package com.jabin.myeventbusindex;

import android.os.Handler;
import android.os.Looper;

import com.jabin.eventbus.annotation.SubscriberInfo;
import com.jabin.eventbus.annotation.SubscriberInfoIndex;
import com.jabin.eventbus.annotation.SubscriberMethod;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class MyEventBus {

    private static volatile MyEventBus instance;

    private SubscriberInfoIndex subscriberInfoIndex;

    private static final Map<Object, List<SubscriberMethod>> CACHE_MAP = new ConcurrentHashMap<>();

    private Handler handler;
    private ExecutorService executorService;

    private MyEventBus() {
        handler = new Handler(Looper.getMainLooper());
        executorService = Executors.newCachedThreadPool();
    }

    public static MyEventBus geDefault() {
        if (instance == null) {
            synchronized (MyEventBus.class) {
                if (instance == null) {
                    instance = new MyEventBus();
                }
            }
        }
        return instance;
    }


    public void addIndex(SubscriberInfoIndex index) {
        subscriberInfoIndex = index;
    }

    public void register(Object subscriber) {

        Class<?> subscriberClass = subscriber.getClass();

        List<SubscriberMethod> subscriberMethods = findSubscriberMethod(subscriber);

    }

    private List<SubscriberMethod> findSubscriberMethod(Object subscriber) {
        List<SubscriberMethod> subscriberMethods = CACHE_MAP.get(subscriber);
        if (subscriberMethods != null) {
            return subscriberMethods;
        }
        Class<?> subscriberClass = subscriber.getClass();
        subscriberMethods = findUsingIndex(subscriberClass);
        if (subscriberMethods != null) {
            CACHE_MAP.put(subscriber, subscriberMethods);
        }
        return subscriberMethods;
    }

    private List<SubscriberMethod> findUsingIndex(Class<?> subscriberClass) {
        if (subscriberInfoIndex == null) {
            throw new RuntimeException("error");
        }
        SubscriberInfo info = subscriberInfoIndex.getSubscriberInfo(subscriberClass);
        return Arrays.asList(info.subscriberMethods());
    }

    public void post(final Object event) {

        for (final Map.Entry<Object, List<SubscriberMethod>> entry :
                CACHE_MAP.entrySet()) {
            List<SubscriberMethod> subscriberMethods = entry.getValue();
            for (final SubscriberMethod method :
                    subscriberMethods) {
                if (method.getEventType().isAssignableFrom(event.getClass())) {
                    switch (method.getThreadMode()) {
                        case MAIN:
                            if (Looper.myLooper() == Looper.getMainLooper()) {
                                invoke(method.getMethod(), entry.getKey(), event);
                            } else {
                                handler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        invoke(method.getMethod(), entry.getKey(), event);
                                    }
                                });
                            }
                            break;
                        case BACKGROUND:
                            if (Looper.myLooper() == Looper.getMainLooper()) {
                                invoke(method.getMethod(), entry.getKey(), event);
                            } else {
                                executorService.execute(new Runnable() {
                                    @Override
                                    public void run() {
                                        invoke(method.getMethod(), entry.getKey(), event);
                                    }
                                });
                            }
                            break;
                        case ASYNC:
                            executorService.execute(new Runnable() {
                                @Override
                                public void run() {
                                    invoke(method.getMethod(), entry.getKey(), event);
                                }
                            });
                            break;
                        case POSTING:
                            invoke(method.getMethod(), entry.getKey(), entry);
                            break;
                    }
                }
            }
        }
    }

    private void invoke(Method method, Object subscriber, Object event) {
        try {
            method.invoke(subscriber, event);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }
}
