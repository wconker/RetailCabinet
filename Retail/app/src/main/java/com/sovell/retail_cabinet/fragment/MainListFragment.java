package com.sovell.retail_cabinet.fragment;


import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;

import com.sovell.retail_cabinet.R;
import com.sovell.retail_cabinet.adapter.DishItemDecoration;
import com.sovell.retail_cabinet.adapter.ProdListAdapter;
import com.sovell.retail_cabinet.adapter.ProdListForCVAdapter;
import com.sovell.retail_cabinet.adapter.TabAdapter;
import com.sovell.retail_cabinet.base.BaseFragment;
import com.sovell.retail_cabinet.bean.GoodsBean;
import com.sovell.retail_cabinet.bean.ProdBean;
import com.sovell.retail_cabinet.bean.TermSignIn;
import com.sovell.retail_cabinet.bean.TypeBean;
import com.sovell.retail_cabinet.event.OnClickProdItemListener;
import com.sovell.retail_cabinet.event.OnShopCarClickListener;
import com.sovell.retail_cabinet.presenter.contract.CabinetContract;
import com.sovell.retail_cabinet.presenter.impl.CabinetPresenterImpl;
import com.sovell.retail_cabinet.widget.CustomToast;
import com.sovell.retail_cabinet.widget.SelectDialog;
import com.sovell.retail_cabinet.widget.ShopCarView;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * 净菜柜主页数据列表和购物车视图
 */
public class MainListFragment extends BaseFragment implements OnShopCarClickListener, CabinetContract {
    @BindView(R.id.main_tab_list)
    RecyclerView mCategoryList;
    @BindView(R.id.main_dish_list)
    RecyclerView mDishRv;

    @BindView(R.id.cv_shopCarView)
    ShopCarView shopCarView;
    private List<GoodsBean> tempGoodsList;
    private SelectDialog mSelectDialog;
    private TabAdapter tabAdapter;
    private ProdListForCVAdapter VegetablesAdapter;
    private CabinetPresenterImpl cabinetPresenter;

    public static MainListFragment getInstance() {
        return new MainListFragment();
    }

