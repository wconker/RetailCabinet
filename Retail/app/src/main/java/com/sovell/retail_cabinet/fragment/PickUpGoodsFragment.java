package com.sovell.retail_cabinet.fragment;


import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.sovell.retail_cabinet.R;
import com.sovell.retail_cabinet.adapter.ShopCarDecoration;
import com.sovell.retail_cabinet.base.BaseFragment;
import com.sovell.retail_cabinet.bean.GoodsBean;
import com.sovell.retail_cabinet.bean.OrderBean;
import com.sovell.retail_cabinet.bean.OrderTakeBean;
import com.sovell.retail_cabinet.manager.CardManager;
import com.sovell.retail_cabinet.manager.PickStatusEnum;
import com.sovell.retail_cabinet.presenter.contract.PickUpContract;
import com.sovell.retail_cabinet.presenter.impl.PickPresenterImpI;
import com.sovell.retail_cabinet.widget.CustomToast;
import com.sovell.retail_cabinet.widget.PickTipsDialog;
import com.zhy.adapter.recyclerview.CommonAdapter;
import com.zhy.adapter.recyclerview.base.ViewHolder;

import java.util.ArrayList;
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

import static com.sovell.retail_cabinet.manager.PickStatusEnum.PICK_FAIL;
import static com.sovell.retail_cabinet.manager.PickStatusEnum.PICK_SHIPMENT;
import static com.sovell.retail_cabinet.manager.PickStatusEnum.PICK_SUCCESS;
import static com.sovell.retail_cabinet.manager.PickStatusEnum.PICK_WAIT;

/**
 * 提货
 */
public class PickUpGoodsFragment extends BaseFragment implements PickUpContract, CardManager.ReadCardListener {
    @BindView(R.id.cv_pickOrder)
    RecyclerView pickOrder;
    @BindView(R.id.tv_back)
    TextView tv_back;
    @BindView(R.id.cv_pick_GetYetCount)
    TextView cv_pick_GetYetCount;
    @BindView(R.id.cv_pick_finish_btn)
    TextView cv_pick_finish_btn;
    @BindView(R.id.cv_pick_tip)
    LinearLayout cv_pick_tip;
    @BindView(R.id.cv_pick_once)
    LinearLayout cv_pick_once;
    @BindView(R.id.cv_pick_finish)
    LinearLayout cv_pick_finish;
    @BindView(R.id.cv_pick_up)
    LinearLayout cv_pick_up;
    private Disposable mTimerResultDisposable;//返回结果计时
    PickPresenterImpI pickPresenterImpI;
    private List<GoodsBean> pickDatas;
    CommonAdapter<GoodsBean> pickAdapter;
    //各种状态信息提示的弹框
    private PickTipsDialog tipsDialog;
    //记录失败的商品
    private List<Integer> failGoods;

    //记录成功商品所在地索引，避免
    private List<Integer> successGoodIndex;

    private int successCount = 0;

    private int CurrentlySelectedItem = 0;

    boolean OnceFlag = false; //标记是否是一键取货
    //读卡
    private CardManager cardManager;

    private final int TODAYORDER = 10;//取当日未取货列表

    private final int STROCKFAIL = 20;//出货异常，发送服务器

    public static PickUpGoodsFragment getInstance() {
        return new PickUpGoodsFragment();
    }

    @Override
    public int getLayoutId() {
        return R.layout.fragment_pick_up_goods;
    }

    @Override
    public void initPresenter() {
        pickPresenterImpI = new PickPresenterImpI(this);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        startTimer(false);
    }

    @Override
    public void initView(View view, Bundle savedInstanceState) {
        pickDatas = new ArrayList<>();
        tipsDialog = new PickTipsDialog(mContext);
        failGoods = new ArrayList<>();
        successGoodIndex = new ArrayList<>();
        tv_back.setVisibility(View.GONE);
        initControl();
        startCardOperation(this);
    }

