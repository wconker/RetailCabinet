package com.sovell.retail_cabinet.https;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sovell.retail_cabinet.app.RetailCabinetApp;

import java.io.File;
import java.util.concurrent.TimeUnit;

import okhttp3.Cache;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * 因Api.class中有固定参数，上传失败
 * 暂时这样写，没想到好的方法解决
 */

class ApiUpload {

    private static final String CACHE_PATH = "cache";
    private static final String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ssZ";


    //在访问HttpMethods时创建单例
    private static class ApiHolder {
        private static final ApiUpload INSTANCE = new ApiUpload();
    }

    //获取单例
    public static ApiUpload Instance() {
        return ApiHolder.INSTANCE;
    }

    //构造方法私有
    private ApiUpload() {

        /*设置缓存*/
        File cacheFile = new File(RetailCabinetApp.Instance().getCacheDir(), CACHE_PATH);
        Cache cache = new Cache(cacheFile, 1024 * 1024 * 50); //50Mb

        /*初始化OkHttp*/
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                    .readTimeout(20, TimeUnit.SECONDS)
                    .connectTimeout(20, TimeUnit.SECONDS)
                    .retryOnConnectionFailure(true)
//                    .sslSocketFactory(HttpSLL.getSLLContext().getSocketFactory())         //添加https证书
//                    .addInterceptor(HttpInterceptor.mHeaderInterceptor)                   //添加请求header
//                    .addInterceptor(HttpInterceptor.mLogInterceptor)                      //添加log
//                    .addInterceptor(HttpInterceptor.mDownloadInterceptor)                 //添加下载监听的拦截器
//                    .addNetworkInterceptor(new HttpNetInterceptor.TokenInterceptor())     //检测Token
//                    .addNetworkInterceptor(new HttpNetInterceptor.HttpCacheInterceptor())
//                    .cache(cache)
                    .build();

        /*添加json格式*/
        //Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ssZ").serializeNulls().create();
        Gson gson = new GsonBuilder().setDateFormat(DATE_FORMAT).create();

        Retrofit retrofit = new Retrofit.Builder()
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
//                .baseUrl(HttpsAddress.UPLOAD_URL)
                .build();
        ApiService mApiService = retrofit.create(ApiService.class);
    }

}
