package com.jabin.myeventbus;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.jabin.eventbus.annotation.Subscribe;
import com.jabin.eventbus.annotation.ThreadMode;
import com.jabin.eventbus.apt.EventBusIndex;
import com.jabin.myeventbusindex.MyEventBus;


public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main1);
        MyEventBus.geDefault().addIndex(new EventBusIndex());
        MyEventBus.geDefault().register(this);
    }

    // 默认不填线程
    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void getMessage(BookInfo bean) {
        Log.e("EventBus >>1>> ", "BACKGROUND thread = " + Thread.currentThread().getName());
        Log.e("EventBus >>1>> ", "BACKGROUND " + bean.getName());
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void getMessage1(BookInfo bean) {
        Log.e("EventBus >>1>> ", "MAIN thread = " + Thread.currentThread().getName());
        Log.e("EventBus >>1>> ", "MAIN " + bean.getName());
    }

    @Subscribe(threadMode = ThreadMode.ASYNC)
    public void getMessage2(BookInfo eventBean) {
        Log.e("EventBus >>1>> ", "ASYNC thread = " + Thread.currentThread().getName());
        Log.e("EventBus >>1>> ", "ASYNC " + eventBean.getName());
    }

    public void click(View view) {
        Intent intent = new Intent(this, SecondActivity.class);
        startActivity(intent);
    }
}
