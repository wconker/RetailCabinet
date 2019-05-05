package com.sovell.retail_cabinet.widget;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.sovell.retail_cabinet.R;
import com.sovell.retail_cabinet.bean.GoodsBean;
import com.sovell.retail_cabinet.bean.PayResultBean;
import com.sovell.retail_cabinet.https.RxException;
import com.sovell.retail_cabinet.https.RxProgress;
import com.sovell.retail_cabinet.manager.ApiManager;
import com.sovell.retail_cabinet.manager.BVMManager;
import com.sovell.retail_cabinet.manager.CardManager;
import com.sovell.retail_cabinet.manager.PayModeEnum;
import com.sovell.retail_cabinet.manager.PayStatusEnum;
import com.sovell.retail_cabinet.presenter.contract.PayContract;
import com.sovell.retail_cabinet.presenter.impl.PayPresenterImpl;
import com.sovell.retail_cabinet.utils.ConfigUtil;
import com.sovell.retail_cabinet.utils.ParamsUtil;
import com.sovell.retail_cabinet.utils.QRCodeUtil;

import java.util.Locale;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

import static com.sovell.retail_cabinet.manager.PayStatusEnum.BY_CARD;
import static com.sovell.retail_cabinet.manager.PayStatusEnum.FAIL;
import static com.sovell.retail_cabinet.manager.PayStatusEnum.IN_PAYMENT;
import static com.sovell.retail_cabinet.manager.PayStatusEnum.PICK_GOODS;
import static com.sovell.retail_cabinet.manager.PayStatusEnum.SHIPMENT;
import static com.sovell.retail_cabinet.manager.PayStatusEnum.SUCCESS;

