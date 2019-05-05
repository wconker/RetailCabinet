package com.sovell.retail_cabinet.widget;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.sovell.retail_cabinet.R;
import com.sovell.retail_cabinet.widget.sheet.BottomSheetLayout;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public final class MainSearchView {

    private Context context;
    private BottomSheetLayout bottomSheetLayout;

    public MainSearchView(Context context, BottomSheetLayout bottomSheetLayout) {
        this.context = context;
        this.bottomSheetLayout = bottomSheetLayout;
    }

    public Builder newBuilder() {
        return new Builder(this);
    }

    public static final class Builder {

        @BindView(R.id.et_serch)
        PwdEditText etSerch;
        @BindView(R.id.tv_search_cancel)
        TextView tvSearchCancel;
        @BindView(R.id.main_search_list)
        RecyclerView mainSearchList;
        private Context context;
        private BottomSheetLayout bottomSheetLayout;

        public Builder(MainSearchView view) {
            this.context = view.context;
            this.bottomSheetLayout = view.bottomSheetLayout;
        }

        public Builder(Context context, BottomSheetLayout bottomSheetLayout) {
            this.context = context;
            this.bottomSheetLayout = bottomSheetLayout;
        }

        public View build() {
            View view = LayoutInflater.from(context).inflate(R.layout.layout_main_search, bottomSheetLayout, false);
            ButterKnife.bind(view);
            FrameLayout.LayoutParams linearParams = (FrameLayout.LayoutParams) view.getLayoutParams();
            linearParams.height = 500;
            view.setLayoutParams(linearParams);


            return view;
        }

        @OnClick(R.id.et_serch)
        public void onViewClicked() {
        }
    }
}
