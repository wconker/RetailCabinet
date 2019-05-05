package com.sovell.retail_cabinet.presenter.contract;

import com.sovell.retail_cabinet.bean.PayResultBean;
import com.sovell.retail_cabinet.manager.PayModeEnum;

public interface PayContract {

    /**
     * 获取卡信息成功,可以正常支付
     */
    void onGetCardSuccess();

    /**
     * 获取卡信息失败或卡已失效
     */
    void onGetCardFailed(int code, String msg);

    /**
     * 支付成功,更新ui
     */
    void onPaySuccess();

    /**
     * 空货道,清空货道,并更新首页ui
     */
    void onEmptyCargo();

    /**
     * 商品出货成功
     */
    void shipmentSuccess(PayResultBean payResultBean);

    /**
     * 交易失败
     */
    void onTradeFailed(int code, String msg, int amt, boolean isPay);

    /**
     * pass 支付调用成功
     */
    void onSweepCodeSuccess(String status, String msg, String payMode);

    void onPassPaySuccess(String invoices, String amt, PayModeEnum payCode);

//    /**
//     * pass 检查订单状态
//     */
//    void onCheckOrderStatus(String status, String msg);


}
