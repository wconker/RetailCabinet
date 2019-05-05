package com.sovell.retail_cabinet.presenter.impl;

import android.annotation.SuppressLint;
import android.os.RemoteException;
import android.text.TextUtils;
import android.util.Log;

import com.sovell.retail_cabinet.base.PassPayBean;
import com.sovell.retail_cabinet.bean.CardInfoBean;
import com.sovell.retail_cabinet.bean.GoodsBean;
import com.sovell.retail_cabinet.bean.OrderBean;
import com.sovell.retail_cabinet.bean.PayLogBean;
import com.sovell.retail_cabinet.bean.PayResultBean;
import com.sovell.retail_cabinet.bean.RefundBean;
import com.sovell.retail_cabinet.bean.ShipmentBean;
import com.sovell.retail_cabinet.bean.TermSignIn;
import com.sovell.retail_cabinet.https.RxException;
import com.sovell.retail_cabinet.https.RxProgress;
import com.sovell.retail_cabinet.manager.ApiManager;
import com.sovell.retail_cabinet.manager.BVMManager;
import com.sovell.retail_cabinet.manager.DBManager;
import com.sovell.retail_cabinet.manager.PassStatusEnum;
import com.sovell.retail_cabinet.manager.PayModeEnum;
import com.sovell.retail_cabinet.presenter.contract.PayContract;
import com.sovell.retail_cabinet.utils.FormatUtil;
import com.sovell.retail_cabinet.utils.JsonUtils;
import com.sovell.retail_cabinet.utils.PayLogUtil;

import java.util.List;
import java.util.Locale;
import java.util.Map;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.Scheduler;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

import static com.sovell.retail_cabinet.utils.ConfigUtil.PASS_MAX_TIME;

public class PayPresenterImpl {

    public static final int ASK_PAY_ERROR = 10001;//支付接口请求失败
    public static final int REFUND_SUCCESS = 10002;//退款成功
    public static final int REFUND_ERROR = 10003;//退款失败


    public static final int SHIPMENT_SUCCESS = 10010;//出货成功
    public static final int SHIPMENT_ERROR = 10011;//出货失败
    public static final int STOCK_ERROR = 10012;//本地商品库存不足
    public static final int SHIPMENT_UNUSUAL = 10013;//商品出货失败,机器异常

    /*PASS退款计数*/
    private int mPassRefundCount = 0;
    /*检查pass订单状态的开始时间*/
    private volatile long mPassCheckStart;
    /*PASS退款调用次数上限*/
    private static final int PASS_REFUND_MAX_COUNT = 5;

    private static final String PAY_ERROR = "支付失败";
    private PayContract payContract;
    private CompositeDisposable disposable;
    private PayResultBean mPayResultBean;
    private Disposable passCheckDisposable;

    public PayPresenterImpl(PayContract payContract) {
        this.payContract = payContract;
        this.disposable = new CompositeDisposable();
        mPayResultBean = new PayResultBean();
    }


    //----------支付宝或微信扫码支付调用pass接口部分=-----------------------------------------

    /**
     * 智盘查单
     * 开单前遍历pass异常订单,智盘后端存在则删除该单,
     * 不存在则加入到退款订单中
     */
    private void dishCheck() {
        List<PayLogBean> passList = PayLogUtil.readPayLog(PayLogUtil.PASS_UNUSUAL, PayLogBean.class);
        Observable.fromIterable(passList)
                .observeOn(Schedulers.io())
                .flatMap(new Function<PayLogBean, ObservableSource<OrderBean>>() {
                    @Override
                    public ObservableSource<OrderBean> apply(@NonNull final PayLogBean payLogBean) throws Exception {
                        return ApiManager.order_get(payLogBean.getInvoice()).doOnNext(new Consumer<OrderBean>() {
                            @Override
                            public void accept(OrderBean orderBean) throws Exception {
                                //删除本地异常,该加入退款的加入退款,没有的就不操作
                                unusualPassInvoice(false, payLogBean.getInvoice(), "", "");
                                if (!RxException.isSuccess(orderBean.getCode())) {
                                    //没有结果把订单加入到退单文件中
                                    refundPassInvoice(true, payLogBean.getInvoice(), "", "");
                                }
                            }
                        });
                    }
                }).subscribe(new RxProgress<OrderBean>() {
            @Override
            protected void onOverSubscribe(Disposable d) {

            }

            @Override
            protected void onOverNext(OrderBean payLogBean) {

            }

            @Override
            protected void onOverError(int code, String msg) {

            }
        });
    }