public class PayDialog extends Dialog implements PayContract, CardManager.ReadCardListener,
        PayDetailsView.OnCloseListener, PaymentView.OnCloseListener, PayDetailsView.OnPayModelChangeListener {

    @BindView(R.id.payDetailsView)
    PayDetailsView payDetailsView;
    @BindView(R.id.paymentView)
    PaymentView paymentView;


    private CompositeDisposable mCompositeDisposable;//出货结果轮询器
    private Disposable mTimerResultDisposable;//返回结果计时
    private OnPayDialogListener mListener;
    private boolean firstShow = true;
    private GoodsBean mGoodsBean = new GoodsBean();
    private PayPresenterImpl presenter;
    private CardManager cardManager;
    private String mDevTermNum;
    private PayResultBean mPayResultBean = new PayResultBean();
    /*全部支付唯一单据流水号*/
    private volatile String mInvoiceNum = ConfigUtil.Instance().getString(ConfigUtil.TERM) + String.valueOf(System.currentTimeMillis());
    private int mInterval;

    public PayDialog(Context context) {
        super(context, R.style.BottomDialogStyle);
        this.presenter = new PayPresenterImpl(this);
        this.mCompositeDisposable = new CompositeDisposable();
        this.cardManager = new CardManager();
        this.cardManager.setReadCardListener(this);
        this.cardManager.open(context);
        this.mDevTermNum = ConfigUtil.Instance().getString(ConfigUtil.TERM);
        this.mInterval = ConfigUtil.Instance().getExitSecond(ConfigUtil.Instance().getInteger(ConfigUtil.EXIT_TIME));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_main_pay);
        ButterKnife.bind(this);
        setCanceledOnTouchOutside(false);
        Window window = getWindow();
        if (window != null) {
            WindowManager.LayoutParams layoutParams = window.getAttributes();
            layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
            layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
            window.getDecorView().setPadding(0, 0, 0, 0);
            window.setAttributes(layoutParams);
            window.setGravity(Gravity.BOTTOM);
            window.setWindowAnimations(R.style.DateDialogAnim);
        }
        payDetailsView.setOnCloseListener(this);
        payDetailsView.setOnPayModelChangeListener(this);
        paymentView.setOnCloseListener(this);
    }

    /**
     * 设置支付的各个状态
     */
    public void setState(PayStatusEnum state) {
        payDetailsView.setViewVisibility(View.GONE);
        paymentView.setSuccessVisib(View.GONE);
        paymentView.setFailVisib(View.GONE);
        payDetailsView.setState(state);
    }

    //生成二维码
    public void createQRCode(String payModel) {
        presenter.passPay(String.format(Locale.CHINA, "%d", mGoodsBean.getPrice()), ConfigUtil.getInvoice(), payModel);
    }


    /**
     * 设置支付的各个状态
     *
     * @param state 必须为失败状态
     * @param isPay 是否已支付成功
     * @param msg   显示错误信息
     */
    private void setStatePayment(PayStatusEnum state, boolean isPay, String msg) {
        payDetailsView.setViewVisibility(View.GONE);
        paymentView.setSuccessVisib(View.GONE);
        paymentView.setFailVisib(View.GONE);
        switch (state) {
            case SUCCESS:
                paymentView.setStatePayment(state, isPay, msg, mPayResultBean.getAmt(), mGoodsBean);
                startTimer(SUCCESS);
                break;
            case FAIL:
                paymentView.setStatePayment(state, isPay, msg, String.valueOf(mGoodsBean.getPrice()), mGoodsBean);
                startTimer(FAIL);
                break;
        }
    }

    /**
     * 读取卡号
     *
     * @param cid 卡号
     */
    @Override
    public void onReadCard(String cid) {
        payDetailsView.tvPayClose.setVisibility(View.GONE);
        closeTimer();
        cardManager.beeper();
        String prods = ParamsUtil.getProdsParams(mGoodsBean);
        presenter.cardGet(mInvoiceNum, mGoodsBean.getPrice(), prods, cid, mGoodsBean);
    }

    /**
     * 启动定时器
     *
     * @param state 计时状态
     */
    public void startTimer(final PayStatusEnum state) {
        closeTimer();
        final int countTime = state == BY_CARD ? mInterval : 10; //总时间
        Observable.interval(0, 1, TimeUnit.SECONDS)
                .take(countTime + 1)//设置总共发送的次数
                .map(new Function<Long, Long>() {
                    @Override
                    public Long apply(Long aLong) throws Exception {
                        //aLong从0开始
                        return countTime - aLong;
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Long>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        mTimerResultDisposable = d;
                    }

                    @Override
                    public void onNext(Long value) {//计时中
                        if (state == BY_CARD) {
                            payDetailsView.setTimerText("返回(" + value + "s)");
                        } else if (state == SUCCESS) {
                            paymentView.setSuccessTimerText("确定(" + value + "s)");
                        } else if (state == FAIL) {
                            paymentView.setFailTimerText("确定(" + value + "s)");
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                    }

                    @Override
                    public void onComplete() {//计时结束
                        if (state == BY_CARD) {
                            dismiss();
                        } else if (state == SUCCESS) {
                            dismiss();
                            if (mListener == null) return;
                            mListener.onPayDialog(state.getId());
                        } else if (state == FAIL) {
                            dismiss();
                            if (mListener == null) return;
                            mListener.onPayDialog(state.getId());
                        }
                        closeTimer();
                    }
                });
    }

    /**
     * 关闭定时器
     */
    public void closeTimer() {
        if (mTimerResultDisposable != null) {
            mTimerResultDisposable.dispose();
        }
    }

    /**
     * 设置数据源
     */
    public void setGoodData(GoodsBean goodsBeans) {
        if (isShowing()) return;
        show();
        mInvoiceNum = mDevTermNum + String.valueOf(System.currentTimeMillis());
        mGoodsBean = goodsBeans;
        if (firstShow) {
            setState(BY_CARD);
            firstShow = false;
        }
        payDetailsView.showContent(goodsBeans);
        startTimer(BY_CARD);
        cardManager.readCard();
    }


    @Override
    public void show() {
        super.show();
    }

    @Override
    public void dismiss() {
        cardManager.stop();
        setState(BY_CARD);
        //每次交易弹框消失都进行一次退款
        //PayLogManager.getInstance().unusualRefund();
        super.dismiss();
    }

    @Override
    public void onGetCardSuccess() {
        setState(IN_PAYMENT);
    }

    @Override
    public void onGetCardFailed(int code, String msg) {
        payDetailsView.tvPayClose.setVisibility(View.VISIBLE);
        startTimer(BY_CARD);
        cardManager.readCard();
        CustomToast.show(msg);
    }

    /*支付成功,更新ui*/
    @Override
    public void onPaySuccess() {
        setState(SHIPMENT);
    }

    /*空货道,更新ui*/
    @Override
    public void onEmptyCargo() {
        if (mListener != null) {
            mListener.onNotifyView(mGoodsBean);
        }
    }

    //出货成功
    @Override
    public void shipmentSuccess(PayResultBean payResultBean) {
        setState(PICK_GOODS);
        if (mListener != null) {
            mListener.onNotifyView(mGoodsBean);
        }
        mPayResultBean = payResultBean;
        //轮询取货信息
        pickGoodsInterval();
    }


    @Override
    public void onTradeFailed(int code, String msg, int amt, boolean isPay) {
        setStatePayment(FAIL, isPay, msg);
    }

    @Override
    public void onSweepCodeSuccess(String code, String msg, String payMode) {

        int Rid = payMode.equals(PayModeEnum.ALI_PAY.getValue()) ? R.drawable.ic_alipay : R.drawable.ic_wechat;
        Bitmap QRpay = QRCodeUtil.createQRImageLogo(msg, 400,
                QRCodeUtil.drawable2Bitmap(getContext().getResources().getDrawable(Rid)));
        payDetailsView.setQRCode(QRpay);

    }

    //pass支付成功
    @Override
    public void onPassPaySuccess(String invoices, String amt, PayModeEnum payCode) {
        if (!amt.isEmpty()) {
            String prods = ParamsUtil.getProdsParams(mGoodsBean);
            //智盘入账
          presenter.DishPay(invoices, Integer.valueOf(amt), prods, payCode.getCode(), mGoodsBean);


        }
    }


    private static final int PICKUP = 0;//已取货
    private static final int NO_PICKUP = 1;//未取货
    private static final int MACHINE_ERROR = 2;//机器错误

    /**
     * 取货轮询
     */
    private void pickGoodsInterval() {
        mCompositeDisposable.clear();
        Observable.interval(500, TimeUnit.MILLISECONDS)
                .observeOn(Schedulers.io())
                .flatMap(new Function<Long, ObservableSource<Integer>>() {
                    @Override
                    public ObservableSource<Integer> apply(@NonNull Long aLong) throws Exception {
                        int[] doorState = BVMManager.doorState();
                        //doorState[0] < 0:异常；doorState[1] == 2:开门
                        //BVMManager.errorMsg(doorState[0]);
                        //todo 这里如果抛异常该如何处理
                        if (doorState[0] < 0) {//异常
                            //清除错误
                            BVMManager.faultClean();
                            return Observable.error(new RxException(MACHINE_ERROR, BVMManager.errorMsg(doorState[0])));
                        } else {
                            if (doorState[1] == 1) {
                                BVMManager.faultClean();
                                String[] faultQuery = BVMManager.faultQuery();
                                if (TextUtils.isEmpty(faultQuery[0])) {
                                    //无错误信息(已取货)
                                    return Observable.just(PICKUP);
                                } else if (faultQuery[0].contains("40FA")) {
                                    //货斗有货
                                    BVMManager.openDoorAgain();
                                    return Observable.just(NO_PICKUP);
                                } else {
                                    //提交设备错误信息
                                    int[] temp = BVMManager.currentTemp();
                                    String[] error = faultQuery[0].split(":");
                                    if (error.length > 1) {
                                        ApiManager.termStatus(temp[0], error[0], error[1]);
                                    }
                                    return Observable.error(new RxException(MACHINE_ERROR, faultQuery[0]));
                                }
                            }
                            return Observable.just(NO_PICKUP);
                        }
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new RxProgress<Integer>() {
                    @Override
                    protected void onOverSubscribe(Disposable d) {
                        mCompositeDisposable.add(d);
                    }

                    @Override
                    protected void onOverNext(Integer source) {
                        if (source == PICKUP) {//已取货
                            mCompositeDisposable.clear();
                            setStatePayment(SUCCESS, true, "");
                        } else if (source == MACHINE_ERROR) {//机器错误
                            mCompositeDisposable.clear();
                            setStatePayment(FAIL, true, "未检测到取货信息");
                        }
                    }

                    @Override
                    protected void onOverError(int code, String msg) {
                        mCompositeDisposable.clear();
                        setStatePayment(FAIL, true, msg);
                    }
                });
    }

    public void onDestroy() {
        if (mCompositeDisposable != null) {
            mCompositeDisposable.clear();
        }
        if (cardManager != null) {
            cardManager.close();
        }
        presenter.cancelRequest();
    }

    //关闭按钮点击
    @Override
    public void onClose() {
        dismiss();
    }

    //确定结果关闭按钮点击
    @Override
    public void onConfirmClose() {
        dismiss();
    }

    @Override
    public void onChange(String model) {
        createQRCode(model);
    }


    public interface OnPayDialogListener {
        /**
         * 返回结果
         *
         * @param funFlag
         */
        void onPayDialog(int funFlag);

        /**
         * 刷新视图
         */
        void onNotifyView(GoodsBean goodsBean);
    }

    public void setOnPayDidlogListener(OnPayDialogListener listener) {
        mListener = listener;
    }

}
