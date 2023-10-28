package com.example.x_music.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class ReminderService extends Service {  

    private static PendingIntent pendingIntent;
  
    @Override  
    public int onStartCommand(Intent intent, int flags, int startId) {
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        //获取延迟关闭时间
        int minute = intent.getIntExtra("minute", -1);
        if (minute == -1) return START_STICKY;
        //判断是否已有定时器，有就先取消
        if (getPendingIntent() != null) alarmManager.cancel(pendingIntent);
        setAlarm(alarmManager, minute);
        return START_STICKY;  
    }

    private static synchronized PendingIntent getPendingIntent(){
        return pendingIntent;
    }

    public static synchronized void setPendingIntent(PendingIntent pendingIntent) {
        ReminderService.pendingIntent = pendingIntent;
    }

    private void setAlarm(AlarmManager alarmManager, int minute) {
        Intent intent = new Intent(this, ReminderReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        //触发时间
        long triggerAtTime = System.currentTimeMillis() + minute * 60 * 1000L;
        alarmManager.set(AlarmManager.RTC_WAKEUP, triggerAtTime, pendingIntent);
        setPendingIntent(pendingIntent);
    }
  
    @Override  
    public void onDestroy() {  
        super.onDestroy();
    }  
  
    @Override  
    public IBinder onBind(Intent intent) {  
        return null;  
    }  
}