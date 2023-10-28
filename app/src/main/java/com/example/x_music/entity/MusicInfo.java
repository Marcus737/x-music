package com.example.x_music.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class MusicInfo {
    private Integer id;
    private String pic;
    private String musicId;
    private String picCacheId;
    private String lrc;
    private String lrcCacheId;
    private String playerUrl;
    private String playerUrlCacheId;
    private String title;
    private String author;

    public String getPicCacheId() {
        return musicId + ".pic";
    }

    public String getLrcCacheId() {
        return musicId + ".lrc";
    }

    public String getPlayerUrlCacheId() {
        return musicId + ".mp3";
    }
}
