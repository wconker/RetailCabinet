package com.sovell.retail_cabinet.presenter.impl;

import android.text.TextUtils;

import com.sovell.retail_cabinet.bean.GoodsBean;
import com.sovell.retail_cabinet.bean.TermSignIn;
import com.sovell.retail_cabinet.https.RxProgress;
import com.sovell.retail_cabinet.manager.ApiManager;
import com.sovell.retail_cabinet.manager.BVMManager;
import com.sovell.retail_cabinet.utils.ConfigUtil;
import com.sovell.retail_cabinet.utils.FormatUtil;

import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;
import io.reactivex.functions.Predicate;
import io.reactivex.schedulers.Schedulers;

public class MainPresenterImpl {

    private int mInterval;
    private String mAuthKey;
    private CompositeDisposable mDisposableKeep;
    private CompositeDisposable mDisposableStatus;
    public MainPresenterImpl() {
        this.mAuthKey = ConfigUtil.Instance().getString(ConfigUtil.AUTH_KEY);
        this.mInterval = ConfigUtil.Instance().getInteger(ConfigUtil.KEEP_INTERVAL);
        this.mDisposableKeep = new CompositeDisposable();
        this.mDisposableStatus = new CompositeDisposable();
    }



    /**
     * 终端心跳
     */
    public void intervalKeep() {
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
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new RxProgress<TermSignIn>() {
                    @Override
                    protected void onOverSubscribe(Disposable d) {
                        mDisposableKeep.add(d);
                    }

                    @Override
                    protected void onOverNext(TermSignIn termSignIn) {
                        FormatUtil.setDifTime(termSignIn.getStime());

                        //保存当前时间用于判断预订
                        ConfigUtil.Instance().saveString(ConfigUtil.CV_CurrentTime, termSignIn.getStime());



                    }

                    @Override
                    protected void onOverError(int code, String msg) {

                    }
                });
    }



    /**
     * 上传终端状态
     */
    public void uploadTermStatus() {
        Observable.interval(10, TimeUnit.MINUTES)
                .observeOn(Schedulers.io())
                .flatMap(new Function<Long, ObservableSource<Long>>() {
                    @Override
                    public ObservableSource<Long> apply(@NonNull Long aLong) throws Exception {
                        int[] temp = BVMManager.currentTemp();
                        String code = "", msg = "";
                        String[] faultArr = BVMManager.faultQuery();
                        if (faultArr.length > 0) {
                            String[] error = faultArr[0].split(":");
                            if (error.length >= 2 && !TextUtils.equals(error[0], "-1")) {
                                code = error[0];
                                msg = error[1];
                                BVMManager.faultClean();
                            }
                        }
                        ApiManager.termStatus(temp[0], code, msg);
                        return Observable.just(aLong);
                    }
                })
                .retry(new Predicate<Throwable>() {
                    @Override
                    public boolean test(@NonNull Throwable throwable) throws Exception {
                        return true;
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new RxProgress<Long>() {
                    @Override
                    protected void onOverSubscribe(Disposable d) {
                        mDisposableStatus.add(d);
                    }

                    @Override
                    protected void onOverNext(Long aLong) {
                    }

                    @Override
                    protected void onOverError(int code, String msg) {

                    }
                });
    }
    /**
     * 库存盘点
     */
    public void stockUpload(List<GoodsBean> goodsList) {
        if (goodsList.size() <= 0) {
            return;
        }
        ApiManager.stockCheck(11, goodsList)
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

    public void cancelRequestForCV(){
        if (mDisposableStatus != null) {
            mDisposableStatus.clear();
        }
        if (mDisposableKeep != null) {
            mDisposableKeep.clear();
        }
    }

    public void cancelRequest() {
        ApiManager.termSignOut(mAuthKey);
        if (mDisposableStatus != null) {
            mDisposableStatus.clear();
        }
        if (mDisposableKeep != null) {
            mDisposableKeep.clear();
        }
    }


}
