package com.sovell.retail_cabinet.presenter.impl;

import com.sovell.retail_cabinet.bean.OrderBean;
import com.sovell.retail_cabinet.https.RxProgress;
import com.sovell.retail_cabinet.manager.ApiManager;
import com.sovell.retail_cabinet.presenter.contract.OrderContract;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class OrderPresenterImpl {

    private OrderContract orderContract;
    private CompositeDisposable disposable;
    private CompositeDisposable mCompositeDisposable;//出货结果轮询器
    public OrderPresenterImpl(OrderContract orderContract) {
        this.orderContract = orderContract;
        this.disposable = new CompositeDisposable();
        this.mCompositeDisposable = new CompositeDisposable();

    }

    //获取订单列表
    public void VerificationCardInformation(int type, String cid, int pageNum) {
        ApiManager.orderList(type, cid, pageNum)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new RxProgress<OrderBean>() {
                    @Override
                    protected void onOverSubscribe(Disposable d) {
                        disposable.add(d);
                    }

                    @Override
                    protected void onOverNext(OrderBean orderBean) {
                        orderContract.getOrderSuccess(orderBean);
                    }

                    @Override
                    protected void onOverError(int code, String msg) {
                        orderContract.getOrderFail(code, msg);
                    }
                });
    }

    public void cancelRequest() {
        if (disposable != null) {
            disposable.clear();
        }
    }

}
