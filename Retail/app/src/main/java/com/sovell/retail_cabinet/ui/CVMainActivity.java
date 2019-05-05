package com.sovell.retail_cabinet.ui;

import android.app.Activity;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.sovell.retail_cabinet.R;
import com.sovell.retail_cabinet.base.BaseActivity;
import com.sovell.retail_cabinet.base.BaseFragment;
import com.sovell.retail_cabinet.bean.GoodsBean;
import com.sovell.retail_cabinet.bean.NetState;
import com.sovell.retail_cabinet.event.OnScreenTouchListener;
import com.sovell.retail_cabinet.fragment.CustomerOrdersInquiryFragment;
import com.sovell.retail_cabinet.fragment.EmptyFragment;
import com.sovell.retail_cabinet.fragment.MainListFragment;
import com.sovell.retail_cabinet.fragment.PickUpGoodsFragment;
import com.sovell.retail_cabinet.fragment.PreparedAndPayFragment;
import com.sovell.retail_cabinet.glide.GlideImageLoader;
import com.sovell.retail_cabinet.https.RxBus;
import com.sovell.retail_cabinet.manager.ActivityCollector;
import com.sovell.retail_cabinet.manager.BVMManager;
import com.sovell.retail_cabinet.manager.DBManager;
import com.sovell.retail_cabinet.presenter.impl.MainPresenterImpl;
import com.sovell.retail_cabinet.receiver.NetReceiver;
import com.sovell.retail_cabinet.utils.ConfigUtil;
import com.sovell.retail_cabinet.utils.UsbPermission;
import com.sovell.retail_cabinet.widget.BannerWindow;
import com.sovell.retail_cabinet.widget.CustomToast;
import com.sovell.retail_cabinet.widget.DefaultTitle;
import com.sovell.retail_cabinet.widget.InputDialog;
import com.sovell.retail_cabinet.widget.MainSearchDialog;
import com.sovell.retail_cabinet.widget.PayDialog;
import com.youth.banner.Banner;
import com.youth.banner.BannerConfig;
import com.youth.banner.Transformer;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

public class CVMainActivity extends BaseActivity {

    @BindView(R.id.cv_fragmentContainer)
    FrameLayout fragmentContainer;
    @BindView(R.id.main_container)
    FrameLayout main_container;
    @BindView(R.id.cv_view_net)
    TextView cv_view_net;
    @BindView(R.id.banner)
    Banner mBanner;
    @BindView(R.id.main_def_title)
    DefaultTitle mDefaultTitle;
    public final String MAIN = "main"; //首页列表，选材部分
    public final String CONFIRM = "confirm"; //确认订单支付部分
    public final String PICK = "pick"; //取货部分
    public final String ORDER = "order";//我的订单部分
    public static final int BOOKING = 1;//预订
    public static final int PICKING = 2;//取货
    private BannerWindow mBannerWindow;
    private MainSearchDialog mainSearchDialog;
    private PayDialog payDialog;
    private InputDialog mInputDialog;
    private MainPresenterImpl mMainPresenter;
    public List<GoodsBean> ProductsInCarts;
    /*返回结果计时*/
    private Disposable mTimerResultDisposable;
    /*倒计时关闭页面(秒)*/
    public int mInterval;
    private Disposable mDisposable;
    private Disposable mNetState;
    //将点击屏幕时事件分配给各个fragment，
    private OnScreenTouchListener onScreenTouchListener;
    //检测usb权限。不然到支付页面会崩溃
    private UsbPermission usbPermission;


    public static void openActivity(Activity activity, ArrayList<String> images, int HomeType) {
        Intent intent = new Intent(activity, CVMainActivity.class);
        intent.putStringArrayListExtra("images", images);
        intent.putExtra("HomeType", HomeType);
        activity.startActivity(intent);
    }

    @Override
    public int getLayoutId() {
        return R.layout.activity_cv_main;
    }

