package com.sovell.retail_cabinet.event;

import com.sovell.retail_cabinet.bean.GoodsBean;

public interface OnPayDialogListener {
    /**
     * 返回结果
     *
     * @param funFlag
     */

    void onPayDialog(int funFlag);

    /**
     * 刷新视图
     */
    void onNotifyView(GoodsBean goodsBean);
}
