package com.sovell.retail_cabinet.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import com.sovell.retail_cabinet.app.RetailCabinetApp;

public class ConfigUtil {

    private static final String SERVICE_CONFIG = "retail_cabinet";
    private SharedPreferences mPref;

    private static class ConfigUtilHolder {
        private static final ConfigUtil INSTANCE = new ConfigUtil();
    }

    private ConfigUtil() {
        mPref = RetailCabinetApp.Instance().getSharedPreferences(SERVICE_CONFIG, Context.MODE_PRIVATE);
    }

    public static ConfigUtil Instance() {
        return ConfigUtilHolder.INSTANCE;
    }

    /*进入设置的密码*/
    public static final String ENTER_SET_PWD = "85330909";

    /*默认的API地址*/
    public static final String DEF_API = "";

    /*(Boolean)API地址*/
    public static final String BIND = "is_bind";
    /*(Boolean)货柜初始化*/
    public static final String CABINET_INIT = "cabinet_init";
    /*(Boolean)是否开启守护*/
    public static final String PROTECT = "protect";
    /*(String)API地址*/
    public static final String API = "api_address";

    /*(String)密钥*/
    public static final String KEY = "dish_key";
    /*(String)餐厅编号*/
    public static final String SHOP = "shop";
    /*(String)终端编号*/
    public static final String TERM = "term";
    /*(String)终端名称*/
    public static final String TERM_NAME = "term_name";
    /*(String)会话id*/
    public static final String AUTH_KEY = "auth_key";
    /*(Integer)终端心跳时间间隔*/
    public static final String KEEP_INTERVAL = "keep_interval";
    /*(String)客服电话*/
    public static final String PHONE = "phone";
    /*(Integer)制冷最大温度*/
    public static final String COLD_MAX = "cold_max";
    /*(Integer)制冷最小温度*/
    public static final String COLD_MIN = "cold_min";
    /*(Integer)退出倒计时下标*/
    public static final String EXIT_TIME = "exit_time";
    /*(Integer)主界面广告间隔*/
    public static final String AD_INTERVAL = "ad_interval";





    /**
     * 净菜柜设置
     */
    /*(String)预订开始时间*/
    public static final String CV_BOOKING_START_TIME = "cv_booking_start_time";
    /*(String)预订结束时间*/
    public static final String CV_END_OF_RESERVATION_TIME = "cv_end_of_reservation_time";
    /*(String)取餐开始*/
    public static final String CV_MEAL_PICKUP_BEGINS = "cv_meal_pickup_begins";
    /*(String)取餐结束*/
    public static final String CV_END_OF_MEAL_PICKUP = "cv_end_of_meal_pickup";
    /*(String)取餐状态*/
    public static final String CV_PICKUP_STATUS = "cv_pickup_status";
    /*(String)当前时间*/
    public static final String CV_CurrentTime = "cv_current_time";
    /*(Integer)售货柜类型*/
    public static final String CV_TYPE = "cv_type";

    //净菜柜类别
    public static final int CVDISK = 42;

    //零售柜类别
    public static final int CETAILDISK = 41;

    ///*(Integer)制冷状态*/(0 关闭 1打开),只记录本地操作
    public static final String FROZEN = "frozen";



     /*
    pass支付
     */

    /*(String)终端配置的pass的clientId、clientKey，用于pass支付*/
    public static final String PASS_CLIENT_ID = "pass_client_id";
    /*PASS检查订单最长时间*/
    public static final int PASS_MAX_TIME = 60000;
    public static final String PASS_CLIENT_SECRET = "pass_client_key";
    /*(String)Pass的token*/
    public static final String PASS_TOKEN = "pass_token";
    /*(long)获取Pass的token时间*/
    public static final String PASS_TOKEN_TIME = "pass_token_time";
    /*过期时间，由于各交易通道对过期的定义的差异，该时间定义为最大过期时间
      ，实际过期时间会存在差异，没有单位时默认毫秒，格式为xxxs，默认300s，最大600s
      */
    public static final String PASS_EXPIRE = "50s";


