package com.sovell.retail_cabinet.utils;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * 格式工具类
 */
public class FormatUtil {

    private static SimpleDateFormat mFormatDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA);
    private static SimpleDateFormat FORMAT_HOUR = new SimpleDateFormat("HH:mm", Locale.CHINA);

    /**
     * mTimeDif = 服务器时间 - 本机时间
     */
    private static long mTimeDif = 0;

    /*
     * 将时间转换为时间戳
     */
    public static long dateToStamp(String s) {
        try {
            Date date = mFormatDate.parse(s);
            return date.getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return 0;
    }


    /**
     * 四舍五入
     *
     * @param value 保留几位小数
     */
    public static String doubleFormat(String value) {
        BigDecimal bigDecimal = new BigDecimal(verifyNum(value)).setScale(2, BigDecimal.ROUND_HALF_UP);
        String format = "%.2f";
        return String.format(Locale.CHINA, format, bigDecimal.doubleValue());
    }

    /**
     * 精确的加法运算，保留2位小数
     */
    public static String add(String value1, String value2) {
        BigDecimal b1 = new BigDecimal(verifyNum(value1));
        BigDecimal b2 = new BigDecimal(verifyNum(value2));
        return b1.add(b2).setScale(2, BigDecimal.ROUND_HALF_UP).toString();
    }

    /**
     * 精确的减法运算，保留2位小数
     */
    public static String sub(String value1, String value2) {
        BigDecimal b1 = new BigDecimal(verifyNum(value1));
        BigDecimal b2 = new BigDecimal(verifyNum(value2));
        return b1.subtract(b2).setScale(2, BigDecimal.ROUND_HALF_UP).toString();
    }

    /**
     * 精确的乘法运算，保留2位小数
     */
    public static String mul(String value1, String value2) {
        BigDecimal b1 = new BigDecimal(verifyNum(value1));
        BigDecimal b2 = new BigDecimal(verifyNum(value2));
        return b1.multiply(b2).setScale(2, BigDecimal.ROUND_HALF_UP).toString();
    }

    /**
     * 精确的除法运算，当发生除不尽的情况时，精确到
     * 小数点以后2位，以后的数字四舍五入。
     */
    public static String div(String value1, String value2) {
        if (Double.valueOf(value2) == 0) {
            return "0.00";
        }
        BigDecimal b1 = new BigDecimal(verifyNum(value1));
        BigDecimal b2 = new BigDecimal(verifyNum(value2));
        return b1.divide(b2, 2, BigDecimal.ROUND_HALF_UP).toString();
    }

    private static String verifyNum(String value) {
        if (!CommonsUtil.isNumber(value)) {
            value = "0.00";
        }
        return value;
    }
    
    /**
     * 获取当前时间的字符串
     * 将本机时间 + 时间差，转换成对应格式字符串
     */
    public static String getDateStr() {
        long time = new Date().getTime() + mTimeDif;
        Date mCurDate = new Date(time);
        return mFormatDate.format(mCurDate);
    }

    /**
     * 计算服务器时间与本机时间差，并存放在临时变量
     *
     * @param serviceTime 服务器时间，格式 yyyy-MM-dd HH:mm:ss
     */
    public static long setDifTime(String serviceTime) {
        long serviceLong = 0;
        try {
            serviceLong = dateToStamp(serviceTime);
            mTimeDif = serviceLong - new Date().getTime();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return mTimeDif;
    }

    /**
     * 获取当前时间的字符串
     * 将本机时间 + 时间差，转换成对应格式字符串
     */
    public static String getHourStr() {
        long time = new Date().getTime() + mTimeDif;
        Date mCurDate = new Date(time);
        return FORMAT_HOUR.format(mCurDate);
    }

    /**
     * 将时间转换为通用概念
     */
    public static String formatDay(String time) {

        SimpleDateFormat Gmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);
        long timeStamp = 0;
        try {
            timeStamp = Gmt.parse(time).getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        long curTimeMillis = System.currentTimeMillis();
        Date curDate = new Date(curTimeMillis);
        int todayHoursSeconds = curDate.getHours() * 60 * 60;
        int todayMinutesSeconds = curDate.getMinutes() * 60;
        int todaySeconds = curDate.getSeconds();
        int todayMillis = (todayHoursSeconds + todayMinutesSeconds + todaySeconds) * 1000;
        long todayStartMillis = curTimeMillis - todayMillis;
        if (timeStamp >= todayStartMillis) {
            return "今天";
        }
        int oneDayMillis = 24 * 60 * 60 * 1000;
        long yesterdayStartMilis = todayStartMillis - oneDayMillis;
        if (timeStamp >= yesterdayStartMilis) {
            return "昨天";
        }
        long yesterdayBeforeStartMilis = yesterdayStartMilis - oneDayMillis;
        if (timeStamp >= yesterdayBeforeStartMilis) {
            return "前天";
        }
//        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        return sdf.format(new Date(timeStamp));
    }

    public static String formatTime(String time) {

        SimpleDateFormat Gmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);
        long timeStamp = 0;
        try {
            timeStamp = Gmt.parse(time).getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
        return sdf.format(new Date(timeStamp));
    }

    /**
     * 判断当前时间是否在规定时间内
     *
     * @param nowTime   当前时间
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return
     */
    public static boolean isEffectiveDate(String nowTime, String startTime, String endTime) {

        if (nowTime.isEmpty() || startTime.isEmpty() || endTime.isEmpty()) {
            return false;
        }

        String format = "HH:mm";
        try {
            Date var1 = new SimpleDateFormat(format).parse(nowTime);
            Date var2 = new SimpleDateFormat(format).parse(startTime);
            Date var3 = new SimpleDateFormat(format).parse(endTime);

            if (var1.getTime() == var2.getTime()
                    || var1.getTime() == var3.getTime()) {
                return true;
            }

            Calendar date = Calendar.getInstance();
            date.setTime(var1);

            Calendar begin = Calendar.getInstance();
            begin.setTime(var2);

            Calendar end = Calendar.getInstance();
            end.setTime(var3);

            return date.after(begin) && date.before(end);

        } catch (ParseException e) {

            return false;

        }
    }


    public static String getToday() {
        //得到long类型当前时间

        long l = System.currentTimeMillis();

        Date date = new Date(l);

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

        return dateFormat.format(date);
    }

}
