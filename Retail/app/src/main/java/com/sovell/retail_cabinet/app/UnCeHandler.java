package com.sovell.retail_cabinet.app;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import com.sovell.retail_cabinet.ui.SplashActivity;
import com.sovell.retail_cabinet.utils.PortLogUtil;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;

public class UnCeHandler implements Thread.UncaughtExceptionHandler {

    private Thread.UncaughtExceptionHandler mDefaultHandler;

    public UnCeHandler() {
        //获取系统默认的UncaughtException处理器
        mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler();
    }

    @Override
    public void uncaughtException(Thread thread, Throwable ex) {
        writeErrorMsg(ex);
        if (!handleException(ex) && mDefaultHandler != null) {
            //如果用户没有处理则让系统默认的异常处理器来处理
            mDefaultHandler.uncaughtException(thread, ex);
        } else {
            new Thread() {
                @Override
                public void run() {
                    Intent intent = new Intent(RetailCabinetApp.Instance(), SplashActivity.class);
                    intent.addCategory(Intent.CATEGORY_LAUNCHER);
                    intent.setAction(Intent.ACTION_MAIN);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    PendingIntent restartIntent = PendingIntent.getActivity(RetailCabinetApp.Instance(), 0, intent, Intent.FLAG_ACTIVITY_NEW_TASK);
                    AlarmManager mgr = (AlarmManager) RetailCabinetApp.Instance().getSystemService(Context.ALARM_SERVICE);
                    mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 1500, restartIntent);
                    android.os.Process.killProcess(android.os.Process.myPid());
                }
            }.start();
        }
    }

    /**
     * 自定义错误处理,收集错误信息 发送错误报告等操作均在此完成.
     *
     * @param ex 错误信息
     * @return true:如果处理了该异常信息;否则返回false.
     */
    private boolean handleException(Throwable ex) {
        if (ex == null) {
            return false;
        }
        return true;
    }

    /**
     * 写入错误信息
     *
     * @param ex 错误信息
     */
    private void writeErrorMsg(Throwable ex) {
        Writer writer = new StringWriter();
        PrintWriter pw = new PrintWriter(writer);
        ex.printStackTrace(pw);
        String errorResult = writer.toString();
        PortLogUtil.writeCrashLog(errorResult);
    }
}
