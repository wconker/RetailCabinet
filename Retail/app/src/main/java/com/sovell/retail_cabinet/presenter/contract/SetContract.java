package com.sovell.retail_cabinet.presenter.contract;

import com.sovell.retail_cabinet.bean.TermPairing;
import com.sovell.retail_cabinet.bean.TermSignIn;

public interface SetContract {

    void onPairingSuccess(TermPairing termPairing);

    void onPairingFailed(int code, String msg);

    void onSignInSuccess(TermSignIn termSignIn);

    void onSignInFailed(int code, String msg);
}
