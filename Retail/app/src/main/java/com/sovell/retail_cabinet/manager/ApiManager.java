package com.sovell.retail_cabinet.manager;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.TextUtils;
import android.util.Base64;
import android.widget.Toast;

import com.sovell.retail_cabinet.base.PassPayBean;
import com.sovell.retail_cabinet.base.PassTokenBean;
import com.sovell.retail_cabinet.bean.BannerAD;
import com.sovell.retail_cabinet.bean.CardInfoBean;
import com.sovell.retail_cabinet.bean.GoodsBean;
import com.sovell.retail_cabinet.bean.OrderBean;
import com.sovell.retail_cabinet.bean.OrderTakeBean;
import com.sovell.retail_cabinet.bean.PayLogBean;
import com.sovell.retail_cabinet.bean.PayResultBean;
import com.sovell.retail_cabinet.bean.ProdBean;
import com.sovell.retail_cabinet.bean.RefundBean;
import com.sovell.retail_cabinet.bean.TermPairing;
import com.sovell.retail_cabinet.bean.TermSignIn;
import com.sovell.retail_cabinet.bean.VersionInfo;
import com.sovell.retail_cabinet.https.Api;
import com.sovell.retail_cabinet.https.ApiDownLoad;
import com.sovell.retail_cabinet.https.DownloadListener;
import com.sovell.retail_cabinet.https.HttpsAddress;
import com.sovell.retail_cabinet.https.RetryWhenDelay;
import com.sovell.retail_cabinet.https.RxException;
import com.sovell.retail_cabinet.https.RxProgress;
import com.sovell.retail_cabinet.presenter.impl.PayPresenterImpl;
import com.sovell.retail_cabinet.utils.ConfigUtil;
import com.sovell.retail_cabinet.utils.FileUtil;
import com.sovell.retail_cabinet.utils.FormatUtil;
import com.sovell.retail_cabinet.utils.ParamsUtil;
import com.sovell.retail_cabinet.utils.PayLogUtil;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import okhttp3.ResponseBody;

public class ApiManager {

    /**
     * 终端配对码
     *
     * @param code 配对码
     */
    public static Observable<TermPairing> termPairing(String code, int type) {
        Map<String, Object> params = new HashMap<>();
        params.put("type", type); //41零售柜，42净菜柜
        params.put("code", code);

        return Api.Instance().mApiService
                .termPairing(ParamsUtil.getParams(params, null))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    /**
     * 终端登录
     *
     * @param api  api地址
     * @param shop 餐厅id
     * @param term 终端id
     * @param key  终端密钥
     */
    public static Observable<TermSignIn> termSignIn(String api, String shop, String term, String key) {
        Map<String, Object> params = new HashMap<>();
        params.put("shop", shop);
        params.put("term", term);

        return Api.Instance(api)
                .mApiService
                .termSignIn(ParamsUtil.getParams(params, key))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());

    }

    public static Observable<TermSignIn> termSignIn() {
        return Api.Instance()
                .mApiService
                .termSignIn(ParamsUtil.getParams(null, null));
    }

