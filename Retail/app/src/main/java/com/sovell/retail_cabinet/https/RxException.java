package com.sovell.retail_cabinet.https;

/**
 * 与服务端协议的响应码
 */

public class RxException extends Exception {

    /*加载失败*/
    public static final int NET_ERROR = -1;
    /*网络断开*/
    public static final int NET_BREAK = -2;
    /*网络超时*/
    public static final int NET_TIMEOUT = -3;
    /*接口类型错误*/
    public static final int PORT_ERROR = -4;
    /*api地址错误*/
    public static final int API_ERROR = -5;

    /*卡重复支付*/
    public static final int PAY_REPEAT = 42;
    /*撤单时订单不存在或者异常(可以认为撤单成功)*/
    public static final int ORDER_NONENTITY = 4;


    /*pass支付失败，并需要将订单保存到退款队列*/
    public static final int PASS_ERROR_REFUND = -6;
    /*PASS重复订单号支付，http code == 406，表示此订单已经支付*/
    public static final int PASS_HAVE_PAID = 406;
    /*PASS查不到订单号*/
    public static final int PASS_NO_ORDER = 404;

    private static String message;
    private static int errorCode;

    public RxException(int code, String errorMsg) {
        super(errorMsg);
        message = errorMsg;
        errorCode = code;
    }

    public static boolean isSuccess(int code) {
        return (code == 1);
    }

    @Override
    public String getMessage() {
        return message;
    }

    public int getErrorCode() {
        return errorCode;
    }

    public static String getErrorMsg(int code, int subCode) {
        switch (code) {
            case 2:
                message = "其他失败";
                break;
            case 3:
                if (subCode == 6) {
                    message = "重复请求";
                } else if (subCode == 4) { //智盘查单结果
                    message = "订单不存在";
                }else if (subCode == 9) {
                    message = "消费金额异常";
                } else {
                    message = "参数不合法";
                }
                break;
            case 4:
                if (subCode == 18) {
                    message = "库存不足";
                } else {
                    message = "配对码有误"; //2019、4、17 修改提示
                }
                break;

            case 6:
                if (subCode == 5) {
                    message = "未到预订开始时间";
                } else if (subCode == 6) {
                    message = "已过预订截止时间";
                } else if (subCode == 2) {
                    message = "预订已中止";
                } else if (subCode == 11) {
                    message = "预订已结束";
                }

                break;

            case 8:
                message="终端未授权";
                break;
            case 10:
                if (subCode == 15) {
                    message = "卡类型不支持";
                } else {
                    message = "无效卡";
                }
                break;
            case 12:
                message = "卡已过期";
                break;
            case 13:
                message = "卡被锁定";
                break;
            case 14:
                message = "余额不足";
                break;
            case 31:
                switch (subCode) {
                    case 1:
                        message = "会话异常";
                        break;
                    case 2:
                        message = "心跳异常";
                        break;
                    case 10:
                    case 13:
                        message = "终端被禁用";
                        break;
                    case 11:
                    case 12:
                        message = "餐厅被禁用";
                        break;
                    default:
                        message = "code:" + code + ",subCode:" + subCode;
                        break;
                }
                break;
            case 42:
                message = "重复支付";
                break;
            case NET_ERROR:
                message = "通讯异常";
                break;
            default:
                message = "获取失败(code:" + code + ",subCode:" + subCode + ")";
                break;
        }
        return message;
    }
}