    private void initRecyclerView() {
        //分类菜单栏
        tabAdapter = new TabAdapter(mContext);
        tabAdapter.setOnItemClickListener(new TabAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(TypeBean dishBean, int position) {
                tabAdapter.setSelectPos(position);
                //刷新菜品
                VegetablesAdapter.setProdList(dishBean.getProds());
            }
        });
        LinearLayoutManager manager = new LinearLayoutManager(mContext);
        manager.setOrientation(LinearLayoutManager.HORIZONTAL);
        mCategoryList.setLayoutManager(manager);
        mCategoryList.setAdapter(tabAdapter);
        //菜柜商品列表适配器
        VegetablesAdapter = new ProdListForCVAdapter(mContext, ProdListForCVAdapter.MIAN_RV, ProdListAdapter.CABINETMODE);
        VegetablesAdapter.setOnClickProdItemListener(new OnClickProdItemListener() {
            @Override
            public void onClickProdItem(GoodsBean goodsBean, int position) {
                //菜品点击事件，库存大0触发
                if (goodsBean.getStock_max() > 0 &&
                        goodsBean.getStock_max() - goodsBean.getStock_threshold() > 0) {
                    shopCarView.addGoods(goodsBean);
                    shopCarView.isFooterShow();
                    shopCarView.TotalOperation(goodsBean.getPrice(), 1);
                }

                dataCount(shopCarView.getShopCarCount());

            }
        });
        GridLayoutManager dishManager = new GridLayoutManager(mContext, 3);
        mDishRv.setLayoutManager(dishManager);
        mDishRv.addItemDecoration(new DishItemDecoration(mContext, 3));
        ((DefaultItemAnimator) mDishRv.getItemAnimator()).setSupportsChangeAnimations(false);
        mDishRv.setAdapter(VegetablesAdapter);
    }

    void autoHeight(int bottomMargin) {
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) mDishRv.getLayoutParams();
        params.setMargins(0, 0, 0, bottomMargin);
    }

    //去结算
    @OnClick(R.id.cv_settlement)
    void goSettlement() {
        mainActivity.FragmentChange(mainActivity.CONFIRM, shopCarView.getCarGoods(),
                shopCarView.getTotalPrice(), shopCarView.getTotalCount(), true);
        mainActivity.ProductsInCarts = shopCarView.getCarGoods();
    }

    @Override
    public int getLayoutId() {
        return R.layout.fragment_main_list;
    }

    @Override
    public void initPresenter() {
        cabinetPresenter = new CabinetPresenterImpl(this);
        mainActivity.baseShow();
        //每次切换都取一遍
        cabinetPresenter.getProdList();
    }

    //数据初始化赋值
    private void initMainViewData(List<GoodsBean> goodsList) {
        List<TypeBean> typeList = new ArrayList<>();
        TypeBean typeBean = new TypeBean();
        typeBean.setCatename("全部");
        typeBean.setProds(goodsList);
        typeList.add(typeBean);
        for (GoodsBean goodsBean : goodsList) {
            TypeBean typeGoodsBean = new TypeBean();
            typeGoodsBean.setCateid(goodsBean.getCateid());
            if (!typeList.contains(typeGoodsBean)) {
                typeGoodsBean.setCatename(goodsBean.getCatename());
                typeGoodsBean.setCateno(goodsBean.getCateno());
                typeGoodsBean.setProds(new ArrayList<GoodsBean>());
                typeList.add(typeGoodsBean);
            }
        }
        for (TypeBean typeBean1 : typeList) {
            if (TextUtils.equals(typeBean1.getCatename(), "全部")) continue;
            for (GoodsBean goodsBean : goodsList) {
                if (TextUtils.equals(typeBean1.getCateid(), goodsBean.getCateid())) {
                    typeBean1.getProds().add(goodsBean);
                }
            }
        }
        tabAdapter.setTabLists(typeList);
        tabAdapter.onFirstClick(0);
        mainActivity.baseHide();
    }

    @Override
    public void initView(View view, Bundle savedInstanceState) {
        initRecyclerView();
        mSelectDialog = new SelectDialog(mContext);
        mSelectDialog.setOnClickSelectDialogListener(new SelectDialog.OnClickSelectDialogListener() {
            @Override
            public void OnClickSelectDialog(boolean isClickSure, int mode) {
                if (isClickSure) {
                    shopCarView.removeAllGoods();
                    dataCount(shopCarView.getShopCarCount());
                }
            }
        });
        tempGoodsList = new ArrayList<>();
        shopCarView.setShopCarClickListener(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mSelectDialog != null)
            mSelectDialog.dismiss();
    }

    //清空购物车按钮
    @Override
    public void clearAll() {
        mSelectDialog.show();
        mSelectDialog.setContent(0, "确定要全部清空？");
    }

    //前往我的订单
    @Override
    public void myOrder() {
        mainActivity.FragmentChange(mainActivity.ORDER, null, 0, 0, false);
    }

    //根据购物车商品数量计算列表高度避免遮挡列表无法看到剩下的内容
    @Override
    public void dataCount(int count) {
        if (count == 0) {
            autoHeight(200);
        } else if (count > 0 & count < 2) {
            autoHeight(350);
        } else if (2 < count & count < 4) {
            autoHeight(500);
        } else if (count >= 4) {
            autoHeight(600);
        }
        VegetablesAdapter.notifyDataSetChanged();
    }

    @Override
    public void onProdListSuccess(ProdBean prodBean) {
        if (prodBean != null) {
            for (TypeBean typeBean : prodBean.getList()) {
                for (GoodsBean goodsBean : typeBean.getProds()) {
                    goodsBean.setCateid(typeBean.getCateid());
                    goodsBean.setCateno(typeBean.getCateno());
                    goodsBean.setCatename(typeBean.getCatename());
                    goodsBean.setStock(0);
                    goodsBean.setBoxid("");
                    tempGoodsList.add(goodsBean);
                }
            }
        }
        initMainViewData(tempGoodsList);
    }

    @Override
    public void onProdListFailed(int code, String msg) {
        mainActivity.baseHide();
    }

    @Override
    public void onStockUpdateSuccess(GoodsBean goods, TermSignIn termSignIn) {

    }

    @Override
    public void onStockUpdateFailed(int code, String msg) {

    }

    @Override
    public void screenTouch() {

    }
}