    /**
     * 判断是否需要保存token
     * 当前时间戳加上超时时间再减去一个缓冲时间(避免刚刚过期用户就发起请求)
     *
     * @param passExpires token的有效时长
     * @param token       pass的token
     */
    public void savePassToken(long passExpires, String token) {
        if (passExpires > 0 && !TextUtils.isEmpty(token)) {
            saveString(ConfigUtil.PASS_TOKEN, token);
            long expires = System.currentTimeMillis() + passExpires * 1000 - 60000;
            saveLong(ConfigUtil.PASS_TOKEN_TIME, expires);
        }
    }


    public void saveString(String key, String value) {
        synchronized (ConfigUtil.class) {
            SharedPreferences.Editor editor = mPref.edit();
            editor.putString(key, value);
            editor.commit();
        }
    }

    public String getString(String key) {
        synchronized (ConfigUtil.class) {
            if (TextUtils.equals(key, PHONE)) {
                return mPref.getString(key, "400-617-9959");
            }
            return mPref.getString(key, "");
        }
    }

    public String getString(String key, String def) {
        synchronized (ConfigUtil.class) {
            return mPref.getString(key, def);
        }
    }

    public void saveInteger(String key, int value) {
        synchronized (ConfigUtil.class) {
            SharedPreferences.Editor editor = mPref.edit();
            if (TextUtils.equals(key, AD_INTERVAL) && value == 0) {
                value = 5;
            }
            editor.putInt(key, value);
            editor.commit();
        }
    }

    public int getInteger(String key) {
        synchronized (ConfigUtil.class) {
            if (TextUtils.equals(key, KEEP_INTERVAL)) {
                return mPref.getInt(key, 10);
            } else if (TextUtils.equals(key, AD_INTERVAL)) {
                return mPref.getInt(key, 5);
            } else if (TextUtils.equals(key, COLD_MAX)) {
                return mPref.getInt(key, 18);
            } else if (TextUtils.equals(key, COLD_MIN)) {
                return mPref.getInt(key, 12);
            }
            return mPref.getInt(key, 0);
        }
    }

    public void saveBoolean(String key, boolean value) {
        synchronized (ConfigUtil.class) {
            SharedPreferences.Editor editor = mPref.edit();
            editor.putBoolean(key, value);
            editor.commit();
        }
    }

    public boolean getBoolean(String key) {
        synchronized (ConfigUtil.class) {
            return mPref.getBoolean(key, false);
        }
    }

    public void saveLong(String key, Long value) {
        synchronized (ConfigUtil.class) {
            SharedPreferences.Editor editor = mPref.edit();
            editor.putLong(key, value);
            editor.commit();
        }
    }

    public long getLong(String key) {
        synchronized (ConfigUtil.class) {
            return mPref.getLong(key, 0);
        }
    }

    public String getApi(String url) {
        synchronized (ConfigUtil.class) {
            if (TextUtils.isEmpty(url)) {
                url = mPref.getString(API, DEF_API);
                if (TextUtils.isEmpty(url)) {
                    url = DEF_API;
                }
            }
            if (!url.endsWith("/")) {
                url += "/";
            }
            return url;
        }
    }

    public int getExitSecond(int index) {
        synchronized (ConfigUtil.class) {
            if (index == 0) {
                return 90;
            } else if (index == 1) {
                return 60;
            } else {
                return 30;
            }
        }
    }

    public int getShopType(int index) {
        synchronized (ConfigUtil.class) {
            if (index == 0) {
                return 41;
            } else {
                return 42;
            }
        }
    }

    public void clear() {
        synchronized (ConfigUtil.class) {
            SharedPreferences.Editor editor = mPref.edit();
            editor.putString(TERM_NAME, null);
            editor.putString(PHONE, null);
            editor.commit();
        }
    }

    /**
     * 订单号 =  终端编号+ 当前时间戳
     *
     * @return
     */
    public synchronized static String getInvoice() {
        return ConfigUtil.Instance().getString(ConfigUtil.TERM) + String.valueOf(System.currentTimeMillis());
    }

}
