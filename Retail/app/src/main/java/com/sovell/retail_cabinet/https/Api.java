package com.sovell.retail_cabinet.https;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sovell.retail_cabinet.app.RetailCabinetApp;
import com.sovell.retail_cabinet.utils.ConfigUtil;

import java.io.File;
import java.util.concurrent.TimeUnit;

import okhttp3.Cache;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.simplexml.SimpleXmlConverterFactory;

/**
 * retrofit2 的 API
 * 可以添加拦截器
 */
public class Api {

    private static final String CACHE_PATH = "cache";
    private static final String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ssZ";

    private static Api mInstance;
    public ApiService mApiService;
    public ApiService mApiXmlService;

    public synchronized static Api Instance() {
        if (mInstance == null) {
            mInstance = new Api("");
        }
        return mInstance;
    }

    /*设置时调用，防止修改api时，单例不变的问题*/
    public synchronized static Api Instance(String url) {
        mInstance = new Api(url);
        return mInstance;
    }

    public synchronized static void setNullForInstance() {
        mInstance = null;
    }

    //构造方法私有
    private Api(String url) {

        /*设置缓存*/
        File cacheFile = new File(RetailCabinetApp.Instance().getCacheDir(), CACHE_PATH);
        Cache cache = new Cache(cacheFile, 1024 * 1024 * 50); //50Mb

        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .readTimeout(20, TimeUnit.SECONDS)
                .connectTimeout(20, TimeUnit.SECONDS)
                .retryOnConnectionFailure(true)
//                .sslSocketFactory(HttpSLL.getSLLContext().getSocketFactory())          //添加https证书
//                .addInterceptor(HttpInterceptor.mHeaderInterceptor)                    //添加请求header
//                .addInterceptor(HttpInterceptor.mBaseParamsInterceptor)                //公共参数
                .addInterceptor(HttpInterceptor.mLogInterceptor)                         //添加log
//                .addInterceptor(HttpInterceptor.mDownloadInterceptor)                  //添加下载监听的拦截器
//                .addNetworkInterceptor(new HttpNetInterceptor.TokenInterceptor())      //检测Token
//                .addNetworkInterceptor(new HttpNetInterceptor.HttpCacheInterceptor())  //设置缓存的拦截器
//                .cache(cache)
                .build();

        /*添加json格式*/
        Gson gson = new GsonBuilder().setDateFormat(DATE_FORMAT).serializeNulls().create();

        try {
            Retrofit retrofit = new Retrofit.Builder()
                    .client(okHttpClient)
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                    .baseUrl(ConfigUtil.Instance().getApi(url))
                    .build();
            mApiService = retrofit.create(ApiService.class);

            Retrofit retrofitXml = new Retrofit.Builder()
                    .client(okHttpClient)
                    .addConverterFactory(SimpleXmlConverterFactory.create())
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                    .baseUrl(ConfigUtil.Instance().getApi(url))
                    .build();
            mApiXmlService = retrofitXml.create(ApiService.class);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
