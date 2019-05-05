package com.sovell.retail_cabinet.presenter.contract;

import com.sovell.retail_cabinet.bean.OrderBean;

public interface OrderContract {
    void getOrderSuccess(OrderBean orderBean);

    void getOrderFail(int code, String msg);

}
