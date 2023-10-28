package com.example.x_music.activity;

import android.app.Application;
import android.content.Context;
import android.os.Environment;
import com.example.x_music.datasource.CacheDataSource;
import com.example.x_music.util.XMusicPlayer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * 全局类
 */
public class ContextApp extends Application {
 
    private static Context context;

    private static XMusicPlayer player;


    @Override
    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();
        CacheDataSource.setCacheDir(getCacheDir());
        player = new XMusicPlayer();

    }
    /**
    *获取全局context
    **/
    public static Context getContext() {
        return context;
    }

    /**
     * 获取全局的播放器
     * @return 播放器
     */
    public static XMusicPlayer getPlayer(){
        return player;
    }


    /**
     * 获取下载路径
     * @return 下载路径
     */
    public static String getDownloadPath(){
        return ContextApp.getContext().getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath();
    }


}