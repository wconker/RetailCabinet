package com.sovell.retail_cabinet.presenter.contract;

import com.sovell.retail_cabinet.bean.GoodsBean;
import com.sovell.retail_cabinet.bean.OrderBean;
import com.sovell.retail_cabinet.bean.OrderTakeBean;

public interface PickUpContract {

    void getOrderSuccess(OrderBean orderBean);

    void getOrderFail(int code, String msg);

    void orderTakeSuccess(OrderTakeBean orderTakeBean, GoodsBean goodsBean);

    void orderTakeFail(int code, String msg);

    void pickGoodSuccess(String msg);

    void pickGoodFail(int code, String msg);

    void pickGoodWait();


}
