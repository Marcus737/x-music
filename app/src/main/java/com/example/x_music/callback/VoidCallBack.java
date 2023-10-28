package com.example.x_music.callback;

public interface VoidCallBack<T> {
    void onSucceed(T t);
    void onFail();
}