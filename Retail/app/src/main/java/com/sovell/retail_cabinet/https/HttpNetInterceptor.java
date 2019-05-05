package com.sovell.retail_cabinet.https;

import android.support.annotation.NonNull;

import com.sovell.retail_cabinet.utils.DeviceUtil;

import java.io.IOException;

import okhttp3.CacheControl;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import timber.log.Timber;

/**
 * OKHttp3 添加网络拦截器
 */
class HttpNetInterceptor {

    /*设置网络拦截器（设置缓存）*/
    static class HttpCacheInterceptor implements Interceptor {

        @Override
        public Response intercept(@NonNull Chain chain) throws IOException {
            Request request = chain.request();
            if (!DeviceUtil.isNetConnect()) {
                request = request.newBuilder()
                        .cacheControl(CacheControl.FORCE_CACHE)
                        .build();
                Timber.e("HttpNetInterceptor ======no network======");
            }

            Response originalResponse = chain.proceed(request);
            if (DeviceUtil.isNetConnect()) {
                //有网的时候读接口上的@Headers里的配置，你可以在这里进行统一的设置
                String cacheControl = request.cacheControl().toString();
                //在线缓存5分钟
                int maxAge = 60 * 5;
                return originalResponse.newBuilder()
                        .removeHeader("Pragma")
                        .removeHeader("Cache-Control")  //不知道要不要这行代码，没试过
                        //.header("Cache-Control", cacheControl)
                        .header("Cache-Control", "public, max-age=" + maxAge)
                        .build();
            } else {
                //离线缓存1周
                int maxStale = 60 * 60 * 24 * 1 * 7;
                return originalResponse.newBuilder()
                        .removeHeader("Pragma")
                        .removeHeader("Cache-Control")      //不知道要不要这行代码，没试过
                        .header("Cache-Control", "public, only-if-cached, max-stale=" + maxStale)
                        .build();
            }
        }
    }

    /**
     * 全局自动刷新Token的拦截器
     */
    public static class TokenInterceptor implements Interceptor {

        @Override
        public Response intercept(Chain chain) throws IOException {
            Request request = chain.request();
            Response response = chain.proceed(request);
//            Logger.e("HttpNetInterceptor response.code=" + response.code());

            if (isTokenExpired(response)) {//根据和服务端的约定判断token过期
                Timber.e("HttpNetInterceptor 静默自动刷新Token,然后重新请求数据");
                //同步请求方式，获取最新的Token
                String newSession = getNewToken();
                //使用新的Token，创建新的请求
                Request newRequest = chain.request()
                        .newBuilder()
                        .header("Cookie", "JSESSIONID=" + newSession)
                        .build();
                //重新请求
                return chain.proceed(newRequest);
            }
            return response;
        }

        /**
         * 根据Response，判断Token是否失效
         */
        private boolean isTokenExpired(Response response) {
            if (response.code() == 404) {
                return true;
            }
            return false;
        }

        /**
         * 同步请求方式，获取最新的Token
         */
        private String getNewToken() throws IOException {
            String token = "";
            // 通过一个特定的接口获取新的token，此处要用到同步的retrofit请求
            /*Response_Login loginInfo = CacheManager.restoreLoginInfo(App.getAppContext());
            String username = loginInfo.getUserName();
            String password = loginInfo.getPassword();

            Logger.e("HttpNetInterceptor loginInfo=" + loginInfo.toString());
            Call<Response_Login> call = WebHelper.getSyncInterface().synclogin(new Request_Login(username, password));
            loginInfo = call.execute().body();
            LogUtil.print("loginInfo=" + loginInfo.toString());

            loginInfo.setPassword(password);
            CacheManager.saveLoginInfo(loginInfo);*/
            return token;
        }
    }
}
