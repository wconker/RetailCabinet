package com.sovell.retail_cabinet.utils;

//统一获取预订和取货时间
public class BusinessHelpUtil {

    /*
    判断是否到达预订时间
     */
    public static boolean bookTime() {
        return FormatUtil.isEffectiveDate(FormatUtil.getHourStr(),
                ConfigUtil.Instance().getString(ConfigUtil.CV_BOOKING_START_TIME),
                ConfigUtil.Instance().getString(ConfigUtil.CV_END_OF_RESERVATION_TIME));

    }

    /*
       判断是否到取货时间
        */
    public static boolean tickTime() {

        return FormatUtil.isEffectiveDate(FormatUtil.getHourStr(),
                ConfigUtil.Instance().getString(ConfigUtil.CV_MEAL_PICKUP_BEGINS),
                ConfigUtil.Instance().getString(ConfigUtil.CV_END_OF_MEAL_PICKUP));
    }

    /*
      获取预订时间段
       */
    public static String getBookingTime() {

        return ConfigUtil.Instance().getString(ConfigUtil.CV_BOOKING_START_TIME) +
                "-" + ConfigUtil.Instance().getString(ConfigUtil.CV_END_OF_RESERVATION_TIME);
    }

    /*
       获取取货时间段
        */
    public static String getTickTime() {

        return ConfigUtil.Instance().getString(ConfigUtil.CV_MEAL_PICKUP_BEGINS) +
                "-" + ConfigUtil.Instance().getString(ConfigUtil.CV_END_OF_MEAL_PICKUP);
    }
}
