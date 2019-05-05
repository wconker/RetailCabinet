package com.sovell.retail_cabinet.fragment;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.sovell.retail_cabinet.R;
import com.sovell.retail_cabinet.base.BaseFragment;
import com.sovell.retail_cabinet.bean.GoodsBean;
import com.sovell.retail_cabinet.bean.PayResultBean;
import com.sovell.retail_cabinet.event.OnPayDialogListener;
import com.sovell.retail_cabinet.glide.GlideUtil;
import com.sovell.retail_cabinet.glide.RoundTransformation;
import com.sovell.retail_cabinet.https.HttpsAddress;
import com.sovell.retail_cabinet.manager.CardManager;
import com.sovell.retail_cabinet.manager.PayModeEnum;
import com.sovell.retail_cabinet.manager.PayStatusEnum;
import com.sovell.retail_cabinet.presenter.contract.PayContract;
import com.sovell.retail_cabinet.presenter.impl.PayPresenterImpl;
import com.sovell.retail_cabinet.utils.ConfigUtil;
import com.sovell.retail_cabinet.utils.FormatUtil;
import com.sovell.retail_cabinet.utils.ParamsUtil;
import com.sovell.retail_cabinet.widget.PaymentForCV;
import com.zhy.adapter.recyclerview.CommonAdapter;
import com.zhy.adapter.recyclerview.base.ViewHolder;

import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.OnClick;
import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

import static com.sovell.retail_cabinet.manager.PayStatusEnum.BY_CARD;
import static com.sovell.retail_cabinet.manager.PayStatusEnum.FAIL;
import static com.sovell.retail_cabinet.manager.PayStatusEnum.IN_PAYMENT;
import static com.sovell.retail_cabinet.manager.PayStatusEnum.SUCCESS;

/**
 * A simple {@link Fragment} subclass.
 */
