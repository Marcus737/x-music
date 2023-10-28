package com.example.x_music.util;


import okhttp3.*;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class HttpUtil {

    private static OkHttpClient client ;

    static {
        client = new OkHttpClient.Builder()
                .connectTimeout(20, TimeUnit.SECONDS)
                .writeTimeout(20, TimeUnit.SECONDS)
                .readTimeout(20, TimeUnit.SECONDS)
                .build();
    }

    public static void close() throws IOException {
        client.dispatcher().executorService().shutdown();   //清除并关闭线程池线程池
        client.connectionPool().evictAll();                 //清除并关闭连接池
    }

    /**
     * 发送post请求
     * @param url url
     * @param params 参数
     * @param headers 请求头
     * @param callback 回调
     */
    public static void post(String url, Map<String, String> params, HashMap<String, String> headers, Callback callback){
        FormBody.Builder builder = new FormBody.Builder();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            builder.add(entry.getKey(), entry.getValue());
        }
        Headers.Builder hds = new Headers.Builder();
        for (Map.Entry<String, String> entry : headers.entrySet()) {
            hds.add(entry.getKey(), entry.getValue());
        }
        // 构建Request对象
        Request request = new Request.Builder()
                .url(url)
                .headers(hds.build())
                .post(builder.build())
                .build();

        client.newCall(request).enqueue(callback);
    }

    /**
     * 同步get请求
     * @param url url
     * @return 请求结果
     * @throws IOException 异常
     */
    public static String get(String url) throws IOException {
        // 构建Request对象
        Request request = new Request.Builder()
                .url(url)
                .build();
        // 构建Call对象
        Call call = client.newCall(request);

        Response response = call.execute();
        if (!response.isSuccessful()) {
            return "";
        }
        String s = response.body().string();
        response.close();
        return s;
    }

    /**
     * 异步get
     * @param url url
     * @param callback 回调
     */
    public static void get(String url, Callback callback){
        // 构建Request对象
        Request request = new Request.Builder()
                .url(url)
                .build();
        // 构建Call对象
        Call call = client.newCall(request);
        call.enqueue(callback);
    }


    /**
     * 异步下载
     * @param url url
     * @return call
     */
    public static Call download(String url) {
        // 构建Request对象
        Request request = new Request.Builder()
                .url(url)
                .build();
        return client.newCall(request);
    }

    /**
     * 将url的内容下载到指定文件
     * @param url url
     * @param file 指定文件
     * @throws Exception 异常
     */
    public static void download(String url, File file) throws Exception {
        if (file == null) return;
        // 构建Request对象
        Request request = new Request.Builder()
                .url(url)
                .build();
        Response response = client.newCall(request)
                .execute();
        InputStream is = response.body().byteStream();
        FileOutputStream fos = new FileOutputStream(file);
        swap(is, fos);
        fos.close();
        is.close();
        response.close();
    }

    /**
     * 将输入流的数据复制到输出流
     * @param is 输入流
     * @param os 输出流
     * @throws IOException 异常
     */
    public static void swap(InputStream is, OutputStream os) throws IOException {
        byte[] cache = new byte[4*1024];
        int len;
        while ((len = is.read(cache)) != -1) os.write(cache, 0, len);
    }



}
