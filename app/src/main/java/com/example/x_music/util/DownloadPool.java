package com.example.x_music.util;

import com.example.x_music.callback.VoidCallBack;
import com.example.x_music.activity.ContextApp;
import com.example.x_music.datasource.CacheDataSource;
import com.example.x_music.entity.MusicInfo;
import com.example.x_music.music_api.WuSunMusicApi;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.concurrent.*;

public class DownloadPool {

    private static ExecutorService executorService;

    static {
        //构建线程池
        executorService =  new ThreadPoolExecutor(
                0,
                3,
                5,
                TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(1000));

    }

    /**
     * 异步下载歌曲
     * @param musicInfo 歌曲信息
     * @param callBack 回调
     */
    public static void download(MusicInfo musicInfo, VoidCallBack<MusicInfo> callBack){
        //提交任务
        executorService.submit(() -> {
            try {
                //获取下载目标文件
                String downPath = ContextApp.getDownloadPath() + musicInfo.getTitle() + "-" + musicInfo.getAuthor() + ".mp3";
                File downFile = new File(downPath);
                if (downFile.exists()) {
                    //已经下载过的不用下
                    callBack.onSucceed(musicInfo);
                    return;
                }
                //从缓存拿
                File file = CacheDataSource.getFileByFileName(musicInfo.getPlayerUrlCacheId());
                if (file != null){
                    //缓存有直接复制
                    FileInputStream fis = new FileInputStream(file);
                    FileOutputStream fos = new FileOutputStream(downFile);
                    HttpUtil.swap(fis, fos);
                    fis.close();
                    fos.close();
                    if (callBack != null)
                        callBack.onSucceed(musicInfo);
                }else {
                    //设置下载链接
                    WuSunMusicApi.setPicAndLrcAndPlayUrl(musicInfo, new VoidCallBack<MusicInfo>() {
                        @Override
                        public void onSucceed(MusicInfo mfo) {
                            try {
                                //异步下载
                                HttpUtil.download(mfo.getPlayerUrl(), downFile);
                                if (callBack != null)
                                    callBack.onSucceed(mfo);
                            }catch (Exception e){
                                if (callBack != null)
                                    callBack.onFail();
                                throw new RuntimeException(e);
                            }
                        }
                        @Override
                        public void onFail() {
                            if (callBack != null)
                                callBack.onFail();
                        }
                    });
                }
            }catch (Exception e){
                e.printStackTrace();
                if (callBack != null)
                    callBack.onFail();
            }
        });
    }

}
