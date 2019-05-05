package com.sovell.retail_cabinet.utils;

import android.text.TextUtils;

import com.sovell.retail_cabinet.bean.GoodsBean;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import timber.log.Timber;

import static com.sovell.retail_cabinet.utils.ConfigUtil.PASS_EXPIRE;

public class ParamsUtil {

    private static final SimpleDateFormat FORMAT_DATE = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA);

    private static final String SIG = "sig";
    private static final String APP = "dish";
    private static final String SID = "1";
    private static final String V = "2";
    private static final String FORMAT = "json";
    public static final String OPER = "admin";
    private static final String VER = DeviceUtil.versionName();

    public static final String T_0 = "0";
    public static final String T_1 = "1";
    public static final String T_2 = "2";
    public static final String T_11 = "11";
    public static final String T_12 = "12";
    public static final String T_14 = "14";
    public static final String T_21 = "21";
    public static final String T_1000 = "1000";
    public static final String T_2111 = "2111";
    public static final String T_2112 = "2112";
    public static final String T_2211 = "2211";
    public static final String T_2113 = "2113";

    /**
     * 组织接口上传的参数
     *
     * @param value 不同接口 有特定的参数 放在map中 然后再次组合
     * @param key   智盘终端密钥
     */
    public synchronized static Map<String, Object> getParams(Map<String, Object> value, String key) {
        Map<String, Object> params = new HashMap<>();
        params.put("v", V);
        params.put("ver", VER);
        params.put("app", APP);
        params.put("sid", SID);
        params.put("d", FORMAT_DATE.format(new Date()));
        params.put("shop", ConfigUtil.Instance().getString(ConfigUtil.SHOP, "1"));
        params.put("term", ConfigUtil.Instance().getString(ConfigUtil.TERM, "1"));
        params.put("format", FORMAT);
        if (value != null) {
            params.putAll(value);
        }

        List<String> paramList = new ArrayList<>();
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            paramList.add(entry.getKey() + "=" + entry.getValue());
        }

        String paramsStr[] = new String[paramList.size()];
        for (int j = 0; j < paramList.size(); j++) {
            paramsStr[j] = paramList.get(j);
        }

        StringBuilder result = new StringBuilder();
        Arrays.sort(paramsStr, String.CASE_INSENSITIVE_ORDER);
        for (String s : paramsStr) {
            result.append(s);
        }

        if (TextUtils.isEmpty(key)) {
            result.append(ConfigUtil.Instance().getString(ConfigUtil.KEY));
        } else {
            result.append(key);
        }

        Timber.e(result.toString());
        params.put(SIG, DeviceUtil.stringToMD5(result.toString()));
        return params;
    }
    /**
     * 发起PASS支付的参数
     *
     * @param amt           支付金额
     * @param payCode       支付码
     * @param seq           订单号
     * @param payModeEnName 支付类型
     */
    public synchronized static Map<String, Object> passParamMap(String amt, String payCode, String seq, String payModeEnName) {
        String termName = ConfigUtil.Instance().getString(ConfigUtil.TERM_NAME);
        Map<String, Object> paramsMap = new HashMap<>();
        paramsMap.put("title", String.format("%s-%s", "智盘消费", termName));
        int to = new BigDecimal(amt).multiply(new BigDecimal(100)).intValue();
        paramsMap.put("seq", seq);
        paramsMap.put("app", payCode);
        paramsMap.put("intent", "pay " + 1);

        paramsMap.put("expire", PASS_EXPIRE);
        paramsMap.put("sign", passSign(paramsMap));

        return paramsMap;
    }


    /**
     * 生成PASS支付的签名
     *
     * @param paramMap pass支付参数的map
     */
    public synchronized static String passSign(Map<String, Object> paramMap) {
        List<String> paramList = new ArrayList<>();
        for (Map.Entry<String, Object> entry : paramMap.entrySet()) {
            paramList.add(entry.getKey() + "=" + entry.getValue());
        }

        String paramsStr[] = new String[paramList.size()];
        for (int j = 0; j < paramList.size(); j++) {
            paramsStr[j] = paramList.get(j);
        }

        Arrays.sort(paramsStr, String.CASE_INSENSITIVE_ORDER);
        StringBuilder result = new StringBuilder();
        for (String str : paramsStr) {
            result.append(str);
            result.append("&");
        }

        result.append("key=");
        result.append(ConfigUtil.Instance().getString(ConfigUtil.PASS_CLIENT_SECRET));
        Timber.e(result.toString());
        return DeviceUtil.stringToMD5(result.toString());
    }


    /**
     * 将要支付的菜品列表拼接
     *
     * @param bean 菜品
     */
    public synchronized static String getProdsParams(GoodsBean bean) {
        return String.format(Locale.CHINA, "%s,%s,%s,%d,%s,%s,%s", bean.getProdid(), bean.getProdno(), bean.getProdname(),
                bean.getPrice(), bean.getCateid(), bean.getCateno(), bean.getCatename());
    }
    /**
     * 将要支付的菜品列表拼接(适用于净菜柜)
     *
     * @param beanList 菜品列表
     */
    public synchronized static String getProdsParamsForCV(List<GoodsBean> beanList) {

        StringBuilder sb = new StringBuilder();

        for (GoodsBean bean : beanList) {
            for (int i = 0; i < bean.getBuycount(); i++) {
                sb.append(String.format(Locale.CHINA, "%s,%s,%s,%d,%s,%s,%s", bean.getProdid(),
                        bean.getProdno(), bean.getProdname(),
                        bean.getPrice(), bean.getCateid(),
                        bean.getCateno(), bean.getCatename()));
                sb.append("│");
            }
        }

        if (sb.toString().length() > 0 && sb.toString().lastIndexOf("│") == sb.toString().length()-1) {
            return sb.substring(0, sb.toString().length() - 1);
        }

        return sb.toString();
    }
}
