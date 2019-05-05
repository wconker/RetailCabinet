package com.sovell.retail_cabinet.presenter.contract;

import com.sovell.retail_cabinet.bean.GoodsBean;
import com.sovell.retail_cabinet.bean.ProdBean;
import com.sovell.retail_cabinet.bean.TermSignIn;

public interface CabinetContract {

    void onProdListSuccess(ProdBean prodBean);

    void onProdListFailed(int code, String msg);

    void onStockUpdateSuccess(GoodsBean goods, TermSignIn termSignIn);

    void onStockUpdateFailed(int code, String msg);
}
