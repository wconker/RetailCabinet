package com.sovell.retail_cabinet.ui;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.RemoteException;
import android.text.TextUtils;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.sovell.retail_cabinet.R;
import com.sovell.retail_cabinet.base.BaseActivity;
import com.sovell.retail_cabinet.bean.BannerAD;
import com.sovell.retail_cabinet.bean.GoodsBean;
import com.sovell.retail_cabinet.bean.ProdBean;
import com.sovell.retail_cabinet.bean.TermSignIn;
import com.sovell.retail_cabinet.bean.TypeBean;
import com.sovell.retail_cabinet.glide.GlideUtil;
import com.sovell.retail_cabinet.https.RxProgress;
import com.sovell.retail_cabinet.manager.ActivityCollector;
import com.sovell.retail_cabinet.manager.ApiManager;
import com.sovell.retail_cabinet.manager.BVMManager;
import com.sovell.retail_cabinet.manager.DBManager;
import com.sovell.retail_cabinet.presenter.impl.MainPresenterImpl;
import com.sovell.retail_cabinet.receiver.NetReceiver;
import com.sovell.retail_cabinet.service.MainService;
import com.sovell.retail_cabinet.service.ProtectService;
import com.sovell.retail_cabinet.utils.BusinessHelpUtil;
import com.sovell.retail_cabinet.utils.ConfigUtil;
import com.sovell.retail_cabinet.utils.FormatUtil;
import com.sovell.retail_cabinet.utils.UsbPermission;
import com.sovell.retail_cabinet.widget.CustomToast;
import com.sovell.retail_cabinet.widget.DefaultTitle;
import com.sovell.retail_cabinet.widget.InputDialog;
import com.sovell.retail_cabinet.widget.RingDialog;
import com.tbruyelle.rxpermissions2.Permission;
import com.tbruyelle.rxpermissions2.RxPermissions;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.OnClick;
import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.functions.Predicate;
import io.reactivex.schedulers.Schedulers;

public class SplashActivity extends BaseActivity {
    private Handler mHandler;
    private Disposable mDisposable;
    private ArrayList<String> CV_Images;
    @BindView(R.id.retail_img)
    ImageView retailImg;
    @BindView(R.id.cv_book_start)
    TextView cv_book_start;
    @BindView(R.id.cv_pick_time)
    TextView cv_pick_time;
    @BindView(R.id.cv_the_screen)
    LinearLayout cv_the_screen;
    @BindView(R.id.cv_box)
    FrameLayout cv_box;
    @BindView(R.id.cv_book_status)
    TextView cv_book_status;
    @BindView(R.id.cv_book_img)
    ImageView cv_book_img;
    @BindView(R.id.main_def_title)
    DefaultTitle mDefaultTitle;
    private InputDialog mInputDialog;
    private final int EndOfTheBooking = 11; //结束状态（表示机器不可用）
    RingDialog ringDialog;
    private UsbPermission usbPermission;
    private int mInterval;
    private String mAuthKey;
    private CompositeDisposable mDisposableKeep;


    public static void openActivity(Activity activity) {
        Intent intent = new Intent(activity, SplashActivity.class);
        activity.startActivity(intent);
    }

    @Override
    public int getLayoutId() {
        return R.layout.activity_splash;
    }

    @Override
    public void initPresenter() {

        mHandler = new Handler();
        mDisposable = new RxPermissions(this)
                .requestEach(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .subscribe(new Consumer<Permission>() {
                    @Override
                    public void accept(Permission permission) throws Exception {
                        if (permission.granted) {
                            mHandler.postDelayed(mSplashRunnable, 1000);
                        } else {
                            ActivityCollector.Instance().finishAllActivity();
                        }
                    }
                });

    }

    @Override
    public void initView(Bundle savedInstanceState) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            IntentFilter filter = new IntentFilter();
            filter.addAction(NetReceiver.ACTION);
            NetReceiver netReceiver = new NetReceiver();
            registerReceiver(netReceiver, filter);
        }

        GlideUtil.Instance().clearMemoryCache(this);

        GlideUtil.clearDiskCache(this);

        CV_Images = new ArrayList<>();

        ringDialog = new RingDialog(this);

        usbPermission = new UsbPermission(this);

        mInputDialog = new InputDialog(this);

        mDisposableKeep = new CompositeDisposable();

        mAuthKey = ConfigUtil.Instance().getString(ConfigUtil.AUTH_KEY);

        mInterval = ConfigUtil.Instance().getInteger(ConfigUtil.KEEP_INTERVAL);

