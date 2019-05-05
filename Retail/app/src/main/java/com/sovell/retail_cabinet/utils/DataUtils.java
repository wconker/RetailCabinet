package com.sovell.retail_cabinet.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import java.text.DecimalFormat;

/**
 * Created by hulong on 2018/4/27.
 * 常用工具类
 */

public class DataUtils {
    /**
     * 根据手机的分辨率从 dp 的单位 转成为 px(像素)
     */
    public static int dip2px(Context context, double dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        //Log.e("density: ", String.valueOf(scale));
        return (int) (dpValue * scale + 0.5f);
    }

    /**
     * px转换dip
     */
    public static int px2dip(Context context, int px) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (px / scale + 0.5f);
    }

    /**
     * 检查网络是否可用
     *
     * @param context
     * @return
     */
    public static boolean isNetworkAvailable(Context context) {

        ConnectivityManager manager = (ConnectivityManager) context
                .getApplicationContext().getSystemService(
                        Context.CONNECTIVITY_SERVICE);

        if (manager == null) {
            return false;
        }

        NetworkInfo networkinfo = manager.getActiveNetworkInfo();

        if (networkinfo == null || !networkinfo.isAvailable()) {
            return false;
        }

        return true;
    }

    /**
     * 保留两位小数
     * DecimalFormat is a concrete subclass of NumberFormat that formats decimal numbers.
     * @param d
     * @return
     */
    public static String formatDouble(double d) {
        DecimalFormat df = new DecimalFormat("######0.00");
        return df.format(d);
    }
}
