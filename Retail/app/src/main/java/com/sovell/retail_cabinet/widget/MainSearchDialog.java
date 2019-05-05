package com.sovell.retail_cabinet.widget;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import com.sovell.retail_cabinet.R;
import com.sovell.retail_cabinet.adapter.DishItemDecoration;
import com.sovell.retail_cabinet.adapter.ProdListAdapter;
import com.sovell.retail_cabinet.bean.GoodsBean;
import com.sovell.retail_cabinet.manager.DBManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnTextChanged;

public class MainSearchDialog extends Dialog {

    @BindView(R.id.et_serch)
    PwdEditText etSearch;
    @BindView(R.id.tv_search_cancel)
    TextView tvSearchCancel;
    @BindView(R.id.main_search_list)
    RecyclerView mDishRv;

    private OnSearchClickListener mListener;
    private List<GoodsBean> goodsList = new ArrayList<>();
    private ProdListAdapter mDishAdapter;
    private Context context;

    public MainSearchDialog(Context context) {
        super(context, R.style.BottomDialogStyle);
        this.context = context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_main_search);
        ButterKnife.bind(this);
        setCanceledOnTouchOutside(false);
        Window window = getWindow();
        if (window != null) {
            WindowManager.LayoutParams layoutParams = window.getAttributes();
            layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
            layoutParams.height = WindowManager.LayoutParams.MATCH_PARENT;
            window.getDecorView().setPadding(0, 0, 0, 0);
            window.setAttributes(layoutParams);
            window.setGravity(Gravity.BOTTOM);
            window.setWindowAnimations(R.style.DateDialogAnim);
        }
        init();
    }

    private void init() {
        //商品栏
        mDishAdapter = new ProdListAdapter(context, ProdListAdapter.MIAN_RV);
        mDishAdapter.setOnClickProdItemListener(new ProdListAdapter.OnClickProdItemListener() {
            @Override
            public void onClickProdItem(GoodsBean goodsBean, int position) {
                //菜品点击事件
                if (goodsBean.getStock() > 0) {
                    if (mListener == null) return;
                    mListener.onSearchClick(goodsBean);
                }
            }
        });
        GridLayoutManager dishManager = new GridLayoutManager(context, 3);
        mDishRv.setLayoutManager(dishManager);
        mDishRv.addItemDecoration(new DishItemDecoration(context, 3));
        ((DefaultItemAnimator) mDishRv.getItemAnimator()).setSupportsChangeAnimations(false);
        mDishRv.setAdapter(mDishAdapter);
    }

    /**
     * 设置搜索数据源
     */
    public void setListData() {
        List<GoodsBean> allGoods = DBManager.findAll();
        goodsList.clear();
        goodsList.addAll(allGoods);
    }

    @OnTextChanged(value = R.id.et_serch, callback = OnTextChanged.Callback.TEXT_CHANGED)
    void onSearchTextChanged(CharSequence s) {
        searchDish(s.toString());
        if (mListener != null) {
            mListener.onTouchEvent();
        }
    }

    private void searchDish(String number) {
        int length = 3 - number.length();
        for (int i = 0; i < length; i++) {
            number += "0";
        }
        int value = Integer.parseInt(number);
        int row = value / 100;
        int column = value / 10 % 10;

        List<GoodsBean> goodsBeanList = new ArrayList<>();
        for (GoodsBean goodsBean : goodsList) {
            if (TextUtils.isEmpty(goodsBean.getProdid())) {
                continue;
            }
            if (length == 0) {
                String id = String.format(Locale.CHINA, "%d%02d", goodsBean.getRow(), goodsBean.getColumn());
                if (TextUtils.equals(number, id)) {
                    goodsBeanList.add(goodsBean);
                }
            } else if (length == 1 && row == goodsBean.getRow()) {
                if ((column < 1 && goodsBean.getColumn() < 10)
                        || (column * 10 < goodsBean.getColumn() && goodsBean.getColumn() < (column + 1) * 10)) {
                    goodsBeanList.add(goodsBean);
                }
            } else if (length == 2) {
                if (row == goodsBean.getRow()) {
                    goodsBeanList.add(goodsBean);
                }
            }
        }
        mDishAdapter.setProdList(goodsBeanList);
    }

    @OnClick(R.id.tv_search_cancel)
    public void onViewClicked() {
        dismiss();
    }


    @Override
    public void show() {
        super.show();
    }

    @Override
    public void dismiss() {
        if (etSearch != null) {
            hideKeyboard();
            etSearch.setText("");
            goodsList.clear();
            mDishAdapter.setProdList(goodsList);
        }
        super.dismiss();
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

    public interface OnSearchClickListener {
        void onSearchClick(GoodsBean goodsBean);

        void onTouchEvent();
    }

    public void setOnReadCardDialogListener(OnSearchClickListener listener) {
        mListener = listener;
    }

    /**
     * 隐藏键盘
     */
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (mListener != null) {
            mListener.onTouchEvent();
        }
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            View v = getCurrentFocus();
            if (isShouldHideInput(v, ev)) {
                InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm != null) {
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                }
            }
            return super.dispatchTouchEvent(ev);
        }
        // 必不可少，否则所有的组件都不会有TouchEvent了
        if (getWindow().superDispatchTouchEvent(ev)) {
            return true;
        }
        return onTouchEvent(ev);
    }

    /**
     * 获取EditText的位置
     */
    protected boolean isShouldHideInput(View v, MotionEvent event) {
        if (v instanceof EditText) {
            int[] leftTop = {0, 0};
            //获取输入框当前的location位置
            v.getLocationInWindow(leftTop);
            int left = leftTop[0];
            int top = leftTop[1];
            int bottom = top + v.getHeight();
            int right = left + v.getWidth();
            if (event.getX() > left && event.getX() < right
                    && event.getY() > top && event.getY() < bottom) {
                // 点击的是输入框区域，保留点击EditText的事件
                return false;
            } else {
                return true;
            }
        }
        return false;
    }
}
