package com.sovell.retail_cabinet.manager;

import android.content.Context;
import android.text.TextUtils;

import com.sovell.card.CardUSB;

import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;
import io.reactivex.functions.Predicate;
import io.reactivex.schedulers.Schedulers;

public class CardManager {

    private CompositeDisposable mCompositeDisposable;
    /*开始时间，用来判断超时*/
    private long mStartTime;
    /*上一次读到的卡号，用来判断重复刷卡*/
    private String mLastCardNo;
    /*超时时间*/
    private static final long TIMEOUT = 2000;


    public CardManager() {
        mCompositeDisposable = new CompositeDisposable();

    }

    public void open(Context context) {
        boolean is = CardUSB.Instance().openIC(context, CardUSB.TYPE_A);
    }

    public void readCard() {
        stop();
        Observable.interval(1000, 500, TimeUnit.MILLISECONDS)
                .observeOn(Schedulers.io())
                .flatMap(new Function<Long, ObservableSource<String>>() {
                    @Override
                    public ObservableSource<String> apply(@NonNull Long aLong) throws Exception {
                        //Log.e("读卡", "0.5秒一次");
                        String cid = CardUSB.Instance().readICCard();
                        if (!TextUtils.isEmpty(cid)) {
                            long now = System.currentTimeMillis();
                            if (now - mStartTime > TIMEOUT || !TextUtils.equals(mLastCardNo, cid)) {
                                //Log.e("读卡", "2秒一次");
                                mStartTime = now;
                                mLastCardNo = cid;
                                return Observable.just(cid);
                            }
                        }
                        return Observable.just("");
                    }
                })
                .retry(new Predicate<Throwable>() {
                    @Override
                    public boolean test(@NonNull Throwable throwable) throws Exception {
                        return true;
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<String>() {
                    @Override
                    public void onSubscribe(@NonNull Disposable d) {
                        mCompositeDisposable.add(d);
                    }

                    @Override
                    public void onNext(@NonNull String cid) {
                        if (!TextUtils.isEmpty(cid)) {
                            stop();
                            if (readCardListener == null) return;
                            readCardListener.onReadCard(cid);
                        }
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {

                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    public void stop() {
        if (mCompositeDisposable != null) {
            mCompositeDisposable.clear();
        }
    }

    /**
     * 蜂鸣
     */
    public void beeper() {
        CardUSB.Instance().beep();
    }

    /**
     * 关闭串口
     */
    public void close() {
        stop();
        CardUSB.Instance().closePort();
    }

    public interface ReadCardListener {
        void onReadCard(String cid);
    }

    private ReadCardListener readCardListener;

    public void setReadCardListener(ReadCardListener readCardListener) {
        this.readCardListener = readCardListener;
    }
}
