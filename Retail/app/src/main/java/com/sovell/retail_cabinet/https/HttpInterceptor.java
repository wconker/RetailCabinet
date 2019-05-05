package com.sovell.retail_cabinet.https;

import android.support.annotation.NonNull;

import com.sovell.retail_cabinet.base.BaseDownLoad;
import com.sovell.retail_cabinet.utils.PortLogUtil;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import timber.log.Timber;

/**
 * Http 的一些常用拦截器
 */

class HttpInterceptor {

    private static final String X_LC_Id = "i7j2k7bm26g7csk7uuegxlvfyw79gkk4p200geei8jmaevmx";
    private static final String X_LC_Key = "n6elpebcs84yjeaj5ht7x0eii9z83iea8bec9szerejj7zy3";

    //添加header信息
    public static Interceptor mHeaderInterceptor = new Interceptor() {
        @Override
        public Response intercept(@NonNull Chain chain) throws IOException {
            Response response = chain.proceed(chain.request().newBuilder()
                    .addHeader("X-LC-Id", X_LC_Id)
                    .addHeader("X-LC-Key", X_LC_Key)
                    .addHeader("Content-Type", "application/json")
                    .build());
            return response;
        }
    };

    /**
     * 设置拦截器，添加Log，打印响应信息
     * 当相应数据做了encoder处理，可以在回调中做处理，但是下载时会崩，不知道为啥
     */
    public static HttpLoggingInterceptor mLogInterceptor = new HttpLoggingInterceptor(new HttpLoggingInterceptor.Logger() {
        @Override
        public void log(@NonNull String message) {
            Timber.e(message);
            PortLogUtil.writePortLog(message);
        }
    }).setLevel(HttpLoggingInterceptor.Level.BODY);

    /*设置log 拦截器，打印响应数据*/
//    public static HttpLoggingInterceptor mLogInterceptor = new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY);


    /*下载进度拦截器的监听方法*/
    private static DownloadListener downloadListener = new DownloadListener() {
        @Override
        public void update(long bytesRead, long contentLength, boolean done) {
            //不频繁发送通知，防止通知栏下拉卡顿
            int progress = (int) ((bytesRead * 100) / contentLength);
            if (progress > 0) {
                BaseDownLoad bean = new BaseDownLoad();
                bean.setTotalFileSize(contentLength);
                bean.setCurrentFileSize(bytesRead);
                bean.setProgress(progress);

                // 这里可以发送广播到需要的地方，来显示下载的进度
                RxBus.get().post(bean);
            }
        }
    };

    /*下载进度拦截器*/
    public static DownloadInterceptor mDownloadInterceptor =
            new DownloadInterceptor(downloadListener);

    /*在body中设置固定参数的拦截器*/
    public static BaseParamsInterceptor mBaseParamsInterceptor =
            new BaseParamsInterceptor.Builder()
//                    .addParam("SHIT_ID", HttpsAddress.SHIT_ID)
//                    .addParam("TERROR_ID", DevicesUtil.getAppVersion())
//                    .addParam("CRAZY_ID", DevicesUtil.getPhoneIMEI())
//                    .addParam("TROUBLE_ID", DevicesUtil.getPhoneModel())
//                    .addParam("DEATH_ID", DevicesUtil.getSystemVersion())
//                    .addParam("RECOVERY_ID", "0")
//                    .addParam("NETWORK_TYPE", NetWorkUtil.getNetworkType())
//                    .addParam("SERVER_VERSION", HttpsAddress.getServerVersion())
                    .addParam("BEACH_ID", "")                //用户的authOpenId
                    .interceptor;

}
