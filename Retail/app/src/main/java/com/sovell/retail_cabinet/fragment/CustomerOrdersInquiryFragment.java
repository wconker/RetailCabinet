package com.sovell.retail_cabinet.fragment;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.sovell.retail_cabinet.R;
import com.sovell.retail_cabinet.base.BaseFragment;
import com.sovell.retail_cabinet.bean.OrderBean;
import com.sovell.retail_cabinet.bean.PayResultBean;
import com.sovell.retail_cabinet.manager.CardManager;
import com.sovell.retail_cabinet.manager.PayModeEnum;
import com.sovell.retail_cabinet.presenter.contract.OrderContract;
import com.sovell.retail_cabinet.presenter.contract.PayContract;
import com.sovell.retail_cabinet.presenter.impl.OrderPresenterImpl;
import com.sovell.retail_cabinet.presenter.impl.PayPresenterImpl;
import com.sovell.retail_cabinet.utils.ConfigUtil;
import com.sovell.retail_cabinet.utils.FormatUtil;
import com.sovell.retail_cabinet.widget.CustomToast;
import com.sovell.retail_cabinet.widget.SelectDialog;
import com.zhy.adapter.recyclerview.CommonAdapter;
import com.zhy.adapter.recyclerview.base.ViewHolder;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.OnClick;
import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

import static com.sovell.retail_cabinet.utils.FormatUtil.formatDay;

/**
 * A simple {@link Fragment} subclass.
 */