        mInputDialog.setOnInputDialogListener(new InputDialog.OnInputDialogListener() {
            @Override
            public void onInputDialogContent(int inputMode, String content) {
                if (TextUtils.equals(content, ConfigUtil.ENTER_SET_PWD)) {
                    ActivityCollector.Instance().finishAllActivity();
                    SetActivity.openActivity(SplashActivity.this);
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

        intervalKeep();

    }


    //判断有没有保存过设备类别
    private void ValidationTerminal() {
        if (ConfigUtil.Instance().getInteger(ConfigUtil.CV_TYPE) == ConfigUtil.CETAILDISK) {
            retailImg.setVisibility(View.VISIBLE);
            cv_box.setVisibility(View.GONE);
        } else if (ConfigUtil.Instance().getInteger(ConfigUtil.CV_TYPE) == ConfigUtil.CVDISK) {
            cv_box.setVisibility(View.VISIBLE);
            retailImg.setVisibility(View.GONE);
            setInitView(null);
        } else {
            CustomToast.show("请选择终端类型");
            BasicConfigActivity.openActivity(SplashActivity.this, false);
            ActivityCollector.Instance().finishActivity(SplashActivity.this);
        }
    }

    //预订结束提示框
    boolean checkBookStatus() {
        if (ConfigUtil.Instance().getInteger(ConfigUtil.CV_PICKUP_STATUS) == EndOfTheBooking) {
            showRingDialog(true);
            return true;
        }
        return false;
    }

    private void showRingDialog(boolean mIsStatusFinish) {
        if (ringDialog.isShowing())
            ringDialog.dismiss();
        ringDialog.show();
        //显示结束预订或者不在预订范围内提示
        if (mIsStatusFinish) {
            ringDialog.setBookFinish();
        } else {
            ringDialog.setBookDuringTime();
        }

    }

    @OnClick({R.id.cv_the_screen, R.id.screen_img})
    public void onBoxTouch() {
        SelectPageToEnter();
    }

    //--------------------------------------------开屏配置检测-------------------------------------
    //根据时间判断选择进入的页面
    private void SelectPageToEnter() {
        if (!ConfigUtil.Instance().getString(ConfigUtil.CV_CurrentTime).isEmpty()) {
            //预订时间进入预定界面
            if (BusinessHelpUtil.bookTime()) {
                if (checkBookStatus()) return;
                //   ActivityCollector.Instance().finishActivity(SplashActivity.this);
                CVMainActivity.openActivity(SplashActivity.this, CV_Images, CVMainActivity.BOOKING);
            } else if (BusinessHelpUtil.tickTime()) {
                // ActivityCollector.Instance().finishActivity(SplashActivity.this);
                CVMainActivity.openActivity(SplashActivity.this, CV_Images, CVMainActivity.PICKING);
            } else {
                showRingDialog(false);
            }
        }
    }

    //初始化的时候显示and从sign登录后回掉中显示.
    private void setInitView(TermSignIn termSignIn) {

        String booking = "", picking = "";
        //结束预订以后不给予操作，只有到了时间才可以操作
        //只有当预订状态为11并且不在取货时间范围内改变状态
        if (ConfigUtil.Instance().getInteger(ConfigUtil.CV_PICKUP_STATUS) == EndOfTheBooking
                && !BusinessHelpUtil.tickTime()) {
            cv_book_img.setImageDrawable(getResources().getDrawable(R.drawable.ic_bottom_btn_gray));
        } else {
            cv_book_img.setImageDrawable(getResources().getDrawable(R.drawable.ic_bottom_btn_red));
        }

        if (termSignIn == null) {
            //预订时间
            booking = BusinessHelpUtil.getBookingTime();
            //取货时间
            picking = BusinessHelpUtil.getTickTime();
        } else {
            //预订时间
            booking = termSignIn.getTime_json().getBook_time_st() + "-"
                    + termSignIn.getTime_json().getBook_time_et();
            //取货时间
            picking = termSignIn.getTime_json().getTake_time_st() + "-"
                    + termSignIn.getTime_json().getTake_time_et();
        }

        //预订时间
        cv_book_start.setText(booking);
        //取货时间
        cv_pick_time.setText(picking);

    }


    private Runnable mSplashRunnable = new Runnable() {
        @Override
        public void run() {
            startService(new Intent(SplashActivity.this, MainService.class));
            startService(new Intent(SplashActivity.this, ProtectService.class));
            if (ConfigUtil.Instance().getBoolean(ConfigUtil.BIND)) {
                if (ConfigUtil.Instance().getBoolean(ConfigUtil.CABINET_INIT)) {
                    termSignIn();
                } else {
                    SetActivity.openActivity(SplashActivity.this);
                    ActivityCollector.Instance().finishActivity(SplashActivity.this);
                }
            } else {
                BasicConfigActivity.openActivity(SplashActivity.this, false);
                ActivityCollector.Instance().finishActivity(SplashActivity.this);
            }
        }
    };

    //净菜柜统一参数设置
    private void saveCVConfig(TermSignIn termSignIn) {
        ConfigUtil.Instance().saveString(ConfigUtil.CV_BOOKING_START_TIME, termSignIn.getTime_json().getBook_time_st());
        ConfigUtil.Instance().saveString(ConfigUtil.CV_END_OF_RESERVATION_TIME, termSignIn.getTime_json().getBook_time_et());
        ConfigUtil.Instance().saveString(ConfigUtil.CV_MEAL_PICKUP_BEGINS, termSignIn.getTime_json().getTake_time_st());
        ConfigUtil.Instance().saveString(ConfigUtil.CV_END_OF_MEAL_PICKUP, termSignIn.getTime_json().getTake_time_et());
        ConfigUtil.Instance().saveInteger(ConfigUtil.CV_PICKUP_STATUS, termSignIn.getTime_json().getBookstate());
        ConfigUtil.Instance().saveString(ConfigUtil.CV_CurrentTime, termSignIn.getStime());
    }

    //-----------------------首页网络请求部分----------------------------------
    private void termSignIn() {
        baseShow();
        LoginRequest()
                .flatMap(new Function<TermSignIn, ObservableSource<ProdBean>>() {
                    @Override
                    public ObservableSource<ProdBean> apply(TermSignIn termSignIn) throws Exception {
                        //更新本地库存数据库数据
                        return ApiManager.prodList()
                                .doOnNext(new Consumer<ProdBean>() {
                                    @Override
                                    public void accept(ProdBean prodBean) throws Exception {
                                        if (prodBean.getList() != null) {
                                            List<GoodsBean> goodsList = DBManager.findAll();
                                            for (GoodsBean goodsBean : goodsList) {
                                                boolean isExist = false;
                                                for (TypeBean typeBean : prodBean.getList()) {
                                                    for (GoodsBean bean : typeBean.getProds()) {
                                                        if (TextUtils.equals(goodsBean.getProdid(), bean.getProdid())) {
                                                            goodsBean.setProdno(bean.getProdno());
                                                            goodsBean.setCatename(bean.getCatename());
                                                            goodsBean.setCateno(bean.getCateno());
                                                            goodsBean.setCateid(bean.getCateid());
                                                            goodsBean.setProdname(bean.getProdname());
                                                            goodsBean.setDesc(bean.getDesc());
                                                            goodsBean.setPinyin(bean.getPinyin());
                                                            goodsBean.setPrice(bean.getPrice());
                                                            goodsBean.setUnit(bean.getUnit());
                                                            goodsBean.setStock_threshold(bean.getStock_threshold());
                                                            DBManager.updateById(goodsBean);
                                                            isExist = true;
                                                        }
                                                    }
                                                }
                                                if (!isExist) {
                                                    goodsBean.setProdid("");
                                                    goodsBean.setStock(0);
                                                    goodsBean.setProdno("");
                                                    goodsBean.setCatename("");
                                                    goodsBean.setCateno("");
                                                    goodsBean.setCateid("");
                                                    goodsBean.setProdname("");
                                                    goodsBean.setDesc("");
                                                    goodsBean.setPinyin("");
                                                    goodsBean.setPrice(0);
                                                    goodsBean.setUnit("");
                                                    goodsBean.setStock_threshold(0);
                                                    DBManager.updateById(goodsBean);
                                                }
                                            }
                                        }
                                    }
                                });
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new RxProgress<ProdBean>() {
                    @Override
                    protected void onOverSubscribe(Disposable d) {
                    }

                    @Override
                    protected void onOverNext(ProdBean prodBean) {
                        getBannerAD();
                    }

                    @Override
                    protected void onOverError(int code, String msg) {
                        getBannerAD();
                    }
                });
    }


    private Observable<TermSignIn> LoginRequest() {
        return ApiManager.termSignIn()
                .observeOn(AndroidSchedulers.mainThread()).
                        flatMap(new Function<TermSignIn, ObservableSource<TermSignIn>>() {
                            @Override
                            public ObservableSource<TermSignIn> apply(TermSignIn signIn) throws Exception {
                                ConfigUtil.Instance().saveString(ConfigUtil.TERM_NAME, signIn.getShop_name());
                                ConfigUtil.Instance().saveString(ConfigUtil.AUTH_KEY, signIn.getAuthkey());
                                ConfigUtil.Instance().saveString(ConfigUtil.PHONE, signIn.getOper_json().getName());
                                ConfigUtil.Instance().saveInteger(ConfigUtil.COLD_MAX, signIn.getTemp_json().getColdmax());
                                ConfigUtil.Instance().saveInteger(ConfigUtil.COLD_MIN, signIn.getTemp_json().getColdmin());
                                ConfigUtil.Instance().saveInteger(ConfigUtil.KEEP_INTERVAL, signIn.getInterval());
                                ConfigUtil.Instance().saveString(ConfigUtil.AUTH_KEY, signIn.getAuthkey());
                                //构造passtoken需要用到的
//                        ConfigUtil.Instance().saveString(ConfigUtil.PASS_CLIENT_ID, qrpay.length > 2 ? qrpay[1] + ":" + qrpay[2] : "");
//                        ConfigUtil.Instance().saveString(ConfigUtil.PASS_CLIENT_SECRET, qrpay.length > 2 ? qrpay[2] : "");
                                mInterval = ConfigUtil.Instance().getInteger(ConfigUtil.KEEP_INTERVAL);
                                mAuthKey = ConfigUtil.Instance().getString(ConfigUtil.AUTH_KEY);

                                //保存净菜柜配置
                                saveCVConfig(signIn);

                                FormatUtil.setDifTime(signIn.getStime());

                                ValidationTerminal();
                                //温度控制
                                OpenTemperatureControl(signIn);

                                return Observable.just(signIn);
                            }
                        });
    }


    private void ReLogin() {

        LoginRequest().subscribe(new Observer<TermSignIn>() {
            @Override
            public void onSubscribe(Disposable d) {
                mDisposableKeep.add(d);
            }

            @Override
            public void onNext(TermSignIn signIn) {

            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onComplete() {

            }
        });
    }

    /**
     * 终端心跳
     */
    public void intervalKeep() {

        stopKeepHeart();

        Observable.interval(mInterval, TimeUnit.MINUTES)
                .observeOn(Schedulers.io())
                .flatMap(new Function<Long, ObservableSource<TermSignIn>>() {
                    @Override
                    public ObservableSource<TermSignIn> apply(@NonNull Long aLong) throws Exception {

                        return ApiManager.termKeep(mAuthKey);
                    }
                })
                .retry(new Predicate<Throwable>() {
                    @Override
                    public boolean test(@NonNull Throwable throwable) throws Exception {
                        return true;
                    }
                })
                .flatMap(new Function<TermSignIn, ObservableSource<TermSignIn>>() {
                    @Override
                    public ObservableSource<TermSignIn> apply(TermSignIn signIn) throws Exception {
                        if (signIn.getCode() == 31 && signIn.getSub_code() == 1) {
                            //过期重新登录
                            ReLogin();
                        }
                        return Observable.just(signIn);
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new RxProgress<TermSignIn>() {
                    @Override
                    protected void onOverSubscribe(Disposable d) {
                        mDisposableKeep.add(d);
                    }

                    @Override
                    protected void onOverNext(TermSignIn termSignIn) {
                        if (termSignIn.getCode() == 1) {

                            FormatUtil.setDifTime(termSignIn.getStime());

                            //保存当前时间用于判断预订
                            ConfigUtil.Instance().saveString(ConfigUtil.CV_CurrentTime, termSignIn.getStime());

                            //控件赋值
                            setInitView(termSignIn);

                            //保存净菜柜设置
                            saveCVConfig(termSignIn);

                            //温度控制
                            OpenTemperatureControl(termSignIn);

                        }

                    }

                    @Override
                    protected void onOverError(int code, String msg) {


                    }
                });
    }

    public void getBannerAD() {
        ApiManager.showcase()
                .subscribe(new RxProgress<BannerAD>() {
                    @Override
                    protected void onOverSubscribe(Disposable d) {

                    }

                    @Override
                    protected void onOverNext(BannerAD bannerAD) {
                        baseHide();
                        String api = ConfigUtil.Instance().getApi("");
                        ArrayList<String> images = new ArrayList<>();
                        if (!TextUtils.isEmpty(bannerAD.getShowcase())) {
                            String[] imagesArr = bannerAD.getShowcase().split(",");
                            for (String name : imagesArr) {
                                images.add(String.format("%s%s%s", api, bannerAD.getPath(), name));
                            }
                        }
                        if (ConfigUtil.Instance().getInteger(ConfigUtil.CV_TYPE) == 41) {
                            MainActivity.openActivity(SplashActivity.this, images);
                            ActivityCollector.Instance().finishActivity(SplashActivity.this);
                            cv_box.setVisibility(View.GONE);
                        } else {
                            CV_Images.addAll(images);
                            cv_box.setVisibility(View.VISIBLE);
                        }
                    }

                    @Override
                    protected void onOverError(int code, String msg) {
                        baseHide();
                        ArrayList<String> images = new ArrayList<>();
                        if (ConfigUtil.Instance().getInteger(ConfigUtil.CV_TYPE) == 41) {
                            MainActivity.openActivity(SplashActivity.this, images);
                            ActivityCollector.Instance().finishActivity(SplashActivity.this);
                            cv_box.setVisibility(View.GONE);
                        } else {
                            CV_Images.addAll(images);
                            cv_box.setVisibility(View.VISIBLE);
                        }

                    }
                });
    }


    //开启温度控制,根据接口数据判断是否开启.
    private void OpenTemperatureControl(final TermSignIn termSignIn) {
        Observable.just("").
                observeOn(Schedulers.io()).
                flatMap(new Function<String, ObservableSource<String>>() {
                    @Override
                    public ObservableSource<String> apply(String s) throws Exception {
                        try {
                            int code = BVMManager.setHeatColdModel(termSignIn.getTemp_json().getState());

                            if (!BVMManager.isSuccess(code)) {
                                return Observable.error(new Exception(BVMManager.errorMsg(code)));
                            }

                        } catch (RemoteException e) {
                            return Observable.error(new Exception("异常串口"));
                        }
                        return Observable.just("");
                    }
                }).observeOn(AndroidSchedulers.mainThread())
                .subscribe(new RxProgress<String>() {
                    @Override
                    protected void onOverSubscribe(Disposable d) {

                    }

                    @Override
                    protected void onOverNext(String s) {
                        if (termSignIn.getTemp_json().getState() == 1) {
                            setTemperatureCold(termSignIn.getTemp_json().getColdmax(), termSignIn.getTemp_json().getColdmin());
                        }
                    }

                    @Override
                    protected void onOverError(int code, String msg) {

                    }

                });

    }

    //设置制冷温度
    private void setTemperatureCold(final int codeMax, final int codeMin) {
        baseShow();
        Observable.just("")
                .observeOn(Schedulers.io())
                .flatMap(new Function<String, ObservableSource<String>>() {
                    @Override
                    public ObservableSource<String> apply(String s) throws Exception {
                        if (codeMax - codeMin >= 4) {
                            int code = BVMManager.setHeatColdModel(1);
                            if (BVMManager.isSuccess(code)) {
                                code = BVMManager.setColdModel(1); //强冷模式
                                if (BVMManager.isSuccess(code)) {
                                    code = BVMManager.setColdTemp(codeMax, codeMin);
                                    if (BVMManager.isSuccess(code)) {
                                        return Observable.just("");
                                    }
                                }
                            }
                            return Observable.just(BVMManager.errorMsg(code));
                        } else {
                            return Observable.just("温度差不能低于4℃");
                        }
                    }
                }).flatMap(new Function<String, ObservableSource<String>>() {
            @Override
            public ObservableSource<String> apply(String s) throws Exception {
                exitVindicate();
                return Observable.just(s);
            }
        })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new RxProgress<String>() {

                    @Override
                    protected void onOverSubscribe(Disposable d) {

                    }

                    @Override
                    protected void onOverNext(String s) {
                        if (s.length() > 0)
                            CustomToast.show(s);

                    }

                    @Override
                    protected void onOverError(int code, String msg) {

                    }
                });
    }

    private void exitVindicate() {
        try {
            BVMManager.initSetKey();
            BVMManager.maintainState(false);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private void stopKeepHeart() {
        if (mDisposableKeep != null) {
            mDisposableKeep.clear();
        }
    }

    @Override
    protected void onDestroy() {
        if (mDisposable != null) {
            mDisposable.dispose();
        }
        if (ringDialog != null)
            ringDialog.dismiss();

        if (mDefaultTitle != null) {
            mDefaultTitle.destroy();
        }

        stopKeepHeart();

        usbPermission.unRegistered();

        mHandler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }


}