public class PreparedAndPayFragment extends BaseFragment implements CardManager.ReadCardListener,
        PayContract, PaymentForCV.OnCloseListenerForCV {
    @BindView(R.id.tv_back)
    TextView tv_back;
    @BindView(R.id.cv_confirm_totalCommodityCount)
    TextView confirm_totalCommodityCount;
    @BindView(R.id.cv_confirm_totalCommodityPrice)
    TextView confirm_totalCommodityPrice;
    @BindView(R.id.cv_pay_msg)
    TextView pay_msg;
    @BindView(R.id.tv_pay_price)
    TextView pay_price;
    @BindView(R.id.cv_confirmOrder)
    RecyclerView confirmOrder;
    @BindView(R.id.cv_confirm_settlement)
    LinearLayout confirm_settlement;
    @BindView(R.id.ll_pay_by_card)
    LinearLayout llPayByCard;
    @BindView(R.id.cv_progress)
    FrameLayout cv_progress;
    @BindView(R.id.cv_payBox)
    LinearLayout payBox;
    @BindView(R.id.cv_card_tips)
    ImageView cv_card_tips;
    @BindView(R.id.cv_prepareBox)
    LinearLayout prepareBox;
    @BindView(R.id.paymentView)
    PaymentForCV paymentView;
    private RoundTransformation mTransformation;
    private String mApiAddress;
    //返回结果计时
    private Disposable mTimerResultDisposable;
    //菜品计数
    private int TotalCount = 0;
    //总价计数
    private int TotalPrice = 0;
    private PayPresenterImpl presenter;
    private volatile String mInvoiceNum = ConfigUtil.Instance().getString(ConfigUtil.TERM) + String.valueOf(System.currentTimeMillis());
    private String mDevTermNum;
    //购买的商品列表
    List<GoodsBean> carData;
    //读卡
    private CardManager cardManager;


    private int failMsgShowTime = 0;

    public static PreparedAndPayFragment getInstance() {
        return new PreparedAndPayFragment();
    }


    @Override
    public void initView(View view, Bundle savedInstanceState) {
        if (getArguments() != null) {
            carData = (List<GoodsBean>) getArguments().getSerializable("carData");
            TotalCount = getArguments().getInt("totalCount");
            TotalPrice = getArguments().getInt("totalPrice");
        }

        if (carData == null || TotalCount == 0 || TotalPrice == 0) {
            goBack();
        }

        confirm_totalCommodityCount.setText(String.format(Locale.CHINA, "共%d份，", TotalCount));
        confirm_totalCommodityPrice.setText(String.format(Locale.CANADA, "￥%s",
                FormatUtil.div(String.valueOf(TotalPrice), "100")));

        startTimer(BY_CARD);

        this.mDevTermNum = ConfigUtil.Instance().getString(ConfigUtil.TERM);

        paymentView.setOnCloseListener(this);

        initControl();

    }

    @Override
    public int getLayoutId() {
        return R.layout.fragment_prepared_pay;
    }

    @Override
    public void initPresenter() {
        presenter = new PayPresenterImpl(this);
    }

    /***
     * 初始化列表适配器等
     */
    public void initControl() {
        mTransformation = new RoundTransformation(8, RoundTransformation.CornerType.TOP);
        String api = ConfigUtil.Instance().getApi("");
        String shop = ConfigUtil.Instance().getString(ConfigUtil.SHOP);
        this.mApiAddress = String.format("%s%s?shop=%s&id=", api, HttpsAddress.PROD_PICTURE, shop);
        confirmOrder.setAdapter(new CommonAdapter<GoodsBean>(mContext,
                R.layout.item_confirm_order,
                this.carData) {
            @Override
            protected void convert(ViewHolder holder, GoodsBean goodsBean, int position) {
                ImageView iv = holder.getView(R.id.cv_confirm_img);
                String url = String.format(Locale.CHINA, "%s%s",
                        mApiAddress,
                        goodsBean.getProdid());
                GlideUtil.Instance().loadRoundImage(mContext, url, iv, mTransformation);

                holder.setText(R.id.cv_confirm_name, goodsBean.getProdname())
                        .setText(R.id.cv_confirm_price, String.format(Locale.CANADA, "￥%s",
                                FormatUtil.div(String.valueOf(goodsBean.getPrice()), "100")))
                        .setText(R.id.cv_confirm_count, String.format(Locale.CHINA,
                                "x%d", goodsBean.getBuycount()));
            }
        });
        confirmOrder.setLayoutManager(new LinearLayoutManager(mContext));
    }


//----------------------------------------------定时器操作部分------------------------------------

    /**
     * 关闭定时器
     */
    public void closeTimer() {
        if (mTimerResultDisposable != null) {
            mTimerResultDisposable.dispose();
        }
    }

    /**
     * 启动定时器
     *
     * @param state 计时状态
     */
    public void startTimer(final PayStatusEnum state) {
        closeTimer();
        final int countTime = state == BY_CARD ? mainActivity.mInterval : 10; //总时间
        Observable.interval(0, 1, TimeUnit.SECONDS)
                .take(countTime + 1)//设置总共发送的次数
                .map(new Function<Long, Long>() {
                    @Override
                    public Long apply(Long aLong) throws Exception {

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

                        showMsgTimeSet();

                        if (state == BY_CARD) {
                            tv_back.setText("返回(" + value + "s)");
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
                        closeTimer();
                        mainActivity.goBackToSplash();
                    }
                });
    }


    //显示提示框消失和隐藏方法
    void showMsgTimeSet() {

        if (failMsgShowTime == 0 && pay_msg.getVisibility() == View.VISIBLE) {
            pay_msg.setText("");
            pay_msg.setVisibility(View.INVISIBLE);
        } else if (failMsgShowTime > 0) {
            failMsgShowTime = failMsgShowTime - 1;
        }
    }

    //----------------------------------------------按钮操作部分------------------------------------

    @OnClick(R.id.tv_back)
    void goBack() {
        //手动点击时返回有购物车的主界面
        if (prepareBox.getVisibility() == View.VISIBLE || payBox.getVisibility() == View.VISIBLE) {
            mainActivity.FragmentChange(mainActivity.MAIN, null, 0, 0, true);
        } else {
            mainActivity.FragmentChange(mainActivity.MAIN, null, 0, 0, false);
        }
    }

    @OnClick(R.id.cv_confirm_settlement)
    void goPay() {
        //跳转支付部分
        payBox.setVisibility(View.VISIBLE);

        cv_card_tips.setVisibility(View.VISIBLE);

        cv_progress.setVisibility(View.GONE);

        prepareBox.setVisibility(View.GONE);
        //倒计时重置，并开启
        mainActivity.mInterval = mainActivity.getIntervalTime();

        setPayPriceShow();

        startCardOperation(this);

        startTimer(BY_CARD);
    }


    //---------------------------------------------卡信息读取操作部分------------------------------------

    //服务端卡验证失败
    //显示错误信息，把读卡器打开设置倒计时
    @Override
    public void onGetCardFailed(int code, String msg) {
        failMsgShowTime = 3;
        pay_msg.setVisibility(View.VISIBLE);
        pay_msg.setText(msg);
        cardManager.readCard();
        startTimer(BY_CARD);
        setState(FAIL);
    }


    //卡信息操作
    public void startCardOperation(CardManager.ReadCardListener cardListener) {
        if (cardManager == null) {
            cardManager = new CardManager();
            cardManager.setReadCardListener(cardListener);
            cardManager.open(mContext);
        }
        cardManager.readCard();
    }

    //读卡成功
    @Override
    public void onReadCard(String cid) {
        closeTimer();
        setPayParameter(cid);
        cardManager.beeper();
    }

    //服务端卡验证成功
    @Override
    public void onGetCardSuccess() {
        pay_msg.setVisibility(View.INVISIBLE);
        pay_msg.setText("");
        setState(IN_PAYMENT);
    }


    /**
     * 构造商品数据格式，向后端提交
     */
    public void setPayParameter(String cid) {
        String prods = ParamsUtil.getProdsParamsForCV(carData);
        mInvoiceNum = mDevTermNum + String.valueOf(System.currentTimeMillis());
        presenter.cardGetForCV(mInvoiceNum, TotalPrice, prods, cid);
    }


    /**
     * 设置支付完成后的各个状态
     *
     * @param state 必须为失败状态
     * @param isPay 是否已支付成功
     * @param msg   显示错误信息
     */
    private void setStatePayment(PayStatusEnum state, boolean isPay, String msg) {
        payBox.setVisibility(View.GONE);
        paymentView.setSuccessVisib(View.GONE);
        paymentView.setFailVisib(View.GONE);
        paymentView.setVisibility(View.VISIBLE);
        switch (state) {
            case SUCCESS:
                //支付成功返回按钮也不显示
                tv_back.setVisibility(View.GONE);
                paymentView.setSuccessVisib(View.VISIBLE);
                paymentView.setStatePayment(state, isPay, msg, "", TotalPrice);
                startTimer(SUCCESS);
                break;
            case FAIL:
                tv_back.setVisibility(View.GONE);
                paymentView.setStatePayment(state, isPay, msg, "", TotalPrice);
                startTimer(FAIL);
                break;
        }

    }

    //---------------------------------------------支付操作部分------------------------------------

    @Override
    public void onPaySuccess() {
        setStatePayment(SUCCESS, true, "");
    }

    @Override
    public void onTradeFailed(int code, String msg, int amt, boolean isPay) {
        setStatePayment(FAIL, isPay, msg);
    }

    @Override
    public void onSweepCodeSuccess(String status, String msg, String payMode) {

    }

    @Override
    public void onPassPaySuccess(String invoices, String amt, PayModeEnum payCode) {

    }

    @Override
    public void onDestroy() {
        if (cardManager != null) {
            cardManager.close();
        }
        closeTimer();
        //释放p增重的资源
        presenter.cancelRequest();
        super.onDestroy();
    }


    @Override
    public void onEmptyCargo() {

    }

    //取货成功
    @Override
    public void shipmentSuccess(PayResultBean payResultBean) {
    }


    //成功失败界面按钮实践
    @Override
    public void onConfirmClose(int id) {
        if (id == R.id.tv_fail_repay) {
            //返回订单支付
            if (cardManager != null) {
                cardManager.readCard();
            } else {
                startCardOperation(this);
            }
            prepareBox.setVisibility(View.VISIBLE);
            payBox.setVisibility(View.GONE);
            paymentView.setFailVisib(View.GONE);
            pay_msg.setVisibility(View.INVISIBLE);
        } else {
            goBack();
        }

    }

    /**
     * 设置支付的各个状态
     */
    public void setState(PayStatusEnum state) {
        llPayByCard.setVisibility(View.GONE);
        cv_progress.setVisibility(View.GONE);
        cv_card_tips.setVisibility(View.VISIBLE);
        switch (state) {
            //卡片余额不足的情况需要在付款页面停留
            case FAIL:
                llPayByCard.setVisibility(View.VISIBLE);
                paymentView.setVisibility(View.GONE);
                paymentView.setSuccessVisib(View.GONE);
                paymentView.setFailVisib(View.GONE);
                payBox.setVisibility(View.VISIBLE);
                break;
            case BY_CARD:
                payBox.setVisibility(View.VISIBLE);
                llPayByCard.setVisibility(View.VISIBLE);
                break;
            case IN_PAYMENT:
                llPayByCard.setVisibility(View.VISIBLE);
                payBox.setVisibility(View.VISIBLE);
                cv_progress.setVisibility(View.VISIBLE);
                cv_card_tips.setVisibility(View.GONE);
                break;
        }
    }

    //显示支付的金额
    public void setPayPriceShow() {
        pay_price.setText("￥" + FormatUtil.div(String.valueOf(TotalPrice), "100"));
    }

    /***
     * 从activity获得的屏幕点击事件
     * 订单预览页面和支付页面显示的时候就开始点击启动定时
     */
    @Override
    public void screenTouch() {
        //当刷卡支付页面显示的时候，执行点击重置
        if (prepareBox.getVisibility() == View.VISIBLE || payBox.getVisibility() == View.VISIBLE) {
            startTimer(BY_CARD);
        }
    }
}
