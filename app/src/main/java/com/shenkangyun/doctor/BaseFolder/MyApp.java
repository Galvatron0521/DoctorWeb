package com.shenkangyun.doctor.BaseFolder;

import android.content.Context;

import com.zhy.http.okhttp.OkHttpUtils;

import org.litepal.LitePal;
import org.litepal.LitePalApplication;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;

/**
 * Created by Administrator on 2018/6/19.
 */

public class MyApp extends LitePalApplication {
    private static Context context;

    @Override
    public void onCreate() {
        super.onCreate();
        //获取全局Context
        context = getApplicationContext();
        //初始化LitePal
        LitePal.initialize(this);
        //初始化Http
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                //.addInterceptor(new LoggerInterceptor("TAG"))
                .connectTimeout(10000L, TimeUnit.MILLISECONDS)
                .readTimeout(10000L, TimeUnit.MILLISECONDS)
                //其他配置
                .build();

        OkHttpUtils.initClient(okHttpClient);
    }

    public static Context getContext() {
        return context;
    }
}