    @Override
    public void initPresenter() {
        mMainPresenter = new MainPresenterImpl();
        mMainPresenter.intervalKeep();
        mMainPresenter.uploadTermStatus();
        startTimer();
        new Thread(new Runnable() {
            @Override
            public void run() {
                exitVindicate();
            }
        }).start();
    }

    private void exitVindicate() {
        try {
            BVMManager.initSetKey();
         BVMManager.maintainState(false);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void initView(Bundle savedInstanceState) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            IntentFilter filter = new IntentFilter();
            filter.addAction(NetReceiver.ACTION);
            NetReceiver netReceiver = new NetReceiver();
            registerReceiver(netReceiver, filter);
        }

        // 获取退出时间参数
        mInterval = getIntervalTime();

        InitializeTheRoundRobin();

        mInputDialog = new InputDialog(this);

        mInputDialog.setOnInputDialogListener(new InputDialog.OnInputDialogListener() {
            @Override
            public void onInputDialogContent(int inputMode, String content) {
                if (TextUtils.equals(content, ConfigUtil.ENTER_SET_PWD)) {
                    ActivityCollector.Instance().finishActivity();
                    SetActivity.openActivity(CVMainActivity.this);
                } else {
                    CustomToast.show("密码错误");
                }
            }

            @Override
            public void onInputDialogPairing(String code) {

            }
        });

        mDefaultTitle.setOnLongClickTitleListener(new DefaultTitle.OnLongClickTitleListener() {
            @Override
            public void OnLongClickTitle() {
                mInputDialog.show();
                mInputDialog.initDialog(InputDialog.MODE_ENTER_SET, "");
            }
        });

        //检查一遍权限
        usbPermission = new UsbPermission(this);

        ChooseHomePage();

        //设置网络监听，没网的时候隐藏布局
        NetStateChange();

    }

    //网络改变之后改变视图 显示隐藏
    private void NetStateChange() {
        mNetState = RxBus.get().toObservable(NetState.class)
                .subscribe(new Consumer<NetState>() {
                    @Override
                    public void accept(NetState netState) throws Exception {
                        if (netState.isConnect()) {
                            cv_view_net.setVisibility(View.GONE);
                            goBackToSplash();
                        } else {
                            cv_view_net.setVisibility(View.VISIBLE);

                        }
                    }
                });
    }


    // 获取退出时间参数
    public int getIntervalTime() {
        return ConfigUtil.Instance().getExitSecond(ConfigUtil.Instance().getInteger(ConfigUtil.EXIT_TIME));
    }

    //选择进入界面的类型，预订和取货
    private void ChooseHomePage() {
        Intent intent = getIntent();
        if (intent != null) {
            int homePage = intent.getIntExtra("HomeType", 1);
            if (homePage == BOOKING) {
                //用fragment 实现切换.
                FragmentChange(MAIN, null, 0, 0, false);
            } else {
                FragmentChange(PICK, null, 0, 0, false);
            }
        }
    }


    //初始化头部轮播
    private void InitializeTheRoundRobin() {
        ArrayList<String> imageList = new ArrayList<>();
        Intent intent = getIntent();
        if (intent != null) {
            imageList = intent.getStringArrayListExtra("images");
        }
        if (imageList != null && imageList.size() > 0) {
            mBanner.setImages(imageList);
        } else {
            List<Integer> images = new ArrayList<>();
            images.add(R.drawable.ic_ad_big);
            mBanner.setImages(images);
        }
        mBanner.setImageLoader(new GlideImageLoader());
        mBanner.setDelayTime(ConfigUtil.Instance().getInteger(ConfigUtil.AD_INTERVAL) * 1000);
        mBanner.setBannerAnimation(Transformer.Accordion);
        mBanner.setBannerStyle(BannerConfig.CIRCLE_INDICATOR);
        mBanner.setIndicatorGravity(BannerConfig.CENTER);
        mBanner.start();
        mBannerWindow = new BannerWindow(this);
        mBannerWindow.setOnBannerViewListener(new BannerWindow.OnBannerViewListener() {
            @Override
            public void onDismiss() {
                startTimer();
            }
        });
    }

