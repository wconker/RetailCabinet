package com.sovell.retail_cabinet.adapter;

import android.content.Context;
import android.view.View;
import android.widget.TextView;

import com.sovell.retail_cabinet.R;
import com.sovell.retail_cabinet.bean.GoodsBean;
import com.sovell.retail_cabinet.event.OnShopCountClickListener;
import com.sovell.retail_cabinet.utils.FormatUtil;
import com.sovell.retail_cabinet.widget.CustomToast;
import com.zhy.adapter.recyclerview.CommonAdapter;
import com.zhy.adapter.recyclerview.base.ViewHolder;

import java.util.List;
import java.util.Locale;

public class CVShopCarAdapter extends CommonAdapter<GoodsBean> {
    private final OnShopCountClickListener mShortClickListener;
    private List<GoodsBean> mOriginalData;

    public CVShopCarAdapter(Context context, int layoutId, List<GoodsBean> datas,
                            OnShopCountClickListener shortClickListener) {
        super(context, layoutId, datas);
        mOriginalData = datas;
        this.mShortClickListener = shortClickListener;
    }

    @Override
    protected void convert(final ViewHolder holder, final GoodsBean dataOrigin, final int position) {
        final TextView localCount = holder.getView(R.id.cv_commodityCount);
        if (mOriginalData.size() > 0) {
            final GoodsBean goods = mOriginalData.get(position);
            holder.setText(R.id.cv_productName, goods.getProdname())
                    .setText(R.id.cv_commodityCount, String.format(Locale.CHINA, "%d", goods.getBuycount()))
                    .setText(R.id.cv_commodityPrice, String.format(Locale.CHINA, "￥%s元",
                            FormatUtil.div(String.valueOf(goods.getPrice()), "100")));
            //购物车减法
            holder.setOnClickListener(R.id.cv_btnSub, new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (position >= mOriginalData.size()) {
                        return;
                    }
                    int localData = StringToInt(localCount.getText().toString());
                    if (localData <= 1) {
                        //只有一个时要先设置数量不然会索引异常
                        mOriginalData.get(position).setBuycount(localData);
                        removeItemByIndex(position);
                    } else {
                        localData = localData - 1;
                        mOriginalData.get(position).setBuycount(localData);
                    }
                    holder.setText(R.id.cv_commodityCount, String.format(Locale.CHINA, "%d", localData));
                    mShortClickListener.SubClick(1, goods.getPrice());
                }
            });

            //购物车加法
            holder.setOnClickListener(R.id.cv_btnAdd, new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (position >= mOriginalData.size()) {
                        return;
                    }
                    //库存不足时给予弹框提示
//                    if (dataOrigin.getStock_max() > 0 &&
//                            dataOrigin.getStock_max() - dataOrigin.getStock_threshold() > 0) {
//                       CustomToast.show("库存不足");
//                        return;
//                    }
                    int localData = StringToInt(localCount.getText().toString()) + 1;
                    holder.setText(R.id.cv_commodityCount, String.format(Locale.CHINA, "%d", localData));
                    mOriginalData.get(position).setBuycount(localData);
                    mShortClickListener.AddClick(1, goods.getPrice());
                }
            });

        }

    }

    private void removeItemByIndex(int pos) {
        if (pos >= 0) {
            mOriginalData.remove(pos);
            notifyDataSetChanged();
        }
    }

    private int StringToInt(String source) {
        if (source.isEmpty()) return 1;
        return Integer.parseInt(source);
    }
}
