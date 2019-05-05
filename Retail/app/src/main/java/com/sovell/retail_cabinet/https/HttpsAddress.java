package com.sovell.retail_cabinet.https;

import com.sovell.retail_cabinet.BuildConfig;
import com.sovell.retail_cabinet.utils.ConfigUtil;

/**
 * 服务端接口地址类
 */
public class HttpsAddress {

    private static final String PACKAGE_NAME = "updates/" + BuildConfig.APPLICATION_ID;
    /*终端授权(终端配对)*/
    public static final String TERM_PAIRING = "retail/term_pairing";
    /*终端登录*/
    public static final String TERM_SIGN_IN = "retail/term_signin";
    /*终端注销*/
    public static final String TERM_SIGN_OUT = "retail/term_signout";
    /*终端心跳*/
    public static final String TERM_KEEP = "retail/term_keep";
    /*终端状态上报*/
    public static final String TERM_STATUS = "retail/term_status";
    /*终端商品列表*/
    public static final String PROD_LIST = "retail/prod_list_term";
    /*终端商品库存盘点*/
    public static final String STOCK_CHECK = "retail/prod_stock_check";
    /*商品图片获取*/
    public static final String PROD_PICTURE = "retail/prod_picture";
    /*获取卡信息*/
    public static final String CARD_GET = "retail/card_get";
    /*消费记账*/
    public static final String ORDER_TRADE = "retail/order_trade";
    /*撤单*/
    public static final String ORDER_REFUND = "retail/order_refund";
    /*广告屏－取得本终端广告屏策略*/
    public static final String SET_SHOWCASE = "set/showcase";
    /* 查询订单*/
    public static final String ORDER_LIST = "retail/order_list";
    /*取货*/
    public static final String ORDER_TAKE = "retail/order_take";


    /*----------PASS接口----------*/
    /*获取 pass token*/
    public static final String PASS_TOKEN = "http://pass.sovell.com/oauth/v2/token";
    /*查询可用账户*/
    public static final String PASS_ACCOUNT = "http://pass.sovell.com/sovellpay/v2/accounts";
    /*查询交易单*/
    public static final String PASS_CHECK = "http://pass.sovell.com/sovellpay/v2/trade/{seq}?";

    public static final String QUERY_TIMEOUT = "5s";
    /*扫码支付*/
    public static final String PASS_PAY_SCAN = "http://pass.sovell.com/sovellpay/v2/trade";
    /*生成支付码*/
    public static final String PASS_PAY_CODE = "http://pass.sovell.com/sovellpay/v2/trade/prep";
    /*全额退款*/
    public static final String PASS_REFUND = "http://pass.sovell.com/sovellpay/v2/trade/{seq}?";
    /*智盘核对订单*/
    public static final String DISH_CHECK = "order/get";


    public static String getCheckVersionUrl() {
        String baseUrl = ConfigUtil.Instance().getApi("");
        return baseUrl + PACKAGE_NAME + "/update.xml";
    }

    public static String getDownloadUrl(String apkName) {
        String baseUrl = ConfigUtil.Instance().getApi("");
        return baseUrl + PACKAGE_NAME + "/" + apkName;
    }

}