    /**
     * 启动定时器
     */
    public void startTimer() {
        closeTimer();
        final int countTime = mInterval; //总时间
        Observable.interval(0, 1, TimeUnit.SECONDS)
                .take(countTime + 1)//设置总共发送的次数
                .map(new Function<Long, Long>() {
                    @Override
                    public Long apply(Long aLong) throws Exception {
                        if (main_container.getVisibility() != View.VISIBLE) {
                            closeTimer();
                        }
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

                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onComplete() {//计时结束
                        closeTimer();
                        goBackToSplash();
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

    @Override
    protected void onDestroy() {
        closeTimer();

        if (mMainPresenter != null) {
            mMainPresenter.cancelRequestForCV();
        }
        if (mDisposable != null) {
            mDisposable.dispose();
        }
        if (mNetState != null) {
            mNetState.dispose();
        }

        usbPermission.unRegistered();
        super.onDestroy();
    }

    public void goBackToSplash() {
        ActivityCollector.Instance().finishActivity();
        SplashActivity.openActivity(CVMainActivity.this);
    }


    //只要点击就重置倒计时
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (main_container.getVisibility() == View.VISIBLE) {
                    startTimer();
                } else if (onScreenTouchListener != null) {
                    onScreenTouchListener.screenTouch();
                }
                break;
        }
        return super.dispatchTouchEvent(ev);
    }

    public void setOnScreenTouch(OnScreenTouchListener onScreenTouchListener) {
        this.onScreenTouchListener = onScreenTouchListener;
    }

    //------------------------------fragment 切换操作------------------------------

    //格局时间切换fragment
    public void FragmentAdd() {
        FragmentChange(MAIN, null, 0, 0, false);
    }

    /**
     * 用于预定时候
     *
     * @param fragment 目标页面
     * @param tag
     * @param isSave   是否保留主页面
     */

    public void Change(Fragment fragment, String tag, boolean isSave) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentContainer.setVisibility(View.GONE);
        main_container.setVisibility(View.GONE);
        if (fragment instanceof MainListFragment) {
            startTimer();
            fragmentTransaction.replace(R.id.cv_fragmentContainer, EmptyFragment.getInstance());
            main_container.setVisibility(View.VISIBLE);
            if (!isSave) {
                fragmentTransaction.replace(R.id.main_container, fragment, tag);
            } else {
                Fragment Main = fragmentManager.findFragmentByTag(tag);
                if (Main == null) {
                    fragmentTransaction.add(R.id.main_container, fragment, tag);
                }
            }
        } else {
            if (!usbPermission.getUsbPermissionState()) {
                usbPermission.setUsbPermissionPass();
                return;
            }
            fragmentContainer.setVisibility(View.VISIBLE);
            fragmentTransaction.replace(R.id.cv_fragmentContainer, fragment, tag);
        }
        fragmentTransaction.commit();
    }

    public void FragmentChange(String tag, List<GoodsBean> carData, int totalP, int totalC, boolean isSave) {
        BaseFragment containFragment = null;
        if (tag.equals("confirm")) {
            containFragment = PreparedAndPayFragment.getInstance();
            Bundle bundle = new Bundle();
            bundle.putSerializable("carData", (Serializable) carData);
            bundle.putInt("totalPrice", totalP);
            bundle.putInt("totalCount", totalC);
            containFragment.setArguments(bundle);
        }
        if (tag.equals(ORDER)) {
            containFragment = CustomerOrdersInquiryFragment.getInstance();
        }
        if (tag.equals(PICK)) {
            containFragment = PickUpGoodsFragment.getInstance();
        }
        if (tag.equals(MAIN)) {
            containFragment = MainListFragment.getInstance();
        }

        Change(containFragment, tag, isSave);
    }


}

