package com.sovell.retail_cabinet.https;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sovell.retail_cabinet.utils.ConfigUtil;
import com.sovell.retail_cabinet.utils.FileUtil;

import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiDownLoad {

    private ApiService mApiService;

    public ApiDownLoad(DownloadListener listener) {

        Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ssZ").serializeNulls().create();
        DownloadInterceptor downloadInterceptor = new DownloadInterceptor(listener);

        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .readTimeout(20, TimeUnit.SECONDS)
                .connectTimeout(20, TimeUnit.SECONDS)
                .retryOnConnectionFailure(true)
//                .addInterceptor(HttpInterceptor.mLogInterceptor)                      //添加log
                .addInterceptor(downloadInterceptor)                                    //添加下载监听的拦截器
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .baseUrl(ConfigUtil.Instance().getApi(""))
                .build();

        mApiService = retrofit.create(ApiService.class);
    }

    public Observable<ResponseBody> download(String url, final String path) {
        return mApiService
                .updateApp(url)
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.computation())
                .doOnNext(new Consumer<ResponseBody>() {
                    @Override
                    public void accept(ResponseBody responseBody) throws Exception {
                        FileUtil.saveResponseBody(responseBody, path);
                    }
                })
                .observeOn(AndroidSchedulers.mainThread());
    }
}
