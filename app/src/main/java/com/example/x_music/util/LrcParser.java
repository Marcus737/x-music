package com.example.x_music.util;

import android.provider.MediaStore;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.example.x_music.datasource.CacheDataSource;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 歌词解析器
 */
public class LrcParser {

    //歌词项
    private class LrcItem{
        //mm是分钟，ss是秒，hs是百分之一秒
        int mm, ss, hs;
        String lrc;
        public LrcItem(int mm, int ss, int hs, String lrc) {
            this.mm = mm;
            this.ss = ss;
            this.hs = hs;
            this.lrc = lrc;
        }

        @Override
        public String toString() {
            return "LrcItem{" +
                    "mm=" + mm +
                    ", ss=" + ss +
                    ", hs=" + hs +
                    ", lrc='" + lrc + '\'' +
                    '}';
        }
    }

    private final List<LrcItem> items = new ArrayList<>();

    /**
     * 从文件中获取歌词
     * @param file
     */
    public LrcParser(File file) {
        //将文件的内容复制到sb中
        StringBuilder sb = new StringBuilder();
        try(BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file)))) {
            String line;
            while ((line = br.readLine()) != null) sb.append(line);
        }catch (Exception e){
            e.printStackTrace();
        }
        //解析json
        try {
            JSONObject jo = JSON.parseObject(sb.toString());
            String lrc = jo.getString("lrc");
            parse(lrc);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public LrcParser(String lrc) {
        try {
            parse(lrc);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void parse(String lrc) {
        //这里有坑
        String[] lines = lrc.split("\n");

        for (String line : lines) {
            line = line.trim();
            line = line.replaceAll("\r", "");
            if (line.isEmpty()) continue;
            //找到时间 timeL->'['  timeR-> ']'
            int timeL = -1, timeR = -1;
            for (int i = 0; i < line.length(); i++) {
                if (line.charAt(i) == '[') timeL = i;
                else if (line.charAt(i) == ']') timeR = i;
            }
            if (timeL == -1 || timeR == -1 || (timeL >= timeR)) throw new IllegalArgumentException("解析歌词错误，未找到 []");
            //寻找冒号
            int m1 = -1, m2 = -1;
            for (int i = timeL + 1; i < timeR; i++) {
                if (line.charAt(i) == ':') {
                    m1 = i;
                }else if (line.charAt(i) == '.'){
                    m2 = i;
                }
            }
            if (m1 == -1 || m2 == -1) continue;
            //寻找mm:ss:hs
            int mm = Integer.parseInt(line.substring(timeL + 1, m1 )),
                    ss =Integer.parseInt( line.substring(m1 + 1, m2)),
                    hs = Integer.parseInt(line.substring(m2 + 1, timeR));
            //歌词
            String lrcString = line.substring(timeR + 1).trim();
            items.add(new LrcItem(mm, ss, hs, lrcString));
        }
        //排序，按mm，ss，hs增序排
        Collections.sort(items, (t1, t2) -> {
            if (t1.mm == t2.mm){
                if (t1.ss == t2.ss){
                    return t1.hs - t2.hs;
                }else {
                    return t1.ss - t2.ss;
                }
            }else {
                return t1.mm - t2.mm;
            }
        });
    }

    /**
     * 获取歌词，前一句，当前句，和后一句
     * @param minute 分钟
     * @param second 秒
     * @param hs 百分之一秒
     * @return 歌词
     */
    public String[] getLrcString(int minute, int second, int hs){
        String prev, current, next;
        for (int i = 0; i < items.size(); i++) {
            LrcItem item = items.get(i);
            if (item.mm == minute && item.ss == second && Math.abs(hs - item.hs) <= 100){
                prev = i - 1 >= 0 ? items.get(i - 1).lrc : "";
                current = item.lrc;
                next = i + 1 < items.size() ? items.get(i + 1).lrc : "";
                return new String[]{prev, current, next};
            }
        }
        return null;
    }


    /**
     * 根据时间获取对应的歌词
     * 其中hs设置为-1时，取对应的hs中最后的一个
     * @param minute 分钟
     * @param second 秒
     * @param hs 百分之一秒
     * @return 歌词
     */
    public String getLrcLineByTime(int minute, int second, int hs){
        for (LrcItem item : items) {
            if (item.mm == minute && item.ss == second && Math.abs(hs - item.hs) <= 50){
                return item.lrc;
            }
        }
        return null;
    }

}

