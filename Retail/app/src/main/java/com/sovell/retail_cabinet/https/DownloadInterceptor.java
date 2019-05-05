package com.sovell.retail_cabinet.https;

import android.support.annotation.NonNull;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Response;

/**
 * Download 拦截器
 */
class DownloadInterceptor implements Interceptor {

    private DownloadListener listener;

    DownloadInterceptor(DownloadListener listener) {
        this.listener = listener;
    }

    @Override
    public Response intercept(@NonNull Chain chain) throws IOException {
        Response originalResponse = chain.proceed(chain.request());

        return originalResponse.newBuilder()
                .body(new DownloadResponseBody(originalResponse.body(), listener))
                .build();
    }
}
