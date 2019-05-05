package com.sovell.retail_cabinet.presenter.impl;

import com.sovell.retail_cabinet.bean.GoodsBean;
import com.sovell.retail_cabinet.bean.ProdBean;
import com.sovell.retail_cabinet.bean.TermSignIn;
import com.sovell.retail_cabinet.https.RxException;
import com.sovell.retail_cabinet.https.RxProgress;
import com.sovell.retail_cabinet.manager.ApiManager;
import com.sovell.retail_cabinet.manager.DBManager;
import com.sovell.retail_cabinet.presenter.contract.CabinetContract;

import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class CabinetPresenterImpl {

    private CabinetContract cabinetContract;
    private CompositeDisposable disposable;

    public CabinetPresenterImpl(CabinetContract cabinetContract) {
        this.cabinetContract = cabinetContract;
        this.disposable = new CompositeDisposable();
    }

    public void getProdList() {
        ApiManager.prodList()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new RxProgress<ProdBean>() {
                    @Override
                    protected void onOverSubscribe(Disposable d) {
                        disposable.add(d);
                    }

                    @Override
                    protected void onOverNext(ProdBean prodBean) {
                        if (RxException.isSuccess(prodBean.getCode())) {
                            cabinetContract.onProdListSuccess(prodBean);
                        } else {
                            cabinetContract.onProdListFailed(prodBean.getCode(), RxException.getErrorMsg(prodBean.getCode(), prodBean.getSub_code()));
                        }
                    }

                    @Override
                    protected void onOverError(int code, String msg) {
                        cabinetContract.onProdListFailed(code, msg);
                    }
                });
    }

    public void stockCheck(final GoodsBean nowGoods, List<GoodsBean> goodsList) {
        ApiManager.stockCheck(10, goodsList)
                .doOnNext(new Consumer<TermSignIn>() {
                    @Override
                    public void accept(TermSignIn termSignIn) throws Exception {
                        if (RxException.isSuccess(termSignIn.getCode())) {
                            DBManager.updateById(nowGoods);
                        }
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new RxProgress<TermSignIn>() {
                    @Override
                    protected void onOverSubscribe(Disposable d) {
                        disposable.add(d);
                    }

                    @Override
                    protected void onOverNext(TermSignIn termSignIn) {
                        if (RxException.isSuccess(termSignIn.getCode())) {
                            cabinetContract.onStockUpdateSuccess(nowGoods, termSignIn);
                        } else {
                            cabinetContract.onStockUpdateFailed(termSignIn.getCode(), RxException.getErrorMsg(termSignIn.getCode(), termSignIn.getSub_code()));
                        }
                    }

                    @Override
                    protected void onOverError(int code, String msg) {
                        cabinetContract.onStockUpdateFailed(code, msg);
                    }
                });
    }

    public void cancelRequest() {
        if (disposable != null) {
            disposable.clear();
        }
    }
}
