package com.example.x_music.datasource;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import androidx.annotation.Nullable;
import com.example.x_music.callback.VoidCallBack;
import com.example.x_music.activity.ContextApp;
import com.example.x_music.entity.MusicInfo;
import com.example.x_music.music_api.WuSunMusicApi;

/**
 * music info db工具类
 */
public class MusicInfoDB extends SQLiteOpenHelper {

    private static final String DB_NAME = "music.db";
    private static final int DB_VERSION = 1;
    private static final String TABLE_NAME = "music";

    private MusicInfoDB(@Nullable Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    /**
     * 是否为空
     * @param s 字符串
     * @return true为空
     */
    private static boolean isEmpty(String s){
        return s == null || s.length() == 0;
    }


    /**
     * 根据music info构建参数
     * @param musicInfo 音乐详情
     * @return 参数
     */
    private static ContentValues buildArgs(MusicInfo musicInfo){
        ContentValues values = new ContentValues();
        if (!isEmpty(musicInfo.getMusicId())) {
            values.put("music_id", musicInfo.getMusicId());
        }
        if (!isEmpty(musicInfo.getPic())) {
            values.put("pic", musicInfo.getPic());
        }
        if (!isEmpty(musicInfo.getPicCacheId())) {
            values.put("pic_cache_id", musicInfo.getPicCacheId());
        }
        if (!isEmpty(musicInfo.getLrc())) {
            values.put("lrc", musicInfo.getLrc());
        }
        if (!isEmpty(musicInfo.getLrcCacheId())) {
            values.put("lrc_cache_id", musicInfo.getLrcCacheId());
        }
        if (!isEmpty(musicInfo.getPlayerUrl())) {
            values.put("play_url", musicInfo.getPlayerUrl());
        }
        if (!isEmpty(musicInfo.getAuthor())) {
            values.put("author", musicInfo.getAuthor());
        }
        if (!isEmpty(musicInfo.getPlayerUrlCacheId())) {
            values.put("play_url_cache_id", musicInfo.getPlayerUrlCacheId());
        }
        if (!isEmpty(musicInfo.getTitle())) {
            values.put("title", musicInfo.getTitle());
        }
        return values;
    }

    /**
     * 获取db
     * @return db
     */
    private static MusicInfoDB getDB(){
        MusicInfoDB db = new MusicInfoDB(ContextApp.getContext());
        SQLiteDatabase wdb = db.getWritableDatabase();
//        wdb.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        // 在这里执行创建数据库的SQL语句
        String sql = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME
                + "(id INTEGER PRIMARY KEY AUTOINCREMENT , "
                + "music_id  TEXT,"
                + "pic TEXT,"
                + "pic_cache_id TEXT,"
                + "lrc TEXT,"
                + "lrc_cache_id TEXT,"
                + "play_url TEXT,"
                + "author TEXT,"
                + "play_url_cache_id TEXT,"
                + "title TEXT,"
                + "unique(music_id))";
        wdb.execSQL(sql);
        wdb.close();
        return db;
    }

    /**
     * 更新 musicInfo
     * @param musicInfo musicInfo
     */
    public static void update(MusicInfo musicInfo){
        if (musicInfo.getId() == null) return;
        MusicInfoDB db = getDB();
        SQLiteDatabase writableDatabase = db.getWritableDatabase();
        writableDatabase.update(TABLE_NAME, buildArgs(musicInfo), "id=?", new String[]{"" + musicInfo.getId()});
        writableDatabase.close();
        db.close();
    }

    /**
     * 根据id获取music info
     * @param id id
     * @return music info
     */
    public static MusicInfo getById(int id){
        MusicInfoDB db = getDB();
        SQLiteDatabase database = db.getReadableDatabase();
        Cursor cursor = database.query(TABLE_NAME, null, "id=?", new String[]{"" + id}, null, null, "id asc");
        MusicInfo info = null;
        if (cursor.moveToNext()){
            info = MusicInfo.builder()
                    .id(cursor.getInt(0))
                    .musicId(cursor.getString(1))
                    .pic(cursor.getString(2))
                    .picCacheId(cursor.getString(3))
                    .lrc(cursor.getString(4))
                    .lrcCacheId(cursor.getString(5))
                    .playerUrl(cursor.getString(6))
                    .author(cursor.getString(7))
                    .playerUrlCacheId(cursor.getString(8))
                    .title(cursor.getString(9))
                    .build();
        }
        cursor.close();
        database.close();
        db.close();
        return info;
    }

    /**
     * 根据 id删除 music info
     * @param id id
     */
    public static void del(int id){
        MusicInfoDB db = getDB();
        SQLiteDatabase database = db.getWritableDatabase();
        database.delete(TABLE_NAME, "id=", new String[]{""+id});
        database.close();
        db.close();
    }

    /**
     * 创建music info
     * @param musicInfo music info
     */
    public static void insert(MusicInfo musicInfo){
        //因为有play url可能为空，所有先获取链接再插入
        if (musicInfo.getPlayerUrl() == null){
            try {
                WuSunMusicApi.setPicAndLrcAndPlayUrl(musicInfo, new VoidCallBack<MusicInfo>() {
                    @Override
                    public void onSucceed(MusicInfo musicInfo) {
                        update(musicInfo);
                    }
                    @Override
                    public void onFail() {
                    }
                });
            }catch (Exception ignored){

            }
        }
        MusicInfoDB db = getDB();
        SQLiteDatabase writableDatabase = db. getWritableDatabase();
        long id = writableDatabase.insert(TABLE_NAME, null, buildArgs(musicInfo));
        musicInfo.setId((int) id);
        writableDatabase.close();
        db.close();
    }

    /**
     * 为music info设置id
     * @param info MusicInfo
     */
    public static void checkMusicId(MusicInfo info){
        MusicInfoDB db = getDB();
        SQLiteDatabase wdb =db. getWritableDatabase();
        Cursor cursor = null;
        try {
            cursor = wdb.rawQuery("select id from " + TABLE_NAME + " where music_id = ?", new String[]{info.getMusicId()});
            cursor.moveToFirst();
            int id = cursor.getInt(0);
            info.setId(id);
        }catch (Exception e){

        }finally {
            if (cursor != null) cursor.close();
            wdb.close();
            db.close();
        }

    }

    @Override
    public void onCreate(SQLiteDatabase db) {

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // 在这里执行升级数据库的SQL语句
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }
}