    /**
     * 发起pass接口支付，返回一个支付宝微信的二维码
     * 用户扫描支付宝微信的二维码以后调用passcheck检查回调结果。
     *
     * @param amt      支付的金额（分为单位）
     * @param payMode  支付类型（支付宝或者微信）
     * @param invoices 流水号（终端编号+当前时间戳的格式）
     */
    public void passPay(final String amt, final String invoices, final String payMode) {
        dishCheck();
        //支付前先保存到pass异常中,
        unusualPassInvoice(true, invoices, amt, payMode);
        //清除上次的订单check,本地文件会保留之前的订单,统一退单
        if (passCheckDisposable != null) {
            passCheckDisposable.dispose();
            passCheckDisposable = null;
        }
        //保存订单后发起支付
        ApiManager.passPayQRCode(amt, invoices, payMode).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new RxProgress<PassPayBean>() {
                    @Override
                    protected void onOverSubscribe(Disposable d) {

                    }

                    @Override
                    protected void onOverNext(PassPayBean passPayBean) {
                        //记录当前时间,为后续轮询超时时间做标准
                        mPassCheckStart = System.currentTimeMillis();
                        passCheck(invoices, amt);
                        payContract.onSweepCodeSuccess(passPayBean.getStatus(), passPayBean.getNotify_uri(), payMode);
                    }

                    @Override
                    protected void onOverError(int code, String msg) {
                        if (code == RxException.PASS_HAVE_PAID) {
                            mPassCheckStart = System.currentTimeMillis();
                        }
                    }
                });

    }


    /**
     * 轮询检查订单的状态.根据订单后续状态执行相对于的操作
     *
     * @param invoices 流水号
     * @param amt      支付金额(单位分)
     */
    private void passCheck(final String invoices, final String amt) {
        ApiManager.passCheck(invoices)
                .subscribe(new RxProgress<PassPayBean>() {
                    @Override
                    protected void onOverSubscribe(Disposable d) {
                        passCheckDisposable = d;
                    }

                    @Override
                    protected void onOverNext(PassPayBean passPayBean) {
                        if (passPayBean.getStatus().equals(PassStatusEnum.NORMAL.getEnName())) { //成功
                            PayModeEnum payModeEnum = PayModeEnum.WEI_CHAT;
                            if (TextUtils.equals(passPayBean.getAccount_id(), "alipay")) {
                                payModeEnum = PayModeEnum.ALI_PAY;
                            }
                            payContract.onPassPaySuccess(invoices, amt, payModeEnum);
                        } else if (passPayBean.getStatus().equals(PassStatusEnum.ACCEPTED.getEnName())) {
                            long now = System.currentTimeMillis();
                            if (now - mPassCheckStart > PASS_MAX_TIME) { //超时
                                mPassRefundCount = 0;
                                passRefund(invoices, amt);
                                Log.e("passPay", "超时,关闭" + invoices);
                            } else {
                                Log.e("passPay", "未超时,继续检查" + invoices);
                                //未超时
                                passCheck(invoices, amt);
                            }
                        } else if (passPayBean.getStatus().equals(PassStatusEnum.CLOSE.getEnName())) {
                            //已经确定是关闭订单,用户无法支付,删除本地异常
                            unusualPassInvoice(false, invoices, amt, "");
                            Log.e("passPay", "该单关闭了");
                        } else {
                            Log.e("passPay", "其他异常" + invoices);
                            mPassRefundCount = 0;
                            passRefund(invoices, amt);
                        }
                    }

                    @Override
                    protected void onOverError(int code, String msg) {
                        long now = System.currentTimeMillis();
                        if (now - mPassCheckStart > PASS_MAX_TIME) {
                            mPassRefundCount = 0;
                            passRefund(invoices, amt);
                            Log.e("passPay", "订单超时");
                        } else {
                            Log.e("passPay", "未超时,继续检查" + invoices);
                            passCheck(invoices, amt);
                        }
                    }
                });
    }


    /***
     * 退款方法,需要设置最大调用次数并判断,
     * 如果超过最大调用次数,
     * 则保存到本地txt文件中另作他用
     * @param invoices 流水号
     * @param amt 金额(分为单位)
     */
    private void passRefund(final String invoices, final String amt) {
        ApiManager.passRefund(invoices)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new RxProgress<PassPayBean>() {
                    @Override
                    protected void onOverSubscribe(Disposable d) {
                        disposable.add(d);
                    }

                    @Override
                    protected void onOverNext(PassPayBean passPayBean) {
                        mPassRefundCount++;
                        if (passPayBean.getStatus().equals(PassStatusEnum.CLOSE.getEnName())) {
                            //退款成功,把本地的异常订单删除
                            unusualPassInvoice(false, invoices, amt, "");
                            //退款成功,删除pass中的退款订单
                            refundPassInvoice(false, invoices, amt, "");
                            Log.e("passPay", "退款成功");
                        } else if (mPassRefundCount > PASS_REFUND_MAX_COUNT) {
                            Log.e("passPay", "退款失败,超过最大退款次数");
                            //需要退款了,先写入退款文件
                            refundPassInvoice(true, invoices, amt, "");
                        } else {
                            passRefund(invoices, amt);
                            Log.e("passPay", "未超过最大退款次数,继续退款1");
                        }
                    }

                    @Override
                    protected void onOverError(int code, String msg) {
                        mPassRefundCount++;
                        if (mPassRefundCount > PASS_REFUND_MAX_COUNT) {
                            Log.e("passPay", "退款失败,支付失败2");
                            //需要退款了,先写入退款文件
                            refundPassInvoice(true, invoices, amt, "");
                        } else {
                            passRefund(invoices, amt);
                            Log.e("passPay", "未超过最大退款次数,继续退款2");
                        }
                    }
                });
    }

    //------------------------------------智盘入账&卡支付--------------------------------------------

    /**
     * 获取卡信息
     */
    public void cardGet(final String invoice, final int amt, final String prods, final String payCode, final GoodsBean mGoodsBean) {
        ApiManager.cardGet(payCode)
                .subscribe(new RxProgress<CardInfoBean>() {
                    @Override
                    protected void onOverSubscribe(Disposable d) {
                        disposable.add(d);
                    }

                    @Override
                    protected void onOverNext(CardInfoBean cardInfoBean) {
                        if (RxException.isSuccess(cardInfoBean.getCode())) {
                            String errorMsg;
                            long expireTime = FormatUtil.dateToStamp(cardInfoBean.getCard().getExpire_date());
                            long nowTime = System.currentTimeMillis();
                            if (cardInfoBean.getCard().getLocked() == 1) {
                                errorMsg = "卡已锁定";
                            } else if (expireTime - nowTime <= 0) {
                                errorMsg = "卡已过期";
                            } else if (cardInfoBean.getCard().getState() == 0) {
                                errorMsg = "卡未激活";
                            } else if (cardInfoBean.getCard().getState() == 2) {
                                errorMsg = "效验失败";
                            } else if (cardInfoBean.getCard().getState() == 3) {
                                errorMsg = "卡已挂失";
                            } else if (cardInfoBean.getCard().getState() == 4) {
                                errorMsg = "卡已作废";
                            } else {
                                errorMsg = "正常";
                            }
                            if (TextUtils.equals(errorMsg, "正常")) {
                                payContract.onGetCardSuccess();
                                orderTrade(invoice, amt, prods, payCode, mGoodsBean);
                            } else {
                                payContract.onGetCardFailed(cardInfoBean.getCode(), errorMsg);
                            }

                        } else {
                            payContract.onGetCardFailed(cardInfoBean.getCode(), RxException.getErrorMsg(cardInfoBean.getCode(), cardInfoBean.getSub_code()));
                        }
                    }

                    @Override
                    protected void onOverError(int code, String msg) {
                        payContract.onGetCardFailed(code, msg);
                    }
                });
    }


    /**
     * 获取卡信息
     */
    public void cardGetForCV(final String invoice, final int amt, final String prods, final String payCode) {
        ApiManager.cardGet(payCode)
                .subscribe(new RxProgress<CardInfoBean>() {
                    @Override
                    protected void onOverSubscribe(Disposable d) {
                        disposable.add(d);
                    }

                    @Override
                    protected void onOverNext(CardInfoBean cardInfoBean) {
                        if (RxException.isSuccess(cardInfoBean.getCode())) {
                            String errorMsg;
                            long expireTime = FormatUtil.dateToStamp(cardInfoBean.getCard().getExpire_date());
                            long nowTime = System.currentTimeMillis();
                            if (cardInfoBean.getCard().getLocked() == 1) {
                                errorMsg = "卡已锁定";
                            } else if (expireTime - nowTime <= 0) {
                                errorMsg = "卡已过期";
                            } else if (cardInfoBean.getCard().getState() == 0) {
                                errorMsg = "卡未激活";
                            } else if (cardInfoBean.getCard().getState() == 2) {
                                errorMsg = "效验失败";
                            } else if (cardInfoBean.getCard().getState() == 3) {
                                errorMsg = "卡已挂失";
                            } else if (cardInfoBean.getCard().getState() == 4) {
                                errorMsg = "卡已作废";
                            } else {
                                errorMsg = "正常";
                            }
                            if (TextUtils.equals(errorMsg, "正常")) {
                                payContract.onGetCardSuccess();
                                orderTradeForCV(invoice, amt, prods, payCode);
                            } else {
                                payContract.onGetCardFailed(cardInfoBean.getCode(), errorMsg);
                            }

                        } else {
                            payContract.onGetCardFailed(cardInfoBean.getCode(), RxException.getErrorMsg(cardInfoBean.getCode(), cardInfoBean.getSub_code()));
                        }
                    }

                    @Override
                    protected void onOverError(int code, String msg) {
                        payContract.onGetCardFailed(code, msg);
                    }
                });
    }

    //------------------------------------卡读取操作--------------------------------------------

    /**
     * 消费支付
     *
     * @param invoice 终端订单号
     * @param amt     实际消费金额，分为单位
     * @param prods   商品列表
     * @param payCode 支付码-卡付时传入物理卡号
     */
    public void orderTrade(final String invoice, final int amt, final String prods, final String payCode, final GoodsBean mGoodsBean) {
        Observable.just("")
                .observeOn(Schedulers.io())
                .flatMap(new Function<String, ObservableSource<PayResultBean>>() {
                    @Override
                    public ObservableSource<PayResultBean> apply(String s) throws Exception {
                        //发起支付
                        BVMManager.faultClean();
                        return ApiManager.orderTrade(invoice, amt, prods, payCode, 1)
                                .doOnError(new Consumer<Throwable>() {
                                    @Override
                                    public void accept(Throwable throwable) throws Exception {
                                        PayLogBean payLogBean = new PayLogBean();
                                        payLogBean.setInvoice(invoice);
                                        payLogBean.setMsg("支付异常");
                                        PayLogUtil.saveLog(PayLogUtil.REFUND, payLogBean, PayLogBean.class);
                                    }
                                });
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .flatMap(new Function<PayResultBean, ObservableSource<String>>() {
                    @Override
                    public ObservableSource<String> apply(PayResultBean payResultBean) throws Exception {
                        //支付结果
                        if (RxException.isSuccess(payResultBean.getCode()) || payResultBean.getCode() == RxException.PAY_REPEAT) {
                            mPayResultBean = payResultBean;
                            //通知首页更新ui
                            payContract.onPaySuccess();
                            return Observable.just(payResultBean.getSeq());
                        } else if (payResultBean.getCode() == 3 && payResultBean.getSub_code() == 6) {
                            //需要进行撤单
                            PayLogBean payLogBean = new PayLogBean();
                            payLogBean.setInvoice(invoice);
                            payLogBean.setMsg("支付异常");
                            payLogBean.setSeq(payResultBean.getSeq());
                            PayLogUtil.saveLog(PayLogUtil.REFUND, payLogBean, PayLogBean.class);
                            return Observable.error(new RxException(ASK_PAY_ERROR, RxException.getErrorMsg(payResultBean.getCode(), payResultBean.getSub_code())));
                        } else {
                            //提示操作失败(支付未成功)
                            return Observable.error(new RxException(ASK_PAY_ERROR, RxException.getErrorMsg(payResultBean.getCode(), payResultBean.getSub_code())));
                        }
                    }
                })
                .observeOn(Schedulers.io())
                .flatMap(new Function<String, ObservableSource<ShipmentBean>>() {
                    @Override
                    public ObservableSource<ShipmentBean> apply(String seq) throws Exception {
                        ShipmentBean shipmentBean = new ShipmentBean();
                        shipmentBean.setSeq(seq);
                        try {
                            List<GoodsBean> goodsBeanList = DBManager.findSameById(mGoodsBean.getProdid());
                            if (goodsBeanList != null && goodsBeanList.size() != 0) {
                                GoodsBean goodsBean = goodsBeanList.get(0);
                                String param = BVMManager.takeGoodsParam(goodsBean.getRow(), goodsBean.getColumn(), goodsBean.getPrice(), null);
                                String response = BVMManager.takeGoods(param);
                                Map<String, Object> responseMap = JsonUtils.convertJsonToObject(response);
                                Log.e("responseMap", responseMap.toString());
                                int machineCode = Integer.parseInt(String.valueOf(responseMap.get("shipresult")));
                                if (machineCode == 0) {
                                    goodsBean.setStock(goodsBean.getStock() - 1);
                                    DBManager.updateById(goodsBean);
                                    mGoodsBean.setStock(mGoodsBean.getStock() - 1);
                                    shipmentBean.setCode(SHIPMENT_SUCCESS);
                                    shipmentBean.setMachineCode(machineCode);
                                    shipmentBean.setErrorMessage("出货成功");
                                    return Observable.just(shipmentBean);
                                } else {
                                    int[] temp = BVMManager.currentTemp();
                                    String errorMsg = BVMManager.errorMsg(machineCode);
                                    ApiManager.termStatus(temp[0], String.valueOf(machineCode), errorMsg);
                                    if (machineCode == -1203) {
                                        //空货道,需要清空库存并向服务器上报信息
                                        Log.e("空货道", "空货道错误");
                                        mGoodsBean.setStock(mGoodsBean.getStock() - goodsBean.getStock());
                                        goodsBean.setStock(0);
                                        DBManager.updateById(goodsBean);
                                    } else if (machineCode == -1202) {
                                        //货斗有货
                                        BVMManager.openDoorAgain();
                                    }
                                    shipmentBean.setCode(SHIPMENT_ERROR);
                                    shipmentBean.setMachineCode(machineCode);
                                    shipmentBean.setErrorMessage(errorMsg);//"出货失败"
                                    return Observable.just(shipmentBean);
                                }
                            } else {
                                //进行退款
                                shipmentBean.setCode(STOCK_ERROR);
                                shipmentBean.setErrorMessage("本地商品库存不足");
                                return Observable.just(shipmentBean);
                            }
                        } catch (RemoteException e) {
                            shipmentBean.setCode(SHIPMENT_UNUSUAL);
                            shipmentBean.setErrorMessage("商品出货失败,机器异常");
                            return Observable.just(shipmentBean);
                        }
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new RxProgress<ShipmentBean>() {
                    @Override
                    protected void onOverSubscribe(Disposable d) {

                    }

                    @Override
                    protected void onOverNext(ShipmentBean shipmentBean) {
                        if (shipmentBean.getCode() == SHIPMENT_SUCCESS) {//出货成功
                            payContract.shipmentSuccess(mPayResultBean);
                        } else {//出货失败
                            orderRefund(invoice, shipmentBean.getErrorMessage(), shipmentBean.getCode(), amt, shipmentBean.getSeq());
                        }
                    }

                    @Override
                    protected void onOverError(int code, String msg) {
                        if (code == ASK_PAY_ERROR) {
                            //支付接口请求失败
                            payContract.onTradeFailed(code, msg, amt, false);
                        } else {
                            //同意处理为交易失败
                            String content = "支付金额将会自动退回您的账户";
                            payContract.onTradeFailed(code, content, amt, true);
                        }
                        //刷新首页ui
                        payContract.onEmptyCargo();
                    }
                });
    }


    /**
     * 消费智盘入账
     *
     * @param invoice    流水号
     * @param amt        金额
     * @param prods      商品信息
     * @param payType    支付方式(微信\支付宝)
     * @param mGoodsBean 商品对象
     */
    public void DishPay(final String invoice, final int amt, final String prods, final int payType, final GoodsBean mGoodsBean) {
        //先记录异常,后面如果没有在删除
        unusualDiskInvoice(true, invoice, String.format(Locale.CHINA, "%d", amt));
        Observable.just("")
                .observeOn(Schedulers.io())
                .flatMap(new Function<String, ObservableSource<PayResultBean>>() {
                    @Override
                    public ObservableSource<PayResultBean> apply(String s) throws Exception {
                        //发起支付
                        // BVMManager.faultClean();
                        return ApiManager.orderTrade(invoice, amt, prods, "", payType)
                                .doOnError(new Consumer<Throwable>() {
                                    @Override
                                    public void accept(Throwable throwable) throws Exception {
                                        //三方支付马上退款并且记录到智盘退款订单中
                                        diskRefund(invoice, "支付失败", -100, amt, "");
                                    }
                                });
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .flatMap(new Function<PayResultBean, ObservableSource<String>>() {
                    @Override
                    public ObservableSource<String> apply(PayResultBean payResultBean) throws Exception {
                        //支付请求成功.但结果需要筛选判断
                        if (RxException.isSuccess(payResultBean.getCode()) || payResultBean.getCode() == RxException.PAY_REPEAT) { //成功
                            mPayResultBean = payResultBean;
                            payContract.onPaySuccess();
                            return Observable.just(payResultBean.getSeq());
                        } else if (payResultBean.getCode() == 3 && payResultBean.getSub_code() == 6) {
                            //调用智盘退款方法进行退款
                            diskRefund(invoice, "支付异常", payResultBean.getCode(), amt, "");
                            return Observable.error(new RxException(ASK_PAY_ERROR, RxException.getErrorMsg(payResultBean.getCode(), payResultBean.getSub_code())));
                        } else {
                            diskRefund(invoice, "支付异常", payResultBean.getCode(), amt, "");
                            //提示操作失败(支付未成功)
                            return Observable.error(new RxException(ASK_PAY_ERROR, RxException.getErrorMsg(payResultBean.getCode(), payResultBean.getSub_code())));
                        }
                    }
                })
                .observeOn(Schedulers.io())
                .flatMap(new Function<String, ObservableSource<ShipmentBean>>() {
                    @Override
                    public ObservableSource<ShipmentBean> apply(String seq) throws Exception {
                        ShipmentBean shipmentBean = new ShipmentBean();
                        shipmentBean.setSeq(seq);
                        try {
                            List<GoodsBean> goodsBeanList = DBManager.findSameById(mGoodsBean.getProdid());
                            if (goodsBeanList != null && goodsBeanList.size() != 0) {
                                GoodsBean goodsBean = goodsBeanList.get(0);
                                String param = BVMManager.takeGoodsParam(goodsBean.getRow(), goodsBean.getColumn(), goodsBean.getPrice(), null);
                                String response = BVMManager.takeGoods(param);
                                Map<String, Object> responseMap = JsonUtils.convertJsonToObject(response);
                                Log.e("responseMap", responseMap.toString());
                                int machineCode = Integer.parseInt(String.valueOf(responseMap.get("shipresult")));
//                                if (machineCode == 0) {
//                                    goodsBean.setStock(goodsBean.getStock() - 1);
//                                    DBManager.updateById(goodsBean);
//                                    mGoodsBean.setStock(mGoodsBean.getStock() - 1);
//                                    shipmentBean.setCode(SHIPMENT_SUCCESS);
//                                    shipmentBean.setMachineCode(machineCode);
//                                    shipmentBean.setErrorMessage("出货成功");
//                                    return Observable.just(shipmentBean);
//                                } else {
//                                    int[] temp = BVMManager.currentTemp();
//                                    String errorMsg = BVMManager.errorMsg(machineCode);
//                                    ApiManager.termStatus(temp[0], String.valueOf(machineCode), errorMsg);
//                                    if (machineCode == -1203) {
//                                        //空货道,需要清空库存并向服务器上报信息
//                                        Log.e("空货道", "空货道错误");
//                                        mGoodsBean.setStock(mGoodsBean.getStock() - goodsBean.getStock());
//                                        goodsBean.setStock(0);
//                                        DBManager.updateById(goodsBean);
//                                    } else if (machineCode == -1202) {
//                                        //货斗有货
//                                        BVMManager.openDoorAgain();
//                                    }
//                                    shipmentBean.setCode(SHIPMENT_ERROR);
//                                    shipmentBean.setMachineCode(machineCode);
//                                    shipmentBean.setErrorMessage(errorMsg);//"出货失败"
//                                    return Observable.just(shipmentBean);
//                                }
                                return Observable.just(shipmentBean);
                            } else {
                                //进行退款
                                shipmentBean.setCode(STOCK_ERROR);
                                shipmentBean.setErrorMessage("本地商品库存不足");
                                return Observable.just(shipmentBean);
                            }
                        } catch (RemoteException e) {
                            shipmentBean.setCode(SHIPMENT_UNUSUAL);
                            shipmentBean.setErrorMessage("商品出货失败,机器异常");
                            return Observable.just(shipmentBean);
                        }

                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new RxProgress<ShipmentBean>() {
                    @Override
                    protected void onOverSubscribe(Disposable d) {

                    }

                    @Override
                    protected void onOverNext(ShipmentBean shipmentBean) {
                        if (shipmentBean.getCode() == SHIPMENT_SUCCESS) {
                            //出货成功
                            //智盘入账成功并且出货成功了.可以删除智盘异常订单
                            unusualDiskInvoice(false, invoice, String.format(Locale.CHINA, "%d", amt));
                            unusualPassInvoice(false, invoice, String.format(Locale.CHINA, "%d", amt), "");
                            payContract.shipmentSuccess(mPayResultBean);
                        } else {
                            //出货失败
                            //出货失败调用第三方使用的智盘入账退款方法
                            diskRefund(invoice, shipmentBean.getErrorMessage(), shipmentBean.getCode(), amt, "");
                        }
                    }

                    @Override
                    protected void onOverError(int code, String msg) {
                        if (code == ASK_PAY_ERROR) {
                            //支付接口请求失败,如果刷卡支付直接提示支付状态,如果异常则调用pass退款
                            payContract.onTradeFailed(code, msg, amt, false);
                        } else {
                            //同意处理为交易失败
                            String content = "支付金额将会自动退回您的账户";
                            payContract.onTradeFailed(code, content, amt, true);
                        }
                        //刷新首页ui
                        payContract.onEmptyCargo();
                    }
                });
    }


    /**
     * 消费支付（适用于净菜柜）
     *
     * @param invoice 终端订单号
     * @param amt     实际消费金额，分为单位
     * @param prods   商品列表
     * @param payCode 支付码-卡付时传入物理卡号
     */
    @SuppressLint("CheckResult")
    private void orderTradeForCV(final String invoice, final int amt, final String prods, final String payCode) {
        Observable.just("")
                .observeOn(Schedulers.io())
                .flatMap(new Function<String, ObservableSource<PayResultBean>>() {
                    @Override
                    public ObservableSource<PayResultBean> apply(String s) throws Exception {
                        //发起支付
                        return ApiManager.orderTradeForCV(invoice, amt, prods, payCode)
                                .doOnError(new Consumer<Throwable>() {
                                    @Override
                                    public void accept(Throwable throwable) throws Exception {
                                        PayLogBean payLogBean = new PayLogBean();
                                        payLogBean.setInvoice(invoice);
                                        payLogBean.setMsg("支付异常");
                                        PayLogUtil.saveLog(PayLogUtil.REFUND, payLogBean, PayLogBean.class);
                                    }
                                });
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .flatMap(new Function<PayResultBean, ObservableSource<String>>() {
                    @Override
                    public ObservableSource<String> apply(PayResultBean payResultBean) throws Exception {
                        payContract.shipmentSuccess(payResultBean);
                        //支付结果
                        if (RxException.isSuccess(payResultBean.getCode()) || payResultBean.getCode() == RxException.PAY_REPEAT) {
                            mPayResultBean = payResultBean;
                            //通知首页更新ui
                            payContract.onPaySuccess();
                            return Observable.just(payResultBean.getSeq());
                        } else if (payResultBean.getCode() == 3 && payResultBean.getSub_code() == 6) {
                            //需要进行撤单
                            PayLogBean payLogBean = new PayLogBean();
                            payLogBean.setInvoice(invoice);
                            payLogBean.setMsg("支付异常");
                            payLogBean.setSeq(payResultBean.getSeq());
                            PayLogUtil.saveLog(PayLogUtil.REFUND, payLogBean, PayLogBean.class);
                            return Observable.error(new RxException(ASK_PAY_ERROR,
                                    RxException.getErrorMsg(payResultBean.getCode(),
                                            payResultBean.getSub_code())));
                        } else {
                            //提示操作失败(支付未成功)
                            return Observable.error(new RxException(ASK_PAY_ERROR,
                                    RxException.getErrorMsg(payResultBean.getCode(),
                                            payResultBean.getSub_code())));
                        }
                    }
                }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new RxProgress<String>() {
                    @Override
                    protected void onOverSubscribe(Disposable d) {

                    }

                    @Override
                    protected void onOverNext(String result) {

                    }

                    @Override
                    protected void onOverError(int code, String msg) {
                        if (code == ASK_PAY_ERROR) {
                            //关于卡信息的问题直接提示道支付界面
                            payContract.onGetCardFailed(code, msg);
                        } else {
                            //同意处理为交易失败
                            String content = "支付金额将会自动退回您的账户";
                            payContract.onTradeFailed(code, content, amt, true);
                        }
                        //刷新首页ui
                        payContract.onEmptyCargo();
                    }
                });

    }


    /**
     * 申请退款
     */
    public void orderRefund(final String invoice, final String eMsg, final int eCode, final int amt, final String seq) {
        ApiManager.logOrderRefund(invoice, eMsg, seq)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new RxProgress<RefundBean>() {
                    @Override
                    protected void onOverSubscribe(Disposable d) {
                        disposable.add(d);
                    }

                    @Override
                    protected void onOverNext(RefundBean refundBean) {
                        if (RxException.isSuccess(refundBean.getCode()) || refundBean.getCode() == RxException.ORDER_NONENTITY) {
                            //退款成功,不保存日志
                            refundCallback(false, invoice, eMsg, eCode, amt, seq);
                        } else {
                            //退款失败,需要保存日志
                            refundCallback(true, invoice, eMsg, eCode, amt, seq);
                        }
                    }

                    @Override
                    protected void onOverError(int code, String msg) {
                        //退款可能失败,需要保存日志
                        refundCallback(true, invoice, eMsg, eCode, amt, seq);
                    }
                });
    }

    /**
     * 退款结果后的回调
     *
     * @param isSave 是否需要保存日志
     */
    private void refundCallback(boolean isSave, final String invoice, final String eMsg, final int eCode, final int amt, final String seq) {
        if (isSave) {
            //退款失败,需要保存日志
            PayLogBean payLogBean = new PayLogBean();
            payLogBean.setInvoice(invoice);
            payLogBean.setMsg(eMsg);
            payLogBean.setSeq(seq);
            writeLogThread(PayLogUtil.REFUND, payLogBean, PayLogBean.class);
        }
        String content = eMsg + "\n" + "支付金额将会自动退回您的账户";
        payContract.onTradeFailed(eCode, content, amt, true);
        //盘点库存
        stockUpload();
        //刷新首页ui
        payContract.onEmptyCargo();
    }

    /**
     * 智盘退款方法(撤单)
     *
     * @param invoice
     * @param eMsg
     * @param eCode
     * @param amt
     * @param seq
     */
    public void diskRefund(final String invoice, final String eMsg, final int eCode, final int amt, final String seq) {
        ApiManager.logOrderRefund(invoice, eMsg, seq)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new RxProgress<RefundBean>() {
                    @Override
                    protected void onOverSubscribe(Disposable d) {
                        disposable.add(d);
                    }

                    @Override
                    protected void onOverNext(RefundBean refundBean) {
                        if (RxException.isSuccess(refundBean.getCode()) || refundBean.getCode() == RxException.ORDER_NONENTITY) {
                            //撤单成功,删除dish异常记录
                            refundDiskInvoice(false, invoice, String.format(Locale.CHINA, "%d", amt));

                            String content = eMsg + "\n" + "支付金额将会自动退回您的账户";
                            //回掉实现显示逻辑
                            payContract.onTradeFailed(eCode, content, amt, true);
                            //盘点库存
                            stockUpload();
                            //刷新首页ui
                            payContract.onEmptyCargo();
                        } else {
                            //退款失败,删除本地智盘异常
                            unusualDiskInvoice(false, invoice, String.format(Locale.CHINA, "%d", amt));
                            //加入智盘撤单
                            refundDiskInvoice(true, invoice, String.format(Locale.CHINA, "%d", amt));
                        }

                        //智盘退款成功后需要继续pass退款,是否成功在pass退款中判断这里只调用
                        passRefund(invoice, String.format(Locale.CHINA, "%d", amt));

                    }

                    @Override
                    protected void onOverError(int code, String msg) {
                        //退款失败,删除本地智盘异常
                        unusualDiskInvoice(false, invoice, String.format(Locale.CHINA, "%d", amt));
                        //加入智盘撤单
                        refundDiskInvoice(true, invoice, String.format(Locale.CHINA, "%d", amt));

                        //智盘退款成功后需要继续pass退款,是否成功在pass退款中判断这里只调用
                        passRefund(invoice, String.format(Locale.CHINA, "%d", amt));

                    }
                });
    }

    /**
     * 库存盘点
     */
    public void stockUpload() {
        Observable.just("")
                .observeOn(Schedulers.io())
                .flatMap(new Function<String, ObservableSource<TermSignIn>>() {
                    @Override
                    public ObservableSource<TermSignIn> apply(String s) throws Exception {
                        List<GoodsBean> goodsList = DBManager.findAllById();
                        return ApiManager.stockCheck(11, goodsList);
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new RxProgress<TermSignIn>() {
                    @Override
                    protected void onOverSubscribe(Disposable d) {

                    }

                    @Override
                    protected void onOverNext(TermSignIn termSignIn) {

                    }

                    @Override
                    protected void onOverError(int code, String msg) {

                    }
                });
    }


    //--------------------------------日志保存部分-------------------------------------------------

    /**
     * 为防止支付界面异常退出、或支付列表中存在异常支付项
     * 微信支付宝支 #发起支付# 时保存日志，单据支付成功后删除日志
     * 每次登录成功后，检查此文件，并做退款
     *
     * @param isSave true 保存；false 删除
     */
    public void unusualPassInvoice(boolean isSave, final String invoices, final String amt, String payMode) {
        if (isSave) {
            //并将此单保存做提交
            PayLogBean payLogBean = new PayLogBean();
            payLogBean.setInvoice(invoices);
            payLogBean.setMsg(payMode);
            payLogBean.setDate(FormatUtil.getDateStr());
            writeLogThread(PayLogUtil.PASS_UNUSUAL, payLogBean, PayLogBean.class);
        } else {
            //保存pass订单号
            PayLogBean payLogBean = new PayLogBean();
            payLogBean.setDate(FormatUtil.getDateStr());
            payLogBean.setInvoice(invoices);
            //删除pass异常订单
            deleteLogThread(PayLogUtil.PASS_UNUSUAL, payLogBean, PayLogBean.class);
        }
    }

    /**
     * pass退款订单
     *
     * @param isSave
     * @param invoices
     * @param amt
     */
    public void refundPassInvoice(boolean isSave, final String invoices, final String amt, String payMode) {
        if (isSave) {
            //并将此单保存做提交
            PayLogBean payLogBean = new PayLogBean();
            payLogBean.setInvoice(invoices);
            payLogBean.setMsg(payMode);
            payLogBean.setDate(FormatUtil.getDateStr());
            writeLogThread(PayLogUtil.PASS_REFUND, payLogBean, PayLogBean.class);
        } else {
            //保存pass订单号 做退款
            PayLogBean payLogBean = new PayLogBean();
            payLogBean.setDate(FormatUtil.getDateStr());
            payLogBean.setInvoice(invoices);
            //删除pass异常订单
            deleteLogThread(PayLogUtil.PASS_REFUND, payLogBean, PayLogBean.class);
        }
    }

    /**
     * 智盘入账异常订单
     *
     * @param isSave
     * @param invoices
     * @param amt
     */
    public void unusualDiskInvoice(boolean isSave, final String invoices, final String amt) {
        if (isSave) {
            //并将此单保存做提交
            PayLogBean payLogBean = new PayLogBean();
            payLogBean.setInvoice(invoices);
            payLogBean.setDate(FormatUtil.getDateStr());
            writeLogThread(PayLogUtil.DISH_UNUSUAL, payLogBean, PayLogBean.class);
        } else {

            PayLogBean payLogBean = new PayLogBean();
            payLogBean.setDate(FormatUtil.getDateStr());
            payLogBean.setInvoice(invoices);
            //删除pass异常订单
            deleteLogThread(PayLogUtil.DISH_UNUSUAL, payLogBean, PayLogBean.class);
        }
    }

    /**
     * 智盘入账退款(不检查库存)
     *
     * @param isSave
     * @param invoices
     * @param amt
     */
    public void refundDiskInvoice(boolean isSave, final String invoices, final String amt) {
        if (isSave) {
            //将此单保存做提交
            PayLogBean payLogBean = new PayLogBean();
            payLogBean.setInvoice(invoices);
            payLogBean.setMsg("智盘入账异常");
            PayLogUtil.saveLog(PayLogUtil.REFUND, payLogBean, PayLogBean.class);
        } else {

            PayLogBean payLogBean = new PayLogBean();
            payLogBean.setDate(FormatUtil.getDateStr());
            payLogBean.setInvoice(invoices);
            //删除智盘异常订单
            deleteLogThread(PayLogUtil.REFUND, payLogBean, PayLogBean.class);
        }
    }


    //----------------------------文件写入---------------------------------------------------------

    /**
     * 异步任务删除文件
     *
     * @param type   文件名标示
     * @param object 写入对象
     * @param tClass 泛型
     */
    private <T> void deleteLogThread(final String type, final T object, final Class<T> tClass) {
        Observable.just("")
                .observeOn(Schedulers.io())
                .flatMap(new Function<String, ObservableSource<String>>() {
                    @Override
                    public ObservableSource<String> apply(@NonNull String s) throws Exception {
                        PayLogUtil.deleteLog(type, object, tClass);
                        return Observable.just("");
                    }
                })
                .subscribe(new Consumer<String>() {
                    @Override
                    public void accept(String s) throws Exception {

                    }
                });
    }

    /**
     * 异步任务写入文件
     *
     * @param type   文件名标示
     * @param object 写入对象
     * @param tClass 泛型
     */
    private <T> void writeLogThread(final String type, final T object, final Class<T> tClass) {
        Observable.just("")
                .observeOn(Schedulers.io())
                .flatMap(new Function<String, ObservableSource<String>>() {
                    @Override
                    public ObservableSource<String> apply(@NonNull String s) throws Exception {
                        PayLogUtil.saveLog(type, object, tClass);
                        return Observable.just("");
                    }
                })
                .subscribe(new Consumer<String>() {
                    @Override
                    public void accept(String s) throws Exception {

                    }
                });

    }


    public void cancelRequest() {
        if (disposable != null) {
            disposable.clear();
        }

        if (passCheckDisposable != null) {
            passCheckDisposable.dispose();
        }
    }
}
