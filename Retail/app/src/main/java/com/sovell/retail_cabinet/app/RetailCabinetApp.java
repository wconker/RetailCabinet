package com.sovell.retail_cabinet.app;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

import com.sovell.retail_cabinet.manager.BVMManager;
import com.sovell.retail_cabinet.manager.TermManager;
import com.tencent.bugly.crashreport.CrashReport;

import org.litepal.LitePal;

import timber.log.Timber;

public class RetailCabinetApp extends Application {

    private int mActivityCount = 0;
    private static RetailCabinetApp mInstance;

    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;

        //数据库
        LitePal.initialize(this);
        //初始化 timber 打印
        Timber.plant(new Timber.DebugTree());
        //设置该CrashHandler为程序的默认处理器
//        UnCeHandler catchExcept = new UnCeHandler();
//        Thread.setDefaultUncaughtExceptionHandler(catchExcept);
        //启动时间轮询
        TermManager.Instance().intervalTime();

        CrashReport.initCrashReport(getApplicationContext(), "b4096e61f9", false);

        BVMManager.bindService(this);

        registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks() {
            @Override
            public void onActivityCreated(Activity activity, Bundle savedInstanceState) {

            }

            @Override
            public void onActivityStarted(Activity activity) {
                mActivityCount++;
            }

            @Override
            public void onActivityResumed(Activity activity) {

            }

            @Override
            public void onActivityPaused(Activity activity) {

            }

            @Override
            public void onActivityStopped(Activity activity) {
                mActivityCount--;
            }

            @Override
            public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

            }

            @Override
            public void onActivityDestroyed(Activity activity) {

            }
        });
    }

    public static RetailCabinetApp Instance() {
        return mInstance;
    }

    public int getActivityCount() {
        return mActivityCount;
    }
}
