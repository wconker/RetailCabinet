package com.sovell.retail_cabinet.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.sovell.retail_cabinet.R;
import com.sovell.retail_cabinet.bean.GoodsBean;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

public class CabinetAdapter extends RecyclerView.Adapter {

    private Context mContext;
    private int mItemType;
    private OnClickGoodsItemListener mListener;
    private List<List<GoodsBean>> mCabinetList;

    public CabinetAdapter(Context context, int itemType) {
        this.mItemType = itemType;
        this.mContext = context;
        this.mCabinetList = new ArrayList<>();
    }

    @NonNull
    @Override
    public CabinetHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(mContext).inflate(R.layout.item_cabinet, parent, false);
        return new CabinetHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        cabinetHolder((CabinetHolder) holder, position);
    }

    private void cabinetHolder(CabinetHolder holder, final int position) {
        holder.mFloorTv.setText(String.format(Locale.CHINA, "%s%d", "层号 ", getItemCount() - position));

        final StockAdapter stockAdapter = new StockAdapter(mContext, mItemType);
        LinearLayoutManager manager = new LinearLayoutManager(mContext);
        manager.setOrientation(LinearLayoutManager.HORIZONTAL);
        holder.mCabinetList.setLayoutManager(manager);
        holder.mCabinetList.setAdapter(stockAdapter);
        stockAdapter.setGoodsList(mCabinetList.get(position));
        stockAdapter.setOnGoodsItemListener(new StockAdapter.OnItemGoodsListener() {
            @Override
            public void onClickGoodsItemStock(int index) {
                mListener.OnClickGoodsItemStock(stockAdapter, mCabinetList.get(position).get(index), mCabinetList.get(position).get(index).getRow(), index + 1);
            }

            @Override
            public void onClickGoodsItemTest(int index) {
                mListener.OnClickGoodsItemTest(mCabinetList.get(position).get(index), mCabinetList.get(position).get(index).getRow(), index + 1);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mCabinetList.size();
    }

    public void setCabinetList(List<List<GoodsBean>> cabinetList) {
        mCabinetList.clear();
        for (int i = cabinetList.size(); i > 0; i--) {
            mCabinetList.add(cabinetList.get(i - 1));
        }
        notifyDataSetChanged();
    }

    public List<List<GoodsBean>> getCabinetList() {
        return mCabinetList;
    }

    public static class CabinetHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.item_cabinet_floor)
        TextView mFloorTv;
        @BindView(R.id.item_cabinet_list)
        RecyclerView mCabinetList;

        public CabinetHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    public interface OnClickGoodsItemListener {
        void OnClickGoodsItemStock(StockAdapter adapter, GoodsBean goodsBean, int position, int index);

        void OnClickGoodsItemTest(GoodsBean goodsBean, int position, int index);
    }

    public void setOnClickGoodsItemListener(OnClickGoodsItemListener listener) {
        mListener = listener;
    }
}
