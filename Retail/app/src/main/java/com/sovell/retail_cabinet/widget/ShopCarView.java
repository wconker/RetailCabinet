package com.sovell.retail_cabinet.widget;

import android.content.Context;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v7.widget.GridLayoutManager;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.sovell.retail_cabinet.R;
import com.sovell.retail_cabinet.adapter.CVShopCarAdapter;
import com.sovell.retail_cabinet.adapter.ShopCarDecoration;
import com.sovell.retail_cabinet.bean.GoodsBean;
import com.sovell.retail_cabinet.event.OnShopCarClickListener;
import com.sovell.retail_cabinet.event.OnShopCountClickListener;
import com.sovell.retail_cabinet.utils.FormatUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class ShopCarView extends LinearLayout {


    @BindView(R.id.cv_shop_car_list)
    MaxHeightRecyclerView shopCarList;

    @BindView(R.id.cv_shop_car_bottom)
    LinearLayout shop_car_bottom;

    @BindView(R.id.cv_totalCommodityCount)
    TextView cv_totalCommodityCount;

    @BindView(R.id.cv_emptyDataPrompt)
    TextView emptyDataPrompt;

    @BindView(R.id.cv_totalCommodityPrice)
    TextView cv_totalCommodityPrice;

    @BindView(R.id.cv_clearAll)
    TextView clearAll;
    @BindView(R.id.cv_myOrder)
    TextView myOrder;
    @BindView(R.id.cv_tip)
    TextView cv_tip;


    OnShopCarClickListener shopCarClickListener;

    private Context context;

    private List<GoodsBean> datas;

    private CVShopCarAdapter shopCarAdapter;

    private int TotalCount = 0;

    private int TotalPrice = 0;

    public int getTotalCount() {
        return TotalCount;
    }

    public int getTotalPrice() {
        return TotalPrice;
    }


    public ShopCarView(Context context) {
        super(context);
    }

    public ShopCarView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public ShopCarView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public ShopCarView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs);
    }


    @OnClick(R.id.cv_clearAll)
    void clearAllClick() {
        if (shopCarClickListener != null) {
            shopCarClickListener.clearAll();
        }

    }

    @OnClick(R.id.cv_myOrder)
    void myOrderClick() {
        if (shopCarClickListener != null) {
            shopCarClickListener.myOrder();
        }
    }

    void initControl() {
        datas = new ArrayList<>();
        shopCarAdapter = new CVShopCarAdapter(context, R.layout.item_shop_car, datas, new OnShopCountClickListener() {
            @Override
            public void AddClick(int count, int price) {
                isFooterShow();
                TotalOperation(price, 1);
            }

            @Override
            public void SubClick(int count, int price) {
                isFooterShow();
                TotalOperation(-price, -1);
            }
        });
        GridLayoutManager dishManager = new GridLayoutManager(context, 2);
        shopCarList.addItemDecoration(new ShopCarDecoration(20));
        shopCarList.setLayoutManager(dishManager);
        shopCarList.setAdapter(shopCarAdapter);
    }


    public void setShopCarClickListener(OnShopCarClickListener listener) {
        this.shopCarClickListener = listener;
    }

    private void init(Context context, AttributeSet attrs) {
        this.context = context;
        LayoutInflater.from(context).inflate(R.layout.view_shop_car, this);
        ButterKnife.bind(this);
        initControl();
    }

    //把商品添加到购物车中
    public void addGoods(GoodsBean goodsBean) {
        boolean Join2List = true;
        for (GoodsBean tempData : datas) {
            if (goodsBean.getProdid().equals(tempData.getProdid())) {
                tempData.setBuycount(tempData.getBuycount() + 1);
                Join2List = false;
                break;
            }
        }

        if (datas.size() < 1 || Join2List) {
            this.datas.add(goodsBean);
        }

        this.shopCarAdapter.notifyDataSetChanged();
    }

    public void removeAllGoods() {
        TotalCount = 0;
        TotalPrice = 0;
        for (GoodsBean tempData : this.datas) {
            tempData.setBuycount(1);
        }
        this.datas.clear();
        this.shopCarAdapter.notifyDataSetChanged();
        isFooterShow();
    }


    public int getShopCarCount(){
        return datas.size();
    }

    public void isFooterShow() {
        shop_car_bottom.setVisibility(datas.size() > 0 ? VISIBLE : GONE);
        emptyDataPrompt.setVisibility(datas.size() > 0 ? GONE : VISIBLE);
        shopCarList.setVisibility(datas.size() > 0 ? VISIBLE : GONE);
        clearAll.setVisibility(datas.size() > 0 ? VISIBLE : GONE);
        cv_tip.setVisibility(datas.size() > 0 ? GONE : VISIBLE);

    }

    public void TotalOperation(int price, int count) {
        if (shopCarClickListener!=null){
            shopCarClickListener.dataCount(getShopCarCount());
        }
        //总个数累加
        TotalCount = TotalCount + count;
        //金额单位为分。
        TotalPrice = TotalPrice + price;
        cv_totalCommodityCount.setText(String.format(Locale.CANADA, "共%d份,", TotalCount));
        if (TotalPrice >= 0)
            cv_totalCommodityPrice.setText(String.format(Locale.CANADA, "￥%s",
                    FormatUtil.div(String.valueOf(TotalPrice), "100")));

    }


    public List<GoodsBean> getCarGoods() {
        return this.datas;
    }
}
