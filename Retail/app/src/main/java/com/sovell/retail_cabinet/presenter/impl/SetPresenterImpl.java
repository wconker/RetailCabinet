package com.sovell.retail_cabinet.presenter.impl;

import com.sovell.retail_cabinet.bean.TermPairing;
import com.sovell.retail_cabinet.bean.TermSignIn;
import com.sovell.retail_cabinet.https.Api;
import com.sovell.retail_cabinet.https.RxException;
import com.sovell.retail_cabinet.https.RxProgress;
import com.sovell.retail_cabinet.manager.ApiManager;
import com.sovell.retail_cabinet.presenter.contract.SetContract;
import com.sovell.retail_cabinet.utils.ConfigUtil;

import io.reactivex.ObservableSource;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;

public class SetPresenterImpl {

    private SetContract setContract;
    private CompositeDisposable disposable;

    public SetPresenterImpl(SetContract setContract) {
        this.setContract = setContract;
        this.disposable = new CompositeDisposable();
    }

    public void termPairing(String api, String code,int type) {
        if (Api.Instance(api).mApiService == null) {
            setContract.onPairingFailed(RxException.API_ERROR, "API地址异常");
            return;
        }
        ApiManager.termPairing(code,type)
                .subscribe(new RxProgress<TermPairing>() {
                    @Override
                    protected void onOverSubscribe(Disposable d) {
                        disposable.add(d);
                    }

                    @Override
                    protected void onOverNext(TermPairing termPairing) {
                        if (RxException.isSuccess(termPairing.getCode())) {
                            setContract.onPairingSuccess(termPairing);
                        } else {
                            setContract.onPairingFailed(termPairing.getCode(), RxException.getErrorMsg(termPairing.getCode(), termPairing.getSub_code()));
                        }
                    }

                    @Override
                    protected void onOverError(int code, String msg) {
                        setContract.onPairingFailed(code, msg);
                    }
                });
    }

    public void termSignIn(String api, String shop, String term, String key) {
        ApiManager.termSignIn(api, shop, term, key)
                .subscribe(new RxProgress<TermSignIn>() {
                    @Override
                    protected void onOverSubscribe(Disposable d) {
                        disposable.add(d);
                    }

                    @Override
                    protected void onOverNext(TermSignIn termSignIn) {
                        if (RxException.isSuccess(termSignIn.getCode())) {
                            setContract.onSignInSuccess(termSignIn);
                        } else {
                            setContract.onSignInFailed(termSignIn.getCode(), RxException.getErrorMsg(termSignIn.getCode(), termSignIn.getSub_code()));
                        }
                        }

                    @Override
                    protected void onOverError(int code, String msg) {
                        setContract.onSignInFailed(code, msg);
                    }
                });
    }

    public void cancelRequest() {
        if (disposable != null) {
            disposable.clear();
        }
    }
}
