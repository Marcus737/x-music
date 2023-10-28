package com.example.x_music.datasource;


import com.example.x_music.activity.ContextApp;
import com.example.x_music.entity.MusicInfo;
import com.example.x_music.util.HttpUtil;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

import java.io.*;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * 缓存文件工具类
 * 用来异步加载歌曲，图片等
 */
public class CacheDataSource {
    //缓存目录
    private static File cacheDir;

    public  static void setCacheDir(File cacheDir) {
        CacheDataSource.cacheDir = cacheDir;
    }

    public static long getCacheSize(){
        return getDirectorySize(cacheDir);
    }

    /**
     * 清除缓存
     */
    public static void clearCache(){
        //添加不被清除名单set
        HashSet<String> set = new HashSet<>();
        List<MusicInfo> playList = ContextApp.getPlayer().getPlayList();
        MusicInfo musicInfo = ContextApp.getPlayer().getCurrentPlayMusicInfo();
        if (musicInfo != null) playList.add(musicInfo);
        if (playList != null){
            for (MusicInfo info : playList) {
                set.add(info.getPlayerUrlCacheId());
                set.add(info.getLrcCacheId());
                set.add(info.getPicCacheId());
            }
        }
        deleteDirectory(cacheDir, set);
    }

    /**
     * 递归删除文件
     * @param directory 根目录
     * @param excludes 排除的文件
     */
    public static void deleteDirectory(File directory, Set<String> excludes) {
        if (directory.exists()) {
            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isDirectory()) {
                        deleteDirectory(file, excludes); // 递归删除子目录
                    } else {
                        if (excludes.contains(file.getName())) continue;
                        file.delete(); // 删除文件
                    }
                }
            }
            directory.delete(); // 删除目录本身
        }
    }

    /**
     * 获取目录下所有文件大小总和
     * @param directory 目录
     * @return 有多少byte
     */
    public static long getDirectorySize(File directory) {
        if (!directory.exists()) {
            return 0;
        }
        if (!directory.isDirectory()) {
            return directory.length();
        }
        long size = 0;
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                size += getDirectorySize(file);
            }
        }
        return size;
    }


    /**
     * 根据文件名获取文件
     * @param fileName 文件名
     * @return 文件
     */
    private static File getFile(String fileName){
        //将文件名的/全部替换成.
        fileName = fileName.replaceAll("/",".");
        String path = cacheDir.getAbsolutePath() +"/" + fileName;
        return new File(path);
    }

    /**
     * 创建文件，存在则覆盖
     * @param fileName 文件名
     * @return 文件
     * @throws IOException 文件操作异常
     */
    private static File createFile(String fileName) throws IOException {
        File file = getFile(fileName);
        if (file.exists()) {
            file.delete();
            file.createNewFile();
        }
        return file;
    }

    /**
     * 根据文件名获取文件
     * @param fileName 文件名
     * @return 返回null表示文件不存在
     */
    public static File getFileByFileName(String fileName){
        if (fileName == null) return null;
        File file = getFile(fileName);
        if (file.exists()) return file;
        return null;
    }


    /**
     * 异步执行下载文件
     * 随机文件名
     * @param url url
     * @param callback 回调
     * @throws IOException io异常
     */
    public static String download(String url, FileCallback callback) {
        String id = UUID.randomUUID().toString();
        download(url, id, callback);
        return id;
    }


    /**
     * 异步执行下载文件
     * @param url url
     * @param fileName 文件名
     * @param callback 回调
     * @throws IOException io异常
     */
    public static void download(String url, String fileName, FileCallback callback) {
        Call call = HttpUtil.download(url);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.callback(null);
            }

            @Override
            public void onResponse(Call call, Response response) {
                File file = download(url, fileName);
                response.close();
                callback.callback(file);
            }
        });
    }

    /**
     * 同步版本
     * 根据指定url下载文件
     * @param url url
     * @param filename 保存的文件名
     * @return 文件对象
     */
    public static File download(String url, String filename)  {
        Call call = HttpUtil.download(url);
        File file = null;
        try {
            Response response = call.execute();
            file = saveInCache(response.body().byteStream(), filename);
            response.body().close();
        }catch (Exception e){
            e.printStackTrace();
        }
        return file;
    }

    /**
     * 将输入流的内容保存进文件
     * 文件名相同的将会覆盖
     * @param is 输入流
     * @param fileName 文件名
     * @return 文件对象
     */
    public static File saveInCache(InputStream is, String fileName) throws IOException {
        File file = createFile(fileName);
        FileOutputStream fos = new FileOutputStream(file);
        write(is, fos);
        fos.close();
        return file;
    }

    /**
     * 将输入流的内容写进输出流
     * @param is 输入流
     * @param os 输出流
     */
    private static void write(InputStream is, OutputStream os) throws IOException {
        int len = 0;
        byte[] data = new byte[4 * 1024];
        while ((len = is.read(data)) != -1){
            os.write(data, 0, len);
        }
    }

    public interface FileCallback {
        void callback(File file) ;
    }

}
