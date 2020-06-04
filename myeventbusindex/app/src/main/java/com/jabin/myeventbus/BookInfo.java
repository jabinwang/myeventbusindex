package com.jabin.myeventbus;

import android.support.annotation.NonNull;

public class BookInfo {

    private String name;

    public BookInfo(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @NonNull
    @Override
    public String toString() {
        return "name = " + name;
    }
}


