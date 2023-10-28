package com.example.x_music.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;
import com.example.x_music.activity.ContextApp;
import com.example.x_music.fragment.SettingFragment;
import com.example.x_music.util.SharedPreferencesUtil;

public class ReminderReceiver extends BroadcastReceiver {

    @Override  
    public void onReceive(Context context, Intent intent) {
        Toast.makeText(context, "暂停播放", Toast.LENGTH_SHORT).show();
        ContextApp.getPlayer().pause();
        ReminderService.setPendingIntent(null);
        SharedPreferencesUtil.putString(SettingFragment.CLOSE_TIME, null);
    }

}