    void initControl() {
        pickAdapter = new CommonAdapter<GoodsBean>(mContext, R.layout.item_pick, pickDatas) {
            @Override
            protected void convert(ViewHolder holder, final GoodsBean gb, final int position) {
                if (gb.getState() == 1) {
                    holder.setVisible(R.id.cv_pick_success, false);
                    holder.setVisible(R.id.cv_error_point, false);
                    holder.setVisible(R.id.cv_pick_opt, true);
                } else if (gb.getState() == 2) {
                    holder.setVisible(R.id.cv_pick_success, true);
                    holder.setVisible(R.id.cv_error_point, false);
                    holder.setVisible(R.id.cv_pick_opt, false);
                } else {
                    holder.setVisible(R.id.cv_pick_success, false);
                    holder.setVisible(R.id.cv_error_point, true);
                    holder.setVisible(R.id.cv_pick_opt, true);
                }
                holder.setText(R.id.cv_pick_name, gb.getProdname());
                //取货按钮点击事件
                holder.setOnClickListener(R.id.cv_pick_opt, new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        //标记当前选中项目
                        CurrentlySelectedItem = position;
                        // 关闭定时
                        startTimer(false);
                        //点击后开始等待出货，显示loading
                        showLoad(PICK_SHIPMENT, "");
                        //第一次提交没有状态码
                        pickPresenterImpI.VerifyBeforeTakingDelivery(gb, TODAYORDER, "");

                    }
                });
            }
        };
        GridLayoutManager dishManager = new GridLayoutManager(mContext, 2);
        pickOrder.setLayoutManager(dishManager);
        pickOrder.addItemDecoration(new ShopCarDecoration(20));
        pickOrder.setAdapter(pickAdapter);
    }

    @OnClick({R.id.tv_back, R.id.cv_pick_finish_btn})
    void goBack() {
        //手动点击时返回有购物车的主界面
        mainActivity.FragmentChange(mainActivity.PICK, null, 0, 0, false);
    }

    //点击一键取货按钮触发
    @OnClick({R.id.cv_pick_once})
    void pickOnceBtn() {
        OnceFlag = true;
        //清空索引，避免用户先独立取货再一键取货
        CurrentlySelectedItem = 0;
        //一键取货时遇到成功的货物跳过，失败的货将再次调用出货，所以需要清空。
        failGoods.clear();
        PickOnce();
    }

    //一键取货功能
    private void PickOnce() {
        if (checkFinish()) {
            OnceFlag = false;
            ShowFinishView(failGoods.size() > 0 ? "出货失败" : "");
        } else {
            //点击后开始等待出货，显示loading
            if (CurrentlySelectedItem < pickDatas.size()) {
                showLoad(PICK_SHIPMENT, "");
                //只出货没有成功的商品，已经成功的商品进行跳过
                if (!IsThereASuccessfulProduct(CurrentlySelectedItem)) {
                    GoodsBean gb = pickDatas.get(CurrentlySelectedItem);
                    pickPresenterImpI.VerifyBeforeTakingDelivery(gb, TODAYORDER, "");
                } else {
                    //遇到已经取过的直接递归调用
                    CurrentlySelectedItem++;
                    PickOnce();
                }
            } else {
                showLoad(PICK_SUCCESS, "");
            }
        }
    }

    //是否存在已经成功的商品
    boolean IsThereASuccessfulProduct(int mCurrentlySelectedItem) {
        for (int i = 0; i < successGoodIndex.size(); i++) {
            if (successGoodIndex.get(i) == mCurrentlySelectedItem) {
                return true;
            }
        }
        return false;
    }

    //-----------------------------------------------刷卡取列表部分------------------------------------
    //获取列表成功
    @Override
    public void getOrderSuccess(OrderBean orderBean) {
        startTimer(false);
        //进入取货页面重新计时
        if (orderBean != null) {
            if (orderBean.getList().size() > 0) {
                cv_pick_up.setVisibility(View.VISIBLE);
                cv_pick_tip.setVisibility(View.GONE);
                pickDatas.addAll(pickPresenterImpI.NetValue2GoodBean(orderBean));
                pickAdapter.notifyDataSetChanged();
                InitialValueSetting();//计算已经取的个数
                cv_pick_GetYetCount.setText(String.format(Locale.CHINA,
                        "已取%d/%d份", successCount, pickDatas.size()));
                tv_back.setVisibility(View.VISIBLE);
            } else {
                CustomToast.show("无可取商品");
                cardManager.readCard();
            }
        }
    }

    //获取列表失败
    @Override
    public void getOrderFail(int code, String msg) {
        startTimer(false);
        if (code == 4) {
            CustomToast.show("无可取商品");
        } else {
            CustomToast.show(msg);
        }
        //重新打开读卡
        cardManager.readCard();
    }

    //-----------------------------------------------取货take回掉部分------------------------------------

    //取货前请求数据
    @Override
    public void orderTakeSuccess(OrderTakeBean orderTakeBean, GoodsBean orderBean) {

            if (orderTakeBean.getType() == 10) {
                //开始出货
                try {
                    Thread.sleep(400);
                    pickPresenterImpI.PickupMethod(orderBean);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                closeTimer();
            }


    }

    //取货前询问服务器是否可以取货
    @Override
    public void orderTakeFail(int code, String msg) {

        if (tipsDialog.isShowing())
            tipsDialog.dismiss();

        if (OnceFlag){
            OnceFlag = false;
            //清空索引，避免用户先独立取货再一键取货
            CurrentlySelectedItem = 0;
            //一键取货时遇到成功的货物跳过，失败的货将再次调用出货，所以需要清空。
            failGoods.clear();
        }

        CustomToast.show(msg);
    }

    //-----------------------------------------------机器调货回掉部分------------------------------------
    //等待客户取货
    @Override
    public void pickGoodWait() {
        showLoad(PICK_WAIT, "");
    }

    //每个货道的取货成功
    @Override
    public void pickGoodSuccess(String msg) {
        //累加成功的商品个数
        successCount++;
        //记录索引
        if (!IsThereASuccessfulProduct(CurrentlySelectedItem)) {
            successGoodIndex.add(CurrentlySelectedItem);
        }
        //显示成功商品个数
        cv_pick_GetYetCount.setText(String.format(Locale.CHINA,
                "已取%d/%d份", successCount, pickDatas.size()));
        //标记商品为已取状态，并且更新列表
        pickDatas.get(CurrentlySelectedItem).setState(2);
        pickAdapter.notifyDataSetChanged();
        //判断是否时点击一键取货，需要判断标志
        if (OnceFlag) {
            //自动取货当前索引值，从零开始标记，每当取货一次进行累加操作，
            CurrentlySelectedItem++;
            PickOnce();
        } else {
            //检查是否是最后一件商品了,最后一件的话显示结束界面
            if (checkFinish())
                ShowFinishView("");
            else
                showLoad(PICK_SUCCESS, msg); //成功的话不显示dialog，直接进行下一步操作
        }
    }

    //取货失败 在一键取货的情况下不提示出货失败原因，
    // 等全部循环出完以后停留在当前，手动取货
    @Override
    public void pickGoodFail(int code, String msg) {
        GoodsBean tmp;
        //标记商品为错误状态，
        tmp = pickDatas.get(CurrentlySelectedItem);
        tmp.setState(3);
        pickAdapter.notifyDataSetChanged();
        FailGoodsAdd(tmp);
        if (OnceFlag) {
            //自动取货当前索引值，从零开始标记，每当取货一次进行累加操作，
            CurrentlySelectedItem++;
            PickOnce();
        } else {
            //加入到错误列表中
            if (checkFinish()) { //检查是否是最后一件商品了,最后一件的话显示结束界面
                ShowFinishView(msg);
            } else {
                showLoad(PICK_FAIL, msg);//不成功的话显示dialog，
            }
        }
        //取货失败向服务端发送20状态码
        pickPresenterImpI.VerifyBeforeTakingDelivery(tmp, STROCKFAIL, code + "│" + msg);
    }

    //加到错误列表里。重复的不添加
    void FailGoodsAdd(GoodsBean goodsBean) {
        if (goodsBean != null && !failGoods.contains(goodsBean.getDetailid())) {
            failGoods.add(goodsBean.getDetailid());
        }
    }

    /*
    检查是不是最有一个商品出货结束了
     */
    public boolean checkFinish() {
        return failGoods.size() + successCount == pickDatas.size();
    }

    /**
     * 结束流程后显示的最后视图
     * 如果最有一个商品页出货结束了，把tip 和pick 两个视图隐藏显示finish 视图
     * 如果都没有故障的商品显示
     * 开始显示倒计时
     */
    private void ShowFinishView(String msg) {
        startTimer(false);
        if (tipsDialog.isShowing()) {
            tipsDialog.dismiss();
            if (failGoods.size() <= 0) {
                cv_pick_tip.setVisibility(View.GONE);
                cv_pick_up.setVisibility(View.GONE);
                tv_back.setVisibility(View.GONE);
                cv_pick_finish.setVisibility(View.VISIBLE);
                startTimer(true);//成功后时间改为10s
            } else if (!tipsDialog.isShowing()) {
                showLoad(PICK_FAIL, msg);
            }
        }

    }

    /***
     * 界面操作，显示各个步骤提示信息
     * @param status 状态
     * @param content 文本信息
     */
    private void showLoad(PickStatusEnum status, String content) {
        if (tipsDialog.isShowing())
            tipsDialog.dismiss();
        //除了成功的状态都进行弹框，成功的话直接显示回到页面。
        if (PICK_SUCCESS != status) {
            tipsDialog.show();
            tipsDialog.setTipState(status, content);
        }
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
    public void startTimer(final boolean finish) {
        closeTimer();
        final int countTime = finish ? 10 : mainActivity.mInterval; //总时间
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
                        if (!finish) {
                            tv_back.setText("返回(" + value + "s)");
                        } else {
                            tv_back.setText("关闭(" + value + "s)");
                            cv_pick_finish_btn.setText(String.format(Locale.CHINA, "确定（%ds）", value));
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

    @Override
    public void onReadCard(String cid) {
        pickPresenterImpI.VerificationCardInformation(cid, 1);
    }

    //获取一开始就成功的商品
    void InitialValueSetting() {
        for (int s = 0; s < pickDatas.size(); s++) {
            if (pickDatas.get(s).getState() == 2) {
                successCount++;
            }
        }
    }

    /***
     * 从activity获得的屏幕点击事件
     */
    @Override
    public void screenTouch() {
        //取货页面存在的时候才分配点击事件
        if (cv_pick_up.getVisibility() == View.VISIBLE) {
            startTimer(false);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        closeTimer();
        if (cardManager != null) {
            cardManager.close();
        }
        if (tipsDialog != null) {
            tipsDialog.dismiss();
        }
        //释放p中的资源
        pickPresenterImpI.cancelRequest();
    }

}
