package com.example.x_music.datasource;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import androidx.annotation.Nullable;
import com.example.x_music.activity.ContextApp;
import com.example.x_music.entity.MusicInfo;
import com.example.x_music.entity.PlayList;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PlaylistDB extends SQLiteOpenHelper {

    private static final String DB_NAME = "music.db";
    private static final String TABLE_NAME = "playlist";
    private static final int DB_VERSION = 1;
    private PlaylistDB(@Nullable Context context) {
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

    private static PlaylistDB getDB(){
        PlaylistDB db = new PlaylistDB(ContextApp.getContext());
        SQLiteDatabase wdb = db.getWritableDatabase();
//        wdb.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        // 在这里执行创建数据库的SQL语句
        String sql = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME
                + "(id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "icon TEXT,"
                + "icon_cache_id TEXT,"
                + "play_list_name TEXT,"
                + "music_info_ids TEXT)";
        wdb.execSQL(sql);
        wdb.close();
        return db;
    }
    /**
     * 根据music info构建参数
     * @param playList 歌单
     * @return 参数
     */
    private static ContentValues buildArgs(PlayList playList){
        ContentValues values = new ContentValues();
        if (!isEmpty(playList.getIcon())) {
            values.put("icon", playList.getIcon());
        }
        if (!isEmpty(playList.getIconCacheId())) {
            values.put("icon_cache_id", playList.getIconCacheId());
        }
        if (!isEmpty(playList.getPlayListName())) {
            values.put("play_list_name", playList.getPlayListName());
        }
        if (!isEmpty(playList.getMusicInfoIds())) {
            values.put("music_info_ids", playList.getMusicInfoIds());
        }
        return values;
    }

    /**
     * 获取歌单中的音乐id列表，以 '-'分割
     * @param playList 歌单
     * @return 音乐id列表
     */
    public static String[] getMusicInfoIdsArray(PlayList playList) {
        playList = PlaylistDB.getById(playList.getId());
        String musicInfoIds = playList.getMusicInfoIds();
        if (musicInfoIds == null || musicInfoIds.length() == 0) return new String[0];
        return musicInfoIds.split("-");
    }

    /**
     * 从歌单中删除音乐
     * @param playList 歌单
     * @param id 音乐id
     */
    public static void deleteMusicInfoId(PlayList playList, String id){
        playList = PlaylistDB.getById(playList.getId());
        String musicInfoIds = playList.getMusicInfoIds();
        String replace = musicInfoIds.replace(id, "").replaceAll("--", "-");
        playList.setMusicInfoIds(replace);
        PlaylistDB.update(playList);
    }

    /**
     * 添加音乐到歌单
     * @param playList 歌单
     * @param musicInfo 音乐
     */
    public static void addMusicIdToPlaylist(PlayList playList, MusicInfo musicInfo) {
        if (playList == null || musicInfo == null) return;
        playList = PlaylistDB.getById(playList.getId());
        //获取mid
        if (musicInfo.getId() == null) {
            MusicInfoDB.checkMusicId(musicInfo);
            if (musicInfo.getId() == null){
                MusicInfoDB.insert(musicInfo);
            }
        }
        String mid = "" + musicInfo.getId();
        if (playList.getMusicInfoIds() == null ){
            //歌单的音乐id为空，直接设置
            playList.setMusicInfoIds(mid);
        }
        else {
            //判断是否以存在
            String[] ids = getMusicInfoIdsArray(playList);
            for (String s : ids) {
                if (mid.equals(s)) return;
            }
            //添加
            playList.setMusicInfoIds(playList.getMusicInfoIds() +   "-" + mid);
        }
        //更新数据库
        PlaylistDB.update(playList);
    }

    /**
     * 查询歌单列表
     * @return 歌单列表
     */
    public static List<PlayList> selectList(){
        PlaylistDB db = getDB();
        List<PlayList> list = new ArrayList<>();
        SQLiteDatabase database = db.getReadableDatabase();
        Cursor cursor = database.query(TABLE_NAME, null, null, null, null, null, "id asc");
        while (cursor.moveToNext()){
            PlayList info = PlayList.builder()
                    .id(cursor.getInt(0))
                    .icon(cursor.getString(1))
                    .iconCacheId(cursor.getString(2))
                    .playListName(cursor.getString(3))
                    .musicInfoIds(cursor.getString(4))
                    .build();
            list.add(info);
        }
        cursor.close();
        database.close();
        db.close();
        return list;
    }

    /**
     * 根据歌单id获取歌单
     * @param id id
     * @return 歌单
     */
    public static PlayList getById(int id){
        PlaylistDB db = getDB();
        SQLiteDatabase database = db.getReadableDatabase();
        Cursor cursor = database.query(TABLE_NAME, null, "id=?", new String[]{"" + id}, null, null, "id desc");
        PlayList info = null;
        if (cursor.moveToNext()){
            info =   PlayList.builder()
                    .id(cursor.getInt(0))
                    .icon(cursor.getString(1))
                    .iconCacheId(cursor.getString(2))
                    .playListName(cursor.getString(3))
                    .musicInfoIds(cursor.getString(4))
                    .build();
        }
        cursor.close();
        database.close();
        db.close();
        return info;
    }


    /**
     * 根据歌单id删除歌单
     * @param id 歌单id
     */
    public static void del(int id){
        PlaylistDB db = getDB();
        SQLiteDatabase database = db.getWritableDatabase();
        database.delete(TABLE_NAME, "id=", new String[]{""+id});
        database.close();
        db.close();
    }

    /**
     * 新建歌单
     * @param playList 歌单
     * @return 歌单id
     */
    public static int insert(PlayList playList){
        PlaylistDB db = getDB();
        ContentValues values = buildArgs(playList);
        SQLiteDatabase writableDatabase = db.getWritableDatabase();
        long id = writableDatabase.insert(TABLE_NAME, null, values);
        playList.setId((int) id);
        writableDatabase.close();
        db.close();
        return (int) id;
    }

    /**
     * 更新歌单
     * @param playList 歌单
     */
    public static void update(PlayList playList) {
        PlaylistDB db = getDB();
        SQLiteDatabase writableDatabase = db.getWritableDatabase();
        writableDatabase.update(TABLE_NAME, buildArgs(playList), "id=?", new String[]{"" + playList.getId()});
        writableDatabase.close();
        db.close();
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
