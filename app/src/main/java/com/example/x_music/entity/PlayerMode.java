package com.example.x_music.entity;

import com.example.x_music.R;

public enum PlayerMode {
    CYCLE("循环播放", 0, R.drawable.m_cycle),
    RANDOM("随机播放", 1, R.drawable.m_rand),
    ORDER("顺序播放", 2, R.drawable.m_order);

    private final String playerModeName;
    private final int playerModeNum;

    private final int modeRes;

    PlayerMode(String playerModeName, int playerModeNum, int modeRes) {
        this.playerModeName = playerModeName;
        this.playerModeNum = playerModeNum;
        this.modeRes = modeRes;
    }

    public String getPlayerModeName() {
        return playerModeName;
    }

    public int getPlayerModeNum() {
        return playerModeNum;
    }

    public int getModeRes() {
        return modeRes;
    }
}
