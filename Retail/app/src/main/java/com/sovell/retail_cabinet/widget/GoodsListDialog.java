package com.sovell.retail_cabinet.widget;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.sovell.retail_cabinet.R;
import com.sovell.retail_cabinet.adapter.ItemDecoration;
import com.sovell.retail_cabinet.adapter.ProdListAdapter;
import com.sovell.retail_cabinet.adapter.TabAdapter;
import com.sovell.retail_cabinet.base.BaseDialog;
import com.sovell.retail_cabinet.bean.GoodsBean;
import com.sovell.retail_cabinet.bean.TypeBean;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.OnClick;

public class GoodsListDialog extends BaseDialog {

    @BindView(R.id.dialog_goods_tab_list)
    RecyclerView mTabList;
    @BindView(R.id.dialog_goods_prod_list)
    RecyclerView mGoodsList;
    @BindView(R.id.dialog_goods_tab_ly)
    LinearLayout mTabLinearLayout;
    @BindView(R.id.dialog_goods_search_ly)
    LinearLayout mSearchLinearLayout;
    @BindView(R.id.dialog_goods_search_input)
    PwdEditText mSearchInput;

    private int mRow;
    private int mColumn;
    private Context mContext;
    private List<TypeBean> mProdList;
    private List<GoodsBean> mTempGoodsList;
    private List<GoodsBean> mSearchList;
    private ProdListAdapter mProdAdapter;
    private OnSelectNewGoodsListener mListener;

    public GoodsListDialog(Context context) {
        super(context);
        this.mContext = context;
        this.mProdList = new ArrayList<>();
        this.mTempGoodsList = new ArrayList<>();
        this.mSearchList = new ArrayList<>();
    }

    @Override
    public int getLayoutId() {
        return R.layout.dialog_goods_list;
    }

    @Override
    public void initView(Bundle savedInstanceState) {
        Window window = getWindow();
        if (window != null) {
            WindowManager.LayoutParams layoutParams = window.getAttributes();
            layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
            layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
            window.getDecorView().setPadding(0, 0, 0, 0);
            window.setAttributes(layoutParams);
            window.setGravity(Gravity.BOTTOM);
            window.setWindowAnimations(R.style.DateDialogAnim);
        }

        mProdAdapter = new ProdListAdapter(mContext, ProdListAdapter.CABINET_RV);
        GridLayoutManager prodManager = new GridLayoutManager(mContext, 3);
        mGoodsList.setLayoutManager(prodManager);
        mGoodsList.addItemDecoration(new ItemDecoration(mContext));
        mGoodsList.setAdapter(mProdAdapter);
        mProdAdapter.setOnClickProdItemListener(new ProdListAdapter.OnClickProdItemListener() {
            @Override
            public void onClickProdItem(GoodsBean goodsBean, int position) {
                goodsBean.setRow(mRow);
                goodsBean.setColumn(mColumn);
                goodsBean.setBoxid(String.format(Locale.CHINA, "%d-%d", mRow, mColumn));
                mListener.OnSelectNewGoods(goodsBean);
            }
        });

        final TabAdapter tabAdapter = new TabAdapter(mContext);
        LinearLayoutManager tabManager = new LinearLayoutManager(mContext);
        tabManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        mTabList.setLayoutManager(tabManager);
        mTabList.setAdapter(tabAdapter);
        tabAdapter.setTabLists(mProdList);
        tabAdapter.setOnItemClickListener(new TabAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(TypeBean typeBean, int position) {
                tabAdapter.setSelectPos(position);
                mProdAdapter.setProdList(typeBean.getProds());
                mTempGoodsList.clear();
                mTempGoodsList.addAll(typeBean.getProds());
            }
        });
        tabAdapter.onFirstClick(0);

        mSearchInput.addTextListener(new PwdEditText.TextListener() {
            @Override
            public void onTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (!isShowing()) return;
                mSearchList.clear();
                if (!TextUtils.isEmpty(s.toString())) {
                    for (TypeBean typeBean : mProdList) {
                        for (GoodsBean goodsBean : typeBean.getProds()) {
                            if (goodsBean.getPinyin().toUpperCase().contains(s.toString().toUpperCase())) {
                                mSearchList.add(goodsBean);
                            }
                        }
                    }
                }
                mProdAdapter.setProdList(mSearchList);
            }
        });
    }

    public void setProdList(List<TypeBean> typeList) {
        if (typeList != null) {
            for (TypeBean typeBean : typeList) {
                for (GoodsBean goodsBean : typeBean.getProds()) {
                    goodsBean.setCateid(typeBean.getCateid());
                    goodsBean.setCateno(typeBean.getCateno());
                    goodsBean.setCatename(typeBean.getCatename());
                    goodsBean.setStock(0);
                }
            }
            mProdList.addAll(typeList);
        }
    }

    public void setBoxId(int row, int column) {
        mRow = row;
        mColumn = column;
    }

    @OnClick({R.id.dialog_goods_search, R.id.dialog_goods_search_cancel, R.id.dialog_goods_close})
    public void OnClickGoodsListDialog(View view) {
        if (view.getId() == R.id.dialog_goods_search) {
            mTempGoodsList.clear();
            mTempGoodsList.addAll(mProdAdapter.getProdList());
            mProdAdapter.setProdList(null);
            mTabLinearLayout.setVisibility(View.GONE);
            mSearchLinearLayout.setVisibility(View.VISIBLE);
        } else if (view.getId() == R.id.dialog_goods_search_cancel) {
            clickCancel();
        } else {
            hideKeyboard();
            clickCancel();
            dismiss();
        }
    }

    private void clickCancel() {
        mSearchInput.setText("");
        mProdAdapter.setProdList(mTempGoodsList);
        mTabLinearLayout.setVisibility(View.VISIBLE);
        mSearchLinearLayout.setVisibility(View.GONE);
    }

    /**
     * 隐藏软键盘
     */
    private void hideKeyboard() {
        View view = getCurrentFocus();
        if (view instanceof TextView) {
            InputMethodManager mInputMethodManager = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            mInputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.RESULT_UNCHANGED_SHOWN);
        }
    }

    @Override
    public void dismiss() {
        hideKeyboard();
        super.dismiss();
    }

    public interface OnSelectNewGoodsListener {
        void OnSelectNewGoods(GoodsBean goodsBean);
    }

    public void setOnSelectNewGoodsListener(OnSelectNewGoodsListener listener) {
        mListener = listener;
    }
}
