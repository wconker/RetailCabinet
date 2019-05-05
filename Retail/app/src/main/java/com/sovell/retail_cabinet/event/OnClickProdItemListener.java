package com.sovell.retail_cabinet.event;

import com.sovell.retail_cabinet.bean.GoodsBean;

public interface OnClickProdItemListener {
    void onClickProdItem(GoodsBean goodsBean, int position);
}