public class CustomerOrdersInquiryFragment extends BaseFragment implements
        CardManager.ReadCardListener, OrderContract, PayContract {


    @BindView(R.id.tv_back)
    TextView tv_back;
    @BindView(R.id.cv_inquiry_tip)
    LinearLayout cv_inquiry_tip;
    @BindView(R.id.cv_inquiry_list)
    LinearLayout cv_inquiry_list;
    @BindView(R.id.cv_inquiry_data_list)
    RecyclerView cv_inquiry_data_list;
    @BindView(R.id.no_data)
    LinearLayout noData;
    private Disposable mTimerResultDisposable;//返回结果计时
    private SelectDialog mSelectDialog;
    private CommonAdapter<OrderBean.ListBean> orderAdapter;
    //读卡
    private CardManager cardManager;
    private OrderPresenterImpl orderPresenter;
    private List<OrderBean.ListBean> beanList;
    private PayPresenterImpl payPresenter;
    private String mCardId = "";
    private String mDevTermNum;
    private final int ALLORDER = 20;
    private final int ALLOW = 1;
    private final int DISALLOW = 2;
    private final int CANCEL = 11;
    private final int PART = 3;

    //卡信息操作
    public void startCardOperation(CardManager.ReadCardListener cardListener) {
        if (cardManager == null) {
            cardManager = new CardManager();
            cardManager.setReadCardListener(cardListener);
            cardManager.open(mContext);
        }
        cardManager.readCard();
    }

    public static CustomerOrdersInquiryFragment getInstance() {
        return new CustomerOrdersInquiryFragment();
    }

    @Override
    public int getLayoutId() {
        return R.layout.fragment_customer_orders_inquiry;
    }

    @Override
    public void initPresenter() {
        payPresenter = new PayPresenterImpl(this);
        orderPresenter = new OrderPresenterImpl(this);
    }

    @Override
    public void initView(View view, Bundle savedInstanceState) {
        startTimer();
        beanList = new ArrayList<>();
        startCardOperation(this);
        initControl();
        this.mDevTermNum = ConfigUtil.Instance().getString(ConfigUtil.TERM);
        mSelectDialog = new SelectDialog(mContext);

    }


    private void initControl() {
        orderAdapter = new CommonAdapter<OrderBean.ListBean>(mContext, R.layout.item_order_content, beanList) {
            @Override
            protected void convert(ViewHolder holder, final OrderBean.ListBean listBean, int position) {
                StringBuilder cName = new StringBuilder();
                final int GoodCount = listBean.getProds().size();
                holder.setText(R.id.cv_show_day, FormatUtil.formatDay(listBean.getTake_date()));
                //显示头部日期，第一个则直接显示，从第二个开始和上一个进行比对，相同则不显示
                if (position == 0) {
                    holder.setVisible(R.id.cv_show_day, true);
                } else {
                    //先判断长度，避免索sub引异常
                    if (listBean.getTake_date().length() > 10 && beanList.get(position - 1)
                            .getTake_date().length() > 10 && listBean.getTake_date().substring(0, 10).equals(beanList.get(position - 1)
                            .getTake_date().substring(0, 10))) {
                        holder.setVisible(R.id.cv_show_day, false);
                    } else {
                        holder.setVisible(R.id.cv_show_day, true);
                    }
                }
                //拼接名称
                for (int i = 0; i < GoodCount; i++) {
                    cName.append(listBean.getProds().get(i).getProdname()).append(',');
                }

                //去掉最后一个逗号
                if (cName.toString().endsWith(",")) {
                    cName.deleteCharAt(cName.length() - 1);
                }

                holder.setText(R.id.cv_order_name, cName.toString())
                        .setText(R.id.cv_order_date, CharacterFormat(listBean.getTake_date()))
                        .setText(R.id.cv_order_count, GoodCount + "");
                //2为已取货状态，1为未取货可以撤单，3为已取消
                if (listBean.getState() == DISALLOW) {
                    holder.setVisible(R.id.cv_order_opt_revoke, true);
                    holder.setVisible(R.id.cv_order_opt, false);
                } else if (listBean.getState() == ALLOW) {
                    holder.setVisible(R.id.cv_order_opt_revoke, false);
                    holder.setVisible(R.id.cv_order_opt, true);
                } else if (listBean.getState() == CANCEL) {
                    holder.setVisible(R.id.cv_order_opt_revoke, true);
                    holder.setVisible(R.id.cv_order_opt, false);
                    holder.setText(R.id.cv_order_opt_revoke, "已取消");
                } else if (listBean.getState() == PART) {
                    holder.setVisible(R.id.cv_order_opt_revoke, true);
                    holder.setVisible(R.id.cv_order_opt, false);
                    holder.setText(R.id.cv_order_opt_revoke, "部分取货");
                }

                holder.setOnClickListener(R.id.cv_order_opt, new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        mSelectDialog.setOnClickSelectDialogListener(new SelectDialog.OnClickSelectDialogListener() {
                            @Override
                            public void OnClickSelectDialog(boolean isClickSure, int mode) {
                                if (isClickSure) {
                                    String mInvoiceNum = mDevTermNum + String.valueOf(System.currentTimeMillis());
                                    payPresenter.orderRefund(mInvoiceNum,
                                            "手动撤单",
                                            0,
                                            listBean.getAmt(),
                                            String.valueOf(listBean.getSeq()));
                                }
                            }
                        });

                        mSelectDialog.show();
                        mSelectDialog.setContent(0, "确定要撤销该订单？");
                    }
                });
            }
        };
        cv_inquiry_data_list.setAdapter(orderAdapter);
        cv_inquiry_data_list.setLayoutManager(new LinearLayoutManager(mContext));
    }

    @OnClick(R.id.tv_back)
    void goBack() {
        //返回保留数据
        mainActivity.FragmentChange(mainActivity.MAIN, null, 0, 0, true);
    }


    //判断时间格式，避免字符剪切时索引异常
    String CharacterFormat(String v) {
        if (v.length() > 11) {
            return v.substring(11, v.length());
        }
        return "时间格式异常";
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
     * 启动定时器
     */
    public void startTimer() {
        closeTimer();
        final int countTime = mainActivity.mInterval; //总时间
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
                        tv_back.setText("返回(" + value + "s)");
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

    @Override
    public void onReadCard(String cid) {
        mCardId = cid;
        orderPresenter.VerificationCardInformation(ALLORDER, cid, 1);

    }

    @Override
    public void onDestroy() {
        closeTimer();
        if (cardManager != null) {
            cardManager.close();
        }

        if (payPresenter != null)
            payPresenter.cancelRequest();

        if (orderPresenter != null)
            orderPresenter.cancelRequest();

        if (mSelectDialog != null)
            mSelectDialog.dismiss();
        super.onDestroy();
    }

    @Override
    public void getOrderSuccess(OrderBean orderBean) {
        beanList.clear();
        if (orderBean != null && orderBean.getList() != null) {
            cv_inquiry_tip.setVisibility(View.GONE);
            if (orderBean.getList().size() > 0) {
                beanList.addAll(orderBean.getList());
                orderAdapter.notifyDataSetChanged();
                cv_inquiry_list.setVisibility(View.VISIBLE);
                noData.setVisibility(View.GONE);
            } else {
                noData.setVisibility(View.VISIBLE);
                cv_inquiry_list.setVisibility(View.GONE);
            }
        } else {
            CustomToast.show("操作异常，请重试！");
        }
        startTimer();
    }

    @Override
    public void getOrderFail(int code, String msg) {
        CustomToast.show("操作异常，请重试！");
        startTimer();
    }

    @Override
    public void onGetCardSuccess() {

    }

    @Override
    public void onGetCardFailed(int code, String msg) {

    }

    @Override
    public void onPaySuccess() {

    }

    @Override
    public void onEmptyCargo() {

    }

    @Override
    public void shipmentSuccess(PayResultBean payResultBean) {

    }

    @Override
    public void onTradeFailed(int code, String msg, int amt, boolean isPay) {
        CustomToast.show(msg);
        if (!mCardId.isEmpty()) {
            orderPresenter.VerificationCardInformation(ALLORDER, mCardId, 1);
        }

    }

    @Override
    public void onSweepCodeSuccess(String status, String msg, String payMode) {

    }

    @Override
    public void onPassPaySuccess(String invoices, String amt, PayModeEnum payCode) {

    }

    @Override
    public void screenTouch() {
        startTimer();
    }
}
