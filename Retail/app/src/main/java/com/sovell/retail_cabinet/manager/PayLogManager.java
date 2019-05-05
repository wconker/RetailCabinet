package com.sovell.retail_cabinet.manager;

import android.util.Log;

import com.sovell.retail_cabinet.base.PassPayBean;
import com.sovell.retail_cabinet.base.PassTokenBean;
import com.sovell.retail_cabinet.bean.PayLogBean;
import com.sovell.retail_cabinet.bean.RefundBean;
import com.sovell.retail_cabinet.https.RxException;
import com.sovell.retail_cabinet.utils.PayLogUtil;

import java.util.List;
import java.util.concurrent.TimeUnit;

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

public class PayLogManager {

    private CompositeDisposable mDisposableRefund;

    private static class LogManagerHolder {
        private static final PayLogManager INSTANCE = new PayLogManager();
    }

    private PayLogManager() {
        this.mDisposableRefund = new CompositeDisposable();
    }

    public static PayLogManager getInstance() {
        return LogManagerHolder.INSTANCE;
    }

    /**
     * 订单进行退款
     */
    public void intervalRefund() {
        close();
        Observable.interval(60, TimeUnit.SECONDS)
                .observeOn(Schedulers.io())
                .flatMap(new Function<Long, ObservableSource<RefundBean>>() {
                    @Override
                    public ObservableSource<RefundBean> apply(@NonNull Long aLong) throws Exception {
                        List<PayLogBean> passList = PayLogUtil.readPayLog(PayLogUtil.REFUND, PayLogBean.class);
                        return Observable.fromIterable(passList)
                                .flatMap(new Function<PayLogBean, ObservableSource<RefundBean>>() {
                                    @Override
                                    public ObservableSource<RefundBean> apply(@NonNull final PayLogBean payLogBean) throws Exception {
                                        return ApiManager.logOrderRefund(payLogBean.getInvoice(), payLogBean.getMsg(), payLogBean.getSeq())
                                                .doOnNext(new Consumer<RefundBean>() {
                                                    @Override
                                                    public void accept(RefundBean refundBean) throws Exception {
                                                        if (RxException.isSuccess(refundBean.getCode()) || refundBean.getCode() == RxException.ORDER_NONENTITY) {
                                                            //退款成功
                                                            PayLogUtil.deleteLog(PayLogUtil.REFUND, payLogBean, PayLogBean.class);
                                                        }
                                                    }
                                                });
                                    }
                                });
                    }
                })
                .retry(new Predicate<Throwable>() {
                    @Override
                    public boolean test(@NonNull Throwable throwable) throws Exception {
                        return true;
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<RefundBean>() {
                    @Override
                    public void onSubscribe(@NonNull Disposable d) {
                        mDisposableRefund.add(d);
                    }

                    @Override
                    public void onNext(@NonNull RefundBean refundBean) {

                    }

                    @Override
                    public void onError(@NonNull Throwable e) {

                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }
    /**
     * pass订单进行退款
     */
    public void passIntervalRefund() {

        Observable.interval(60, TimeUnit.SECONDS)
                .observeOn(Schedulers.io())
                .flatMap(new Function<Long, ObservableSource<PassTokenBean>>() {
                    @Override
                    public ObservableSource<PassTokenBean> apply(Long aLong) throws Exception {
                        return ApiManager.passToken();
                    }
                })
                .flatMap(new Function<PassTokenBean, ObservableSource<PassPayBean>>() {
                    @Override
                    public ObservableSource<PassPayBean> apply(@NonNull PassTokenBean passTokenBean) throws Exception {
                        List<PayLogBean> passList = PayLogUtil.readPayLog(PayLogUtil.PASS_REFUND, PayLogBean.class);
                        return Observable.fromIterable(passList)
                                .flatMap(new Function<PayLogBean, ObservableSource<PassPayBean>>() {
                                    @Override
                                    public ObservableSource<PassPayBean> apply(@NonNull final PayLogBean payLogBean) throws Exception {
                                        Log.e("passPay", "apply: " + payLogBean.getSeq());
                                        return ApiManager.passRefund(payLogBean.getInvoice())
                                                .doOnNext(new Consumer<PassPayBean>() {
                                                    @Override
                                                    public void accept(PassPayBean passPayBean) throws Exception {
                                                        if (passPayBean.getStatus().equals(PassStatusEnum.CLOSE.getEnName())) {
                                                            //退款成功,删除本地关于该单的信息
                                                            PayLogUtil.deleteLog(PayLogUtil.PASS_REFUND, payLogBean, PayLogBean.class);
                                                            PayLogUtil.deleteLog(PayLogUtil.PASS_UNUSUAL, payLogBean, PayLogBean.class);
                                                        }
                                                    }
                                                });
                                    }
                                });
                    }
                })
                .retry(new Predicate<Throwable>() {
                    @Override
                    public boolean test(@NonNull Throwable throwable) throws Exception {
                        return true;
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<PassPayBean>() {
                    @Override
                    public void onSubscribe(@NonNull Disposable d) {
                        mDisposableRefund.add(d);
                    }

                    @Override
                    public void onNext(@NonNull PassPayBean refundBean) {

                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        Log.e("passPay", "onError: " + e.getLocalizedMessage());
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }
    public void close() {
        if (mDisposableRefund != null) {
            mDisposableRefund.clear();
        }
    }

}
