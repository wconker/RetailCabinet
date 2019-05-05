package com.sovell.retail_cabinet.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.sovell.retail_cabinet.R;
import com.sovell.retail_cabinet.base.OnAdapterListener;
import com.sovell.retail_cabinet.bean.GoodsBean;
import com.sovell.retail_cabinet.glide.GlideUtil;
import com.sovell.retail_cabinet.glide.RoundTransformation;
import com.sovell.retail_cabinet.https.HttpsAddress;
import com.sovell.retail_cabinet.utils.ConfigUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

public class StockAdapter extends RecyclerView.Adapter implements OnAdapterListener {

    public static final int TYPE_ADD = 1;
    public static final int TYPE_TEST = 2;

    private int mItemType;
    private String mApiAddress;
    private Context mContext;
    private List<GoodsBean> mGoodsList;
    private OnItemGoodsListener mListener;
    private RoundTransformation mTransformation;

    public StockAdapter(Context context, int itemType) {
        String api = ConfigUtil.Instance().getApi("");
        String shop = ConfigUtil.Instance().getString(ConfigUtil.SHOP);
        this.mApiAddress = String.format("%s%s?shop=%s&id=", api, HttpsAddress.PROD_PICTURE, shop);
        this.mContext = context;
        this.mItemType = itemType;
        this.mGoodsList = new ArrayList<>();
        this.mTransformation = new RoundTransformation(4);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(mContext).inflate(R.layout.item_goods, parent, false);
        return new StockHolder(v, this);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        goodsHolder((StockHolder) holder, position);
    }

    private void goodsHolder(StockHolder holder, int position) {
        String row = String.valueOf(mGoodsList.get(position).getRow());
        String column = String.format(Locale.CHINA, "%02d", mGoodsList.get(position).getColumn());
        holder.mGoodsNum.setText(String.format("%s%s", row, column));
        if (!TextUtils.isEmpty(mGoodsList.get(position).getProdid())) {
            String url = String.format(Locale.CHINA, "%s%s", mApiAddress, mGoodsList.get(position).getProdid());
            GlideUtil.Instance().loadRoundImage(mContext, url, holder.mGoodsIv, mTransformation);
            holder.mGoodsStock.setText(String.format(Locale.CANADA, "%s%d%s", "剩余", mGoodsList.get(position).getStock(), "份"));
        } else {
            GlideUtil.Instance().loadImage(mContext, R.drawable.ic_add, holder.mGoodsIv);
            holder.mGoodsStock.setText("未上货");
        }
        if (mItemType == TYPE_ADD) {
            holder.mGoodsStock.setVisibility(View.VISIBLE);
        } else {
            holder.mGoodsTest.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public int getItemViewType(int position) {
        return mItemType;
    }

    @Override
    public int getItemCount() {
        return mGoodsList.size();
    }

    public void setGoodsList(List<GoodsBean> goodsList) {
        mGoodsList.clear();
        mGoodsList.addAll(goodsList);
        notifyDataSetChanged();
    }

    public void updateGoods(GoodsBean goods) {
        int index = mGoodsList.indexOf(goods);
        if (index >= 0) {
            mGoodsList.get(index).setProdid(goods.getProdid());
            mGoodsList.get(index).setProdno(goods.getProdno());
            mGoodsList.get(index).setProdname(goods.getProdname());
            mGoodsList.get(index).setPrice(goods.getPrice());

            mGoodsList.get(index).setUnit(goods.getUnit());
            mGoodsList.get(index).setPinyin(goods.getPinyin());
            mGoodsList.get(index).setStock(goods.getStock());
            mGoodsList.get(index).setStock_threshold(goods.getStock_threshold());

            mGoodsList.get(index).setCatename(goods.getCatename());
            mGoodsList.get(index).setCateno(goods.getCateno());
            mGoodsList.get(index).setCateid(goods.getCateid());
            mGoodsList.get(index).setDesc(goods.getDesc());
            notifyItemChanged(index);
        }
    }

    @Override
    public void onAdapterClick(int position, View view) {
        if (position < 0 || position >= mGoodsList.size()) {
            return;
        }
        if (view.getId() == R.id.item_goods_iv) {
            mListener.onClickGoodsItemStock(position);
        } else {
            mListener.onClickGoodsItemTest(position);
        }
    }

    public static class StockHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private OnAdapterListener mListener;

        @BindView(R.id.item_goods_iv)
        ImageView mGoodsIv;
        @BindView(R.id.item_goods_num)
        TextView mGoodsNum;
        @BindView(R.id.item_goods_stock)
        TextView mGoodsStock;
        @BindView(R.id.item_goods_test)
        Button mGoodsTest;

        public StockHolder(View itemView, OnAdapterListener listener) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            mListener = listener;
            mGoodsIv.setOnClickListener(this);
            mGoodsTest.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            mListener.onAdapterClick(getAdapterPosition(), v);
        }
    }

    public interface OnItemGoodsListener {
        void onClickGoodsItemStock(int index);

        void onClickGoodsItemTest(int index);
    }

    public void setOnGoodsItemListener(OnItemGoodsListener listener) {
        mListener = listener;
    }
}
