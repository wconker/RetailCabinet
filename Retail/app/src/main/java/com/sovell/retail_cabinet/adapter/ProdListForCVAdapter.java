package com.sovell.retail_cabinet.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.jakewharton.rxbinding2.view.RxView;
import com.sovell.retail_cabinet.R;
import com.sovell.retail_cabinet.base.OnAdapterListener;
import com.sovell.retail_cabinet.bean.GoodsBean;
import com.sovell.retail_cabinet.event.OnClickProdItemListener;
import com.sovell.retail_cabinet.glide.GlideUtil;
import com.sovell.retail_cabinet.glide.RoundTransformation;
import com.sovell.retail_cabinet.https.HttpsAddress;
import com.sovell.retail_cabinet.https.RxProgress;
import com.sovell.retail_cabinet.utils.ConfigUtil;
import com.sovell.retail_cabinet.utils.FormatUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.disposables.Disposable;

public class ProdListForCVAdapter extends RecyclerView.Adapter implements OnAdapterListener {
    public static final int MIAN_RV = 0;//首页的recycleview
    public static final int CABINET_RV = 1;//上货的recycleview
    public static final int CABINETMODE = 1;//表示净菜柜，不是净菜柜不传可以不传。

    private Context mContext;
    private OnClickProdItemListener mListener;
    private List<GoodsBean> mProdLists;
    private RoundTransformation mTransformation;
    private String mApiAddress;
    private int openType;
    private int mCabinetMode = 0; //净菜柜=1，零食柜=0

    //加字段区分净菜柜和零食柜
    public ProdListForCVAdapter(Context context, int openType, int cabinetMode) {
        String api = ConfigUtil.Instance().getApi("");
        String shop = ConfigUtil.Instance().getString(ConfigUtil.SHOP);
        this.mApiAddress = String.format("%s%s?shop=%s&id=", api, HttpsAddress.PROD_PICTURE, shop);
        this.mCabinetMode = cabinetMode;
        this.openType = openType;
        this.mContext = context;
        this.mProdLists = new ArrayList<>();
        this.mTransformation = new RoundTransformation(8, RoundTransformation.CornerType.TOP);
    }

    public ProdListForCVAdapter(Context context, int openType) {
        String api = ConfigUtil.Instance().getApi("");
        String shop = ConfigUtil.Instance().getString(ConfigUtil.SHOP);
        this.mApiAddress = String.format("%s%s?shop=%s&id=", api, HttpsAddress.PROD_PICTURE, shop);
        this.openType = openType;
        this.mContext = context;
        this.mProdLists = new ArrayList<>();
        this.mTransformation = new RoundTransformation(8, RoundTransformation.CornerType.TOP);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = null;
        if (this.mCabinetMode == CABINETMODE) {
            v = LayoutInflater.from(mContext).inflate(R.layout.item_cv_prod,
                    parent, false);
        } else {
            v = LayoutInflater.from(mContext).inflate(R.layout.item_prod,
                    parent, false);
        }

        return new ProdHolder(v, this);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        prodHolder((ProdHolder) holder, position);
    }

    private void prodHolder(ProdHolder holder, int position) {
        GoodsBean bean = mProdLists.get(position);
        String url = String.format(Locale.CHINA, "%s%s", mApiAddress, bean.getProdid());
        GlideUtil.Instance().loadRoundImage(mContext, url, holder.mProdIv, mTransformation);
        holder.mProdName.setText(bean.getProdname());
        holder.mProdPrice.setText(String.format(Locale.CANADA, "%s%s", "¥", FormatUtil.div(String.valueOf(bean.getPrice()), "100")));
        if (openType == MIAN_RV) {
            //最大可预订
            if (bean.getStock_max() > 0
                    && bean.getStock_max() - bean.getStock_threshold() > 0) {
                holder.itemDishEmptyIv.setVisibility(View.GONE);
            } else {
                holder.itemDishEmptyIv.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    public int getItemCount() {
        return mProdLists.size();
    }

    public void setProdList(List<GoodsBean> prodLists) {
        mProdLists.clear();
        if (prodLists != null) {
            mProdLists.addAll(prodLists);
        }
        notifyDataSetChanged();
    }

    public List<GoodsBean> getProdList() {
        return mProdLists;
    }

    @Override
    public void onAdapterClick(final int position, View view) {
        if (mListener == null || position >= mProdLists.size() || position < 0) {
            return;
        }
        mListener.onClickProdItem(mProdLists.get(position), position);
    }

    public static class ProdHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private OnAdapterListener mListener;

        @BindView(R.id.item_prod)
        LinearLayout mProdItem;
        @BindView(R.id.item_prod_iv)
        ImageView mProdIv;
        @BindView(R.id.item_dish_empty_iv)
        ImageView itemDishEmptyIv;
        @BindView(R.id.item_prod_name)
        TextView mProdName;
        @BindView(R.id.item_prod_price)
        TextView mProdPrice;

        ProdHolder(View itemView, OnAdapterListener listener) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            mListener = listener;

            RxView.clicks(mProdItem)
                    .throttleFirst(1, TimeUnit.SECONDS)
                    .subscribe(new RxProgress<Object>() {
                        @Override
                        protected void onOverSubscribe(Disposable d) {

                        }

                        @Override
                        protected void onOverNext(Object o) {
                            mListener.onAdapterClick(getAdapterPosition(), mProdItem);
                        }

                        @Override
                        protected void onOverError(int code, String msg) {

                        }
                    });
        }

        @Override
        public void onClick(View v) {
            mListener.onAdapterClick(getAdapterPosition(), v);
        }
    }


    public void setOnClickProdItemListener(OnClickProdItemListener listener) {
        mListener = listener;
    }
}
