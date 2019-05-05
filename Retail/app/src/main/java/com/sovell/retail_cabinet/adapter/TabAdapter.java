package com.sovell.retail_cabinet.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.sovell.retail_cabinet.R;
import com.sovell.retail_cabinet.base.OnAdapterListener;
import com.sovell.retail_cabinet.bean.TypeBean;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class TabAdapter extends RecyclerView.Adapter implements OnAdapterListener {

    private Context mContext;
    private List<TypeBean> mTabLists;
    private int mSelectPos;
    private OnItemClickListener mListener;
    private int mWhite;
    private int mGray;

    public TabAdapter(Context context) {
        this.mContext = context;
        this.mSelectPos = 0;
        this.mTabLists = new ArrayList<>();
        this.mWhite = context.getResources().getColor(R.color.white);
        this.mGray = context.getResources().getColor(R.color.gray_a2);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(mContext).inflate(R.layout.item_tab, parent, false);
        return new TabHolder(v, this);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        tabHolder((TabHolder) holder, position);
    }

    private void tabHolder(TabHolder holder, int position) {
        if (position >= mTabLists.size()) {
            holder.mNameTv.setText("");
            holder.mNameTv.setBackground(null);
        } else {
            holder.mNameTv.setText(mTabLists.get(position).getCatename());
            holder.mNameTv.setTextColor(position == mSelectPos ? mWhite : mGray);
            holder.mNameTv.setBackground(position == mSelectPos ? ContextCompat.getDrawable(mContext, R.drawable.bg_red_corner_30dp) : null);
        }
    }

    @Override
    public int getItemCount() {
        return mTabLists.size();
    }

    public void setTabLists(List<TypeBean> tabLists) {
        mTabLists.clear();
        mTabLists.addAll(tabLists);
        mSelectPos = 0;
        notifyDataSetChanged();
    }

    public void setSelectPos(int position) {
        mSelectPos = position;
        notifyDataSetChanged();
    }

    public List<TypeBean> getTabLists() {
        return mTabLists;
    }

    public void onFirstClick(int position) {
        if (mListener == null || mTabLists.size() <= 0 || mTabLists.size() <= position) return;
        mListener.onItemClick(mTabLists.get(position), position);
    }

    @Override
    public void onAdapterClick(int position, View view) {
        if (view.getId() == R.id.item_category_ly && mListener != null && position > -1 && position < mTabLists.size()) {
            mListener.onItemClick(mTabLists.get(position), position);
        }
    }

    public static class TabHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private OnAdapterListener mListener;

        @BindView(R.id.item_category_ly)
        RelativeLayout mCategoryLy;
        @BindView(R.id.item_category_name)
        TextView mNameTv;

        public TabHolder(View itemView, OnAdapterListener listener) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            mListener = listener;
            mCategoryLy.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            mListener.onAdapterClick(getAdapterPosition(), v);
        }
    }

    public interface OnItemClickListener {
        void onItemClick(TypeBean dishBean, int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.mListener = listener;
    }
}