    /**
     * 终端注销
     */
    public static void termSignOut(String authKey) {
        Map<String, Object> params = new HashMap<>();
        params.put("authkey", authKey);

        Api.Instance()
                .mApiService
                .termSignOut(ParamsUtil.getParams(params, null))
                .subscribeOn(Schedulers.io())
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

    /**
     * 终端状态上报
     */
    public static void termStatus(int temp, String error, String errorMsg) {
        Map<String, Object> params = new HashMap<>();
        params.put("temp", temp);
        params.put("error_code", error);
        params.put("error_msg", errorMsg);

        Api.Instance()
                .mApiService
                .termStatus(ParamsUtil.getParams(params, null))
                .subscribeOn(Schedulers.io())
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

    /**
     * 终端心跳
     */
    public static Observable<TermSignIn> termKeep(String authKey) {
        Map<String, Object> params = new HashMap<>();
        params.put("authkey", authKey);

        return Api.Instance()
                .mApiService
                .termKeep(ParamsUtil.getParams(params, null));
    }

    /**
     * 终端商品库存盘点
     *
     * @param type      盘点类型 10 正常上货; 11 定时盘点; 12 清空库存
     * @param goodsList prodid 商品id stock 库存
     *                  如:[{"prodid":"1","stock":"2"},{"prodid":"2","stock":"3"}]
     */
    public static Observable<TermSignIn> stockCheck(int type, List<GoodsBean> goodsList) {
        String goodsJson = "[]";
        if (goodsList != null) {
            try {
                JSONArray jsonArray = new JSONArray();
                for (GoodsBean goodsBean : goodsList) {
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("prodid", goodsBean.getProdid());
                    jsonObject.put("stock", goodsBean.getStock());
                    jsonArray.put(jsonObject);
                }
                goodsJson = jsonArray.toString();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        Map<String, Object> params = new HashMap<>();
        params.put("type", type);
        params.put("stocklist", goodsJson);

        return Api.Instance()
                .mApiService
                .stockCheck(ParamsUtil.getParams(params, null));
    }

    /**
     * 终端商品列表
     */
    public static Observable<ProdBean> prodList() {
        return Api.Instance()
                .mApiService
                .prodList(ParamsUtil.getParams(null, null));
    }

    /**
     * 获取卡信息
     *
     * @param cardId 卡号
     */
    public static Observable<CardInfoBean> cardGet(String cardId) {
        Map<String, Object> params = new HashMap<>();
        params.put("cid", cardId);

        return Api.Instance()
                .mApiService
                .cardGet(ParamsUtil.getParams(params, null))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    /**
     * 消费记账
     *
     * @param invoice 订单号
     * @param amt     支付金额，分为单位
     * @param prods   商品列表
     * @param code    支付码-卡付时传入物理卡号
     */
    public static Observable<PayResultBean> orderTrade(String invoice, int amt, String prods, String code, int payType) {
        Map<String, Object> params = new HashMap<>();
        params.put("invoice", invoice);
        params.put("amt", amt);
        params.put("prods", prods);
        params.put("pay_code", code);
        //4:净菜预付快取;3:零售消费
        params.put("type", 3);
        //支付类型 1:卡付 31 微信 32 支付宝
        params.put("pay_type", payType);

        return Api.Instance()
                .mApiService
                .orderTrade(ParamsUtil.getParams(params, null));
    }

    /**
     * 消费记账
     *
     * @param invoice 订单号
     * @param amt     支付金额，分为单位
     * @param prods   商品列表
     * @param code    支付码-卡付时传入物理卡号
     */
    public static Observable<PayResultBean> orderTradeForCV(String invoice, int amt, String prods, String code) {
        Map<String, Object> params = new HashMap<>();
        params.put("invoice", invoice);
        params.put("amt", amt);
        params.put("prods", prods);
        params.put("pay_code", code);
        //4:净菜预付快取;3:零售消费
        params.put("type", 4);
        //支付类型 1:卡付
        params.put("pay_type", 1);

        return Api.Instance()
                .mApiService
                .orderTrade(ParamsUtil.getParams(params, null));
    }

    /**
     * 撤单(暂时废弃)
     *
     * @param invoice 订单号
     * @param msg     退款原因
     */
    public static Observable<Integer> orderRefund(final String invoice, final String msg) {
        Map<String, Object> params = new HashMap<>();
        params.put("invoice", invoice);
        params.put("msg", msg);

        return Api.Instance()
                .mApiService
                .orderRefund(ParamsUtil.getParams(params, null))
                .flatMap(new Function<RefundBean, ObservableSource<Integer>>() {
                    @Override
                    public ObservableSource<Integer> apply(RefundBean refundBean) throws Exception {
                        if (RxException.isSuccess(refundBean.getCode()) || refundBean.getCode() == RxException.ORDER_NONENTITY) {
                            //退款成功
                            return Observable.error(new RxException(PayPresenterImpl.REFUND_SUCCESS, msg));
                        } else {
                            //退款失败,需要保存日志
                            return Observable.error(new RxException(PayPresenterImpl.REFUND_ERROR, msg));
                        }
                    }
                })
                .doOnError(new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        RxException rxException = null;
                        try {
                            rxException = (RxException) throwable;
                            if (rxException.getErrorCode() != PayPresenterImpl.REFUND_SUCCESS) {
                                PayLogBean payLogBean = new PayLogBean();
                                payLogBean.setInvoice(invoice);
                                payLogBean.setMsg(msg);
                                PayLogUtil.saveLog(PayLogUtil.REFUND, payLogBean, PayLogBean.class);
                            }
                        } catch (Exception e) {
                            PayLogBean payLogBean = new PayLogBean();
                            payLogBean.setInvoice(invoice);
                            payLogBean.setMsg(msg);
                            PayLogUtil.saveLog(PayLogUtil.REFUND, payLogBean, PayLogBean.class);
                        }

                    }
                });
    }

    /**
     * 撤单
     *
     * @param invoice 订单号
     * @param msg     退款原因
     */
    public static Observable<RefundBean> logOrderRefund(final String invoice, final String msg, final String seq) {
        Map<String, Object> params = new HashMap<>();
        params.put("invoice", invoice);
        if (!TextUtils.isEmpty(seq)) {
            params.put("seq", seq);
        }
        params.put("msg", msg);

        return Api.Instance()
                .mApiService
                .orderRefund(ParamsUtil.getParams(params, null));
    }


    /***
     * 查询订单
     * @param type  10 查询当日未取菜品,20 查询所有订单列表-默认查询时间为一星期之内
     * @param cid 卡号
     * @param pageNum 页码分页用
     * @return
     */
    public static Observable<OrderBean> orderList(int type, String cid, int pageNum) {
        Map<String, Object> params = new HashMap<>();
        //10 查询当日未取菜品
        //20 查询所有订单列表-默认查询时间为一星期之内
        params.put("type", type);
        //卡号
        params.put("cid", cid);
        //页码
        params.put("pi", pageNum);
        //每页数量
        params.put("ps", 20);

        // 排序规则 0 顺序 1 倒序 默认为0
        params.put("sort", 1);
        if (type == 20) {

            params.put("sd", FormatUtil.getToday() + " 00:01:00");

            params.put("ed", FormatUtil.getToday() + " 23:58:00");
        }

        return Api.Instance()
                .mApiService.orderList(ParamsUtil.getParams(params, null));
    }

    /***
     *
     * 取货
     * 每次取货之前调用一次，取货如果失败，带上失败码再调用一次。
     * @param prodlist//商品列表
     * @param type //类型 10=正常取货 20=取货失败，
     * @param errorMsg //故障说明
     * @return
     */
    public static Observable<OrderTakeBean> orderTake(String prodlist, int type, String errorMsg) {
        Map<String, Object> params = new HashMap<>();
        //商品列表，
        params.put("prodlist", prodlist);
        if (!errorMsg.isEmpty()) {
            //故障码，
            params.put("remark", errorMsg);
        }
        params.put("type", type);
        return Api.Instance()
                .mApiService.orderTake(ParamsUtil.getParams(params, null));
    }

    /***
     * 订单查询
     * @param invoice 本地订单号
     * @return
     */

    public static Observable<OrderBean> order_get(String invoice) {
        Map<String, Object> params = new HashMap<>();
        //订单查询
        params.put("app", "dish");
        params.put("t", 2113);
        params.put("invoice", invoice);
        params.put("target_term", ConfigUtil.Instance().getString(ConfigUtil.TERM, "1"));
        return Api.Instance()
                .mApiService.dishCheck(ParamsUtil.getParams(params, null));
    }


    //---------------------------------------------扫码支付部分-------------------------------------------

    /**
     * 获取Pass Token
     */
    public static Observable<PassTokenBean> passToken() {
        String clientID = ConfigUtil.Instance().getString(ConfigUtil.PASS_CLIENT_ID);
        final String clientCredentials = "client_credentials";
        clientID = "Basic " + Base64.encodeToString(clientID.getBytes(), Base64.NO_WRAP);
        final String finalClientID = clientID;
        return Observable.just("")
                .flatMap(new Function<String, ObservableSource<PassTokenBean>>() {
                    @Override
                    public ObservableSource<PassTokenBean> apply(@NonNull String s) throws Exception {
                        long passTokenTime = ConfigUtil.Instance().getLong(ConfigUtil.PASS_TOKEN_TIME);
                        if (System.currentTimeMillis() > passTokenTime) {
                            return Api.Instance()
                                    .mApiService
                                    .passToken(finalClientID, clientCredentials)
                                    .retryWhen(new RetryWhenDelay());
                        }
                        PassTokenBean passTokenBean = new PassTokenBean();
                        return Observable.just(passTokenBean);
                    }
                }).flatMap(new Function<PassTokenBean, ObservableSource<PassTokenBean>>() {
                    @Override
                    public ObservableSource<PassTokenBean> apply(PassTokenBean passTokenBean) throws Exception {
                        ConfigUtil.Instance().savePassToken(passTokenBean.getExpires_in(), passTokenBean.getAccess_token());
                        return Observable.just(passTokenBean);
                    }
                });
    }


    /**
     * 检查Pass支付结果
     *
     * @param seq 订单号
     */
    public static Observable<PassPayBean> passCheck(String seq) {
        String token = "Bearer " + ConfigUtil.Instance().getString(ConfigUtil.PASS_TOKEN);
        return Api.Instance()
                .mApiService
                .passCheck(token, seq, HttpsAddress.QUERY_TIMEOUT)
                .delaySubscription(50, TimeUnit.MILLISECONDS)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    /***
     *
     * pass支付
     * 获取pass_token,并保存,获取前需要检测passtoken的时效性,
     * (在过期时间上应减少2s避免token刚好过期时用户发起请求)
     * 过期后需要重新请求pass_token,
     * pass开单，返回一个支付地址，Android端生成一个二维码让用户进行支付
     * 接口调用前先检测pass_token是否过期.
     * @return
     */
    public static Observable<PassPayBean> passPayQRCode(final String amt, final String invoices, final String payMode) {

        return passToken().flatMap(new Function<PassTokenBean, ObservableSource<PassPayBean>>() {
            @Override
            public ObservableSource<PassPayBean> apply(PassTokenBean passTokenBean) throws Exception {
                String token = "Bearer " + ConfigUtil.Instance().getString(ConfigUtil.PASS_TOKEN);
                return Api.Instance()
                        .mApiService
                        .passPayQRCode(token, ParamsUtil.passParamMap(amt, payMode, invoices, ""));
            }
        });
    }

    /**
     * Pass退款
     *
     * @param seq 订单号
     */
    public static Observable<PassPayBean> passRefund(String seq) {
        String token = "Bearer " + ConfigUtil.Instance().getString(ConfigUtil.PASS_TOKEN);
        Map<String, Object> paramsMap = new HashMap<>();
        paramsMap.put("seq", seq);
        String sign = ParamsUtil.passSign(paramsMap);
        return Api.Instance()
                .mApiService
                .passRefund(token, seq, sign)
                .delaySubscription(2, TimeUnit.SECONDS);
    }


    /**
     * 广告屏－取得本终端广告屏策略
     */
    public static Observable<BannerAD> showcase() {
        Map<String, Object> params = new HashMap<>();
        params.put("t", 1);
        params.put("oper", "retail");

        return Api.Instance()
                .mApiService
                .showcase(ParamsUtil.getParams(params, null))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    /**
     * 获取最新版本
     */
    public static Observable<VersionInfo> checkVersion() {
        return Api.Instance().mApiXmlService
                .checkVersion(HttpsAddress.getCheckVersionUrl())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    /**
     * 下载APK
     *
     * @param activity 上下文环境，切换主线程
     * @param apkName  下载apk的名称
     */
    public static void updateApp(final Activity activity, String apkName) {
        String downLoadUrl = HttpsAddress.getDownloadUrl(apkName);
        FileUtil.deleteDirApk(FileUtil.DOWNLOAD_PAK);
        final String path = FileUtil.DOWNLOAD_PAK + apkName;
        final ProgressDialog progressDia = initProgressDialog(activity);
        progressDia.show();
        new ApiDownLoad(new DownloadListener() {
            @Override
            public void update(final long bytesRead, final long contentLength, boolean done) {
                final int progress = (int) ((bytesRead * 100) / contentLength);
                if (progress <= 0) return;
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progressDia.setProgress(progress);
                        if (progress == 100 && bytesRead == contentLength) {
                            progressDia.setMessage("下载完成！");
                        } else {
                            String str = FileUtil.getDataSize(bytesRead)
                                    + "/"
                                    + FileUtil.getDataSize(contentLength);
                            progressDia.setProgressNumberFormat(str);
                        }
                    }
                });
            }
        }).download(downLoadUrl, path)
                .subscribe(new RxProgress<ResponseBody>() {
                    @Override
                    protected void onOverSubscribe(Disposable d) {

                    }

                    @Override
                    protected void onOverNext(ResponseBody responseBody) {
                        FileUtil.openApkFile(activity, new File(path));
                        progressDia.dismiss();
                    }

                    @Override
                    protected void onOverError(int code, String msg) {
                        progressDia.dismiss();
                        Toast.makeText(activity, "软件下载失败", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    /**
     * 初始化下载进度提示框
     */
    private static ProgressDialog initProgressDialog(Context context) {
        final ProgressDialog progressDia = new ProgressDialog(context);
        progressDia.setCancelable(true);
        progressDia.setCanceledOnTouchOutside(true);
        progressDia.setProgressNumberFormat(String.format("%s", ""));
        progressDia.setTitle("软件更新");
        progressDia.setMessage("正在下载，请稍后...");
        progressDia.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDia.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
            }
        });
        return progressDia;
    }

}
