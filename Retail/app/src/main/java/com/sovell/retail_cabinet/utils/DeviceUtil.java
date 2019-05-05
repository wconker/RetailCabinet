package com.sovell.retail_cabinet.utils;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;

import com.sovell.retail_cabinet.app.RetailCabinetApp;

import java.math.BigInteger;
import java.security.MessageDigest;

public class DeviceUtil {

    /**
     * 获取APP版本号
     */
    public static int versionCode() {
        int versionCode = 0;
        PackageManager manager = RetailCabinetApp.Instance().getPackageManager();
        try {
            PackageInfo info = manager.getPackageInfo(RetailCabinetApp.Instance().getPackageName(), 0);
            versionCode = info.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return versionCode;
    }

    /**
     * 获取APP版本名
     */
    public static String versionName() {
        String versionName = "";
        PackageManager manager = RetailCabinetApp.Instance().getPackageManager();
        try {
            PackageInfo info = manager.getPackageInfo(RetailCabinetApp.Instance().getPackageName(), 0);
            versionName = "V" + info.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return versionName;
    }

    /**
     * md5加密方法
     */
    public synchronized static String stringToMD5(String val) {
        MessageDigest digest;
        BigInteger bigInteger = null;
        try {
            digest = MessageDigest.getInstance("MD5");
            bigInteger = new BigInteger(1, digest.digest(val.getBytes("utf-8")));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return String.format("%032x", bigInteger);
    }

    /**
     * 全屏展示
     */
    public static void fullScreen(View view) {
        int systemUiVisibility = view.getSystemUiVisibility();
        int flags = View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
                | View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        systemUiVisibility |= flags;
        view.setSystemUiVisibility(systemUiVisibility);
    }

    /**
     * 检测网络是否连接
     */
    public static boolean isNetConnect() {
        ConnectivityManager cm = (ConnectivityManager) RetailCabinetApp.Instance().getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm != null) {
            NetworkInfo info = cm.getActiveNetworkInfo();
            return info != null && info.isAvailable();
        }
        return false;
    }

    /**
     * 获取屏幕数据
     */
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    public static void displayInformation(Activity activity) {
        Point point = new Point();
        activity.getWindowManager().getDefaultDisplay().getSize(point);
        Log.e("分辨率", "the screen size is " + point.toString());
        activity.getWindowManager().getDefaultDisplay().getRealSize(point);
        Log.e("分辨率", "the screen real size is " + point.toString());

        DisplayMetrics dm = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(dm);
        int width = dm.widthPixels;
        int height = dm.heightPixels;
        float xdpi = dm.xdpi;
        float ydpi = dm.ydpi;
        float density = dm.densityDpi;
        float fdensity = dm.density;
        Log.e("分辨率", width + " = " + height + " = " + xdpi + " = " + ydpi + " = " + density + " = " + fdensity);

        int screenWidth = (int) (width / fdensity);//屏幕宽度(dp)
        int screenHeight = (int) (height / fdensity);//屏幕高度(dp)
        Log.e("分辨率 dp", screenWidth + "======" + screenHeight);
    }
}
