package com.example.x_music.music_api;

import android.annotation.SuppressLint;
import android.net.Uri;
import com.example.x_music.callback.VoidCallBack;
import com.example.x_music.entity.MusicInfo;
import com.example.x_music.util.HttpUtil;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class WuSunMusicApi {
    private static final String search = "http://www.qeecc.com/so/%s/%d.html";
    private static final String meataUrl = "http://www.qeecc.com/templets/2t58/js/play.php";
    private static final String refererUrl = " http://www.qeecc.com/song/%s.html";

    private static final String lrcUrl = "http://api.44h4.com/lc.php?cid=%s";


    /**
     * 设置播放链接，歌词id，图片链接
     * @param info 音乐信息
     * @param callBack 回调
     */
    public static void setPicAndLrcAndPlayUrl(MusicInfo info, VoidCallBack<MusicInfo> callBack){
        //设置请求头参数
        HashMap<String, String> params = new HashMap<>(), header = new HashMap<>();
        params.put("id", info.getMusicId());
        params.put("type", "ilingku");
        header.put("Referer", String.format(refererUrl, info.getMusicId()));
        //u异步调用
        HttpUtil.post(meataUrl, params, header, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callBack.onFail();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String s = response.body().string();
                if (s.length() == 0) return;
                //封面
                int fi = s.indexOf("pic") + 6, efi = fi;
                while (s.charAt(efi) != '"') efi++;
                String cpic = s.substring(fi, efi).replaceAll("\\\\", "");

                //歌词id
                fi = s.indexOf("lkid") + 6; efi = fi;
                while (s.charAt(efi) != '"') efi++;
                String clrc = s.substring(fi, efi - 1).replaceAll("\\\\", "");

                //播放链接
                fi = s.indexOf("url") + 6; efi = fi;
                while (s.charAt(efi) != '"') efi++;
                String playUrl = s.substring(fi, efi).replaceAll("\\\\", "");

                info.setPlayerUrl(playUrl);
                info.setPic(cpic);
                info.setLrc(String.format(lrcUrl, clrc));
                response.close();
                callBack.onSucceed(info);
            }
        });

    }

    /**
     * 搜索
     * @param key 关键词
     * @param page 页数
     * @param callBack 结果回调
     */
    @SuppressLint("DefaultLocale")
    public static void search(String key, int page, VoidCallBack<List<MusicInfo>> callBack) {
        HttpUtil.get(String.format(search, Uri.encode(key), page), new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callBack.onFail();
            }
            //<a href="/song/c3hpZGs.html" target="_mp3">周杰伦《外婆》[Mp3]</a>
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String s = response.body().string().replaceAll("\n", "").replaceAll("\r", "").replaceAll("&nbsp;", "&");
                List<MusicInfo> list = new ArrayList<>();
                for (int ai = 0; ai < s.length(); ai++) {
                    //<a
                    if (s.charAt(ai) == '<' && s.charAt(ai + 1) == 'a') {
                        //href
                        int hi = ai + 9, ehi = hi;
                        while (s.charAt(ehi) != '"') ehi++;
                        String href = s.substring(hi, ehi);
                        //target
                        int ti = ehi + 10, eti = ti;
                        while (s.charAt(eti) != '"') eti++;
                        String target = s.substring(ti, eti);
                        //只要target为_mp3的
                        if (!target.equals("_mp3")) continue;
                        //周杰伦
                        int fi = eti + 2, efi = fi;
                        while (s.charAt(efi) != '《') efi++;
                        String author = s.substring(fi, efi);
                        //外婆
                        int ki = efi + 1, eki = ki;
                        while (s.charAt(eki) != '》') eki++;
                        String musicName = s.substring(ki, eki);
                        //music_id c3hpZGs
                        String musicId = href.substring(href.lastIndexOf('/') + 1, href.length() - 5);
                        MusicInfo info = MusicInfo.builder()
                                .title(musicName)
                                .author(author)
                                .musicId(musicId)
                                .build();
                        list.add(info);
                        ai = eki;
                    }
                }
                callBack.onSucceed(list);
                response.close();
            }
        });
    }
}
