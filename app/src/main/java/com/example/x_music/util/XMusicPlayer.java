package com.example.x_music.util;


import android.media.MediaPlayer;
import android.util.Log;
import android.widget.Toast;
import com.example.x_music.callback.VoidCallBack;
import com.example.x_music.activity.ContextApp;
import com.example.x_music.datasource.CacheDataSource;
import com.example.x_music.datasource.MusicInfoDB;
import com.example.x_music.entity.MusicInfo;
import com.example.x_music.entity.PlayerMode;
import com.example.x_music.music_api.WuSunMusicApi;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class XMusicPlayer {

    private static final String TAG = XMusicPlayer.class.getName();
    private List<MusicInfo> playList;
    private int playerIndex;
    private PlayerMode currentPlayMode;
    private volatile MediaPlayer mediaPlayer;
    private Timer timer;
    private MusicInfo currentPlayMusicInfo;

    private MusicInfoCallback musicInfoCallback;

    private TimerUpdateCallback timerUpdateCallback;

    public XMusicPlayer() {
        this.playList = new ArrayList<>();
        playerIndex = 0;
        currentPlayMode = PlayerMode.ORDER;
    }

    /**
     * 获取当前播放的歌曲
     * @return 歌曲
     */
    public synchronized MusicInfo getCurrentPlayMusicInfo() {
        return currentPlayMusicInfo;
    }

    /**
     * 获取歌单
     * @return 歌单
     */
    public synchronized List<MusicInfo>  getPlayList() {
        return playList;
    }

    /**
     * 清除歌单
     */
    public synchronized void clearPlayerList() {
        playList.clear();
    }


    /**
     * 返回歌曲数
     * @return 歌曲数
     */
    public synchronized int getCount(){
        return playList.size();
    }

    /**
     * 设置歌单列表
     * @param playList 歌单列表
     */
    public synchronized void setPlayList(List<MusicInfo> playList) {
        if (Objects.isNull(playList)) return;
        this.playList = playList;
    }

    /**
     * 添加到歌单
     * @param musicInfo 歌曲信息
     */
    public synchronized void addToPlayerList(MusicInfo musicInfo) {
        playList.add(musicInfo);
    }

    /**
     * 根据位置删除歌单指定的歌曲
     * @param pos 位置
     */
    public synchronized void delMusicInfoByPos(int pos){
        if (pos >= 0 && pos < playList.size())
            playList.remove(pos);
    }

    /**
     * 设置播放博士
     * @param playMode 播放模式
     */
    public synchronized void setCurrentPlayMode(PlayerMode playMode) {
        currentPlayMode = playMode;
    }


    /**
     * 根据当前播放模式改变播放索引
     *
     * @param num 要前进或后退的歌曲数
     */
    private void changePlayIndex(int num) {
        if (playList.isEmpty()) return;
        switch (currentPlayMode) {
            case ORDER:
                //顺序
                if (playerIndex + num >= playList.size()) {
                    playerIndex = (playerIndex + num) % playList.size();
                } else if (playerIndex + num < 0) {
                    playerIndex = playList.size() + num + playerIndex;
                } else {
                    playerIndex += num;
                }
                break;
            case CYCLE:
                //单曲循环不变
                break;
            case RANDOM:
                //随机
                playerIndex = new Random().nextInt(playList.size());
                break;
            default:
                break;
        }
    }

    /**
     * 下一首
     */
    public synchronized void playNext() {
        changePlayIndex(1);
        playMusic();
    }

    /**
     * 自动播放下一首播放音乐
     *
     * @param file 播放源文件
     */
    private void autoPlayMusic(File file, MusicInfo musicInfo) {
        this.currentPlayMusicInfo = musicInfo;
        //不为空说明已经缓存了
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setOnCompletionListener(mp -> {
            //播放完后自动播放下一首
            playNext();
        });
        try {
            mediaPlayer.setDataSource(file.getPath());
            mediaPlayer.prepare(); // 准备播放
        } catch (IOException e) {
            Log.e(TAG, "autoPlayMusic: 歌曲播放失败");
            e.printStackTrace();

        }
        mediaPlayer.start(); // 开始播放
        //回调更新信息
        musicInfoCallback.callback(musicInfo);
        runTimerUpdateCallBack();
    }


    /**
     * 设置播放进度百分比
     * @param percent 要设置的百分比
     */
    public synchronized void setPlayPercent(float percent) {
        if (mediaPlayer != null)
            mediaPlayer.seekTo((int) (mediaPlayer.getDuration() * percent));
    }


    /**
     * 是否在播放
     * @return true在播放，false不在播放
     */
    public synchronized boolean isPlaying() {
        try {
            return mediaPlayer != null && mediaPlayer.isPlaying();
        }catch (IllegalStateException e){
            return false;
        }
    }

    /**
     * 获取播放位置
     * @return 播放位置
     */
    public synchronized int getCurrentPosition(){
        if (mediaPlayer == null) return 0;
        return mediaPlayer.getCurrentPosition();
    }

    /**
     * 获取总时长
     * @return 总时长
     */
    public synchronized int getDuration(){
        if (mediaPlayer == null) return 0;
        return mediaPlayer.getDuration();
    }


    /**
     * 停止播放
     */
    public synchronized void pause() {
        if (mediaPlayer != null) mediaPlayer.pause();
    }

    /**
     * 恢复播放
     */
    public synchronized void resume() {
        if (mediaPlayer != null) mediaPlayer.start();
    }

    /**
     * 执行计时器的更新方法
     */
    private void runTimerUpdateCallBack() {
        if (timer != null) timer.cancel();
        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                timerUpdateCallback.callback();
            }
        }, 0, 500);
    }


    /**
     * 播放指定音乐
     * @param info 音乐
     * @param existDB 音乐是否存在数据库
     */
    public synchronized void playMusic(MusicInfo info, boolean existDB) {
        if (mediaPlayer != null) {
            mediaPlayer.pause(); // 暂停当前播放
            mediaPlayer.release(); // 释放当前MediaPlayer对象
            mediaPlayer = null; // 将当前MediaPlayer对象置为空
        }
        //从缓存文件里拿
        File file = CacheDataSource.getFileByFileName(info.getPlayerUrlCacheId());
        if (file != null) {
            autoPlayMusic(file, info);
        } else {
            //获取最新的播放链接
            WuSunMusicApi.setPicAndLrcAndPlayUrl(info, new VoidCallBack<MusicInfo>() {
                @Override
                public void onSucceed(MusicInfo musicInfo) {
                    //异步下载文件，完成后播放
                    CacheDataSource.download(info.getPlayerUrl(), info.getPlayerUrlCacheId(), tempFile -> {
                        autoPlayMusic(tempFile, info);
                    });
                    if (existDB) MusicInfoDB.update(info);
                }
                @Override
                public void onFail() {
                    Toast.makeText(ContextApp.getContext(), "播放失败", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    /**
     * 播放音乐
     */
    public synchronized void playMusic() {
        if (playList.isEmpty()) return;
        try {
            MusicInfo info = playList.get(playerIndex);
            playMusic(info, true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * 播放前一首
     */
    public synchronized void playPrev() {
        changePlayIndex(-1);
        playMusic();
    }

    /**
     * 改变播放的音乐时调用次函数
     *
     * @param callback 回调方法
     */
    public void setMusicInfoCallback(MusicInfoCallback callback) {
        this.musicInfoCallback = callback;
    }

    /**
     * 每隔500毫米调用一次次方法
     *
     * @param callback 回调方法
     */
    public void setTimerUpdateCallback(TimerUpdateCallback callback) {
        this.timerUpdateCallback = callback;
    }

    public synchronized void play(int pos) {
        if (pos >= 0 && pos < playList.size()) playerIndex = pos;
        playMusic();
    }


    public interface TimerUpdateCallback{
        void callback();
    }

    public interface MusicInfoCallback {
        void callback(MusicInfo musicInfo);
    }

}
