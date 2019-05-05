package com.sovell.retail_cabinet.utils;

import android.os.Environment;

import java.io.File;
import java.io.FileOutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class PortLogUtil {

    private static final SimpleDateFormat FORMAT_DATE = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS", Locale.CHINA);
    private static final SimpleDateFormat FORMAT_YEAR = new SimpleDateFormat("yyyy-MM-dd", Locale.CHINA);

    private static final String DEVICE_PATH = Environment.getExternalStorageDirectory().toString() + "/零售柜日志/";
    private static final String DEVICE_PATH_CRASH = Environment.getExternalStorageDirectory().toString() + "/零售柜日志/崩溃日志.txt";

    private static Date dateBefore(int day) {
        Date date = new Date(System.currentTimeMillis());
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.DAY_OF_MONTH, -day);
        return calendar.getTime();
    }

    private static boolean createDirectory() {
        File file1 = new File(DEVICE_PATH);
        if (!file1.exists()) {
            return file1.mkdir();
        }
        return true;
    }

    public static void deleteTimeoutFiles() {
        if (createDirectory()) {
            Date delDate = dateBefore(7);
            File[] files = new File(DEVICE_PATH).listFiles();
            if (files == null) return;
            for (File file : files) {
                try {
                    String fileName = file.getName().replace(".txt", "");
                    Date fileDate = FORMAT_YEAR.parse(fileName);
                    if (fileDate.getTime() < delDate.getTime()) {
                        boolean result = file.delete();
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                    continue;
                }
            }
        }
    }

    public static synchronized void writePortLog(String log) {
        if (createDirectory()) {
            FileOutputStream fos = null;
            try {
                File file = new File(String.format("%s%s%s", DEVICE_PATH, FORMAT_YEAR.format(new Date()), ".txt"));
                if (!file.exists()) {
                    boolean result = file.createNewFile();
                }
                fos = new FileOutputStream(file, true);
                fos.write(String.format("%s:%s%s", FORMAT_DATE.format(new Date()), log, "\n").getBytes());
                fos.flush();
                fos.close();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (fos != null) {
                    try {
                        fos.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    public static synchronized void writeCrashLog(String log) {
        if (createDirectory()) {
            FileOutputStream fos = null;
            try {
                File file = new File(DEVICE_PATH_CRASH);
                if (!file.exists()) {
                    boolean result = file.createNewFile();
                }
                fos = new FileOutputStream(file, true);
                fos.write(String.format("%s:%s%s", FORMAT_DATE.format(new Date()), log, "\n").getBytes());
                fos.flush();
                fos.close();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (fos != null) {
                    try {
                        fos.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}
