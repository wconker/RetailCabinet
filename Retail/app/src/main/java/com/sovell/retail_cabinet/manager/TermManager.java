package com.sovell.retail_cabinet.manager;

import android.text.TextUtils;

import com.sovell.retail_cabinet.bean.TermSignIn;
import com.sovell.retail_cabinet.bean.TimeBean;
import com.sovell.retail_cabinet.https.RxBus;
import com.sovell.retail_cabinet.https.RxProgress;
import com.sovell.retail_cabinet.utils.FormatUtil;

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

public class TermManager {

    private TimeBean mTimeBean;
    private CompositeDisposable mDisposable;

    private static class TermManagerHolder {
        private static final TermManager INSTANCE = new TermManager();
    }

    private TermManager() {
        mTimeBean = new TimeBean();
        mDisposable = new CompositeDisposable();
    }

    public static TermManager Instance() {
        return TermManagerHolder.INSTANCE;
    }

    public void intervalTime() {
        if (mDisposable != null) {
            mDisposable.clear();
        }
        Observable.interval(0, 31, TimeUnit.SECONDS)
                .flatMap(new Function<Long, ObservableSource<TimeBean>>() {
                    @Override
                    public ObservableSource<TimeBean> apply(Long aLong) throws Exception {
                        String time = FormatUtil.getHourStr();
                        mTimeBean.setTime(time);
                        if (TextUtils.equals(time, "23:59")) {
                            ApiManager.stockCheck(11, DBManager.findAllById())
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
                        return Observable.just(mTimeBean);
                    }
                })
                .retry(new Predicate<Throwable>() {
                    @Override
                    public boolean test(@NonNull Throwable throwable) throws Exception {
                        return true;
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new RxProgress<TimeBean>() {
                    @Override
                    protected void onOverSubscribe(Disposable d) {
                        mDisposable.add(d);
                    }

                    @Override
                    protected void onOverNext(TimeBean timeBean) {
                        RxBus.get().post(timeBean);
                    }

                    @Override
                    protected void onOverError(int code, String msg) {

                    }
                });
    }
}
