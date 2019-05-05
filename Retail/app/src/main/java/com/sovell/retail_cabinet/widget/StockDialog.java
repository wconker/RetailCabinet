package com.sovell.retail_cabinet.widget;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.sovell.retail_cabinet.R;
import com.sovell.retail_cabinet.base.BaseDialog;
import com.sovell.retail_cabinet.bean.GoodsBean;
import com.sovell.retail_cabinet.glide.GlideUtil;
import com.sovell.retail_cabinet.glide.RoundTransformation;
import com.sovell.retail_cabinet.https.HttpsAddress;
import com.sovell.retail_cabinet.manager.DBManager;
import com.sovell.retail_cabinet.utils.ConfigUtil;
import com.sovell.retail_cabinet.utils.FormatUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.OnClick;

public class StockDialog extends BaseDialog {

    @BindView(R.id.goods_iv)
    ImageView mGoodsIv;
    @BindView(R.id.goods_row)
    TextView mGoodsRow;
    @BindView(R.id.goods_column)
    TextView mGoodsColumn;
    @BindView(R.id.goods_name)
    TextView mGoodsName;
    @BindView(R.id.goods_price)
    TextView mGoodsPrice;
    @BindView(R.id.goods_stock)
    TextView mGoodsStock;
    @BindView(R.id.goods_num)
    TextView mGoodsNum;

    private String mApiAddress;
    private GoodsBean mNowGoods;
    private GoodsBean mLastGoods;
    private Context mContext;
    private OnClickChangeGoodsListener mListener;
    private RoundTransformation mTransformation;

    public StockDialog(Context context) {
        super(context);
        String api = ConfigUtil.Instance().getApi("");
        String shop = ConfigUtil.Instance().getString(ConfigUtil.SHOP);
        this.mApiAddress = String.format("%s%s?shop=%s&id=", api, HttpsAddress.PROD_PICTURE, shop);
        this.mContext = context;
    }

    @Override
    public int getLayoutId() {
        return R.layout.dialog_socket;
    }

    @Override
    public void initView(Bundle savedInstanceState) {
        mTransformation = new RoundTransformation(4);
    }

    public void setGoodsBean(GoodsBean goodsBean) {
        if (mNowGoods != null) {
            mLastGoods = (GoodsBean) mNowGoods.clone();
        }
        mNowGoods = (GoodsBean) goodsBean.clone();
        String url = String.format(Locale.CHINA, "%s%s", mApiAddress, goodsBean.getProdid());
        GlideUtil.Instance().loadRoundImage(mContext, url, mGoodsIv, mTransformation);
        String row = String.valueOf(goodsBean.getRow());
        String column = String.format(Locale.CHINA, "%02d", goodsBean.getColumn());
        mGoodsRow.setText(String.format(Locale.CHINA, "%s%d", "层号", goodsBean.getRow()));
        mGoodsColumn.setText(String.format("%s%s", row, column));
        mGoodsName.setText(goodsBean.getProdname());
        mGoodsPrice.setText(String.format("%s%s", "价格 ¥", FormatUtil.div(String.valueOf(goodsBean.getPrice()), "100")));
        mGoodsStock.setText(String.format(Locale.CHINA, "%s%d%s", "剩余 ", goodsBean.getStock(), " 份"));
        mGoodsNum.setText("0");
    }

    @OnClick({R.id.goods_close, R.id.goods_change, R.id.goods_delete, R.id.goods_clear, R.id.goods_sub, R.id.goods_add, R.id.goods_save})
    public void onClickStockDialog(View view) {
        int value = Integer.valueOf(mGoodsNum.getText().toString());
        switch (view.getId()) {
            case R.id.goods_close:
                dismiss();
                break;
            case R.id.goods_change:
                mListener.OnClickChangeGoods(mNowGoods);
                break;
            case R.id.goods_delete:
                setGoodsBean(new GoodsBean(mNowGoods.getRow(), mNowGoods.getColumn()));
                break;
            case R.id.goods_clear:
                mGoodsNum.setText("0");
                mGoodsStock.setText("剩余 0 份");
                mNowGoods.setStock(0);
                setGoodsBean(mNowGoods);
                break;
            case R.id.goods_sub:
                if (value > 0) {
                    mGoodsNum.setText(String.valueOf(--value));
                }
                break;
            case R.id.goods_add:
                if (value < 99) {
                    mGoodsNum.setText(String.valueOf(++value));
                }
                break;
            case R.id.goods_save:
                mNowGoods.setStock(mNowGoods.getStock() + value);
                List<GoodsBean> goodsList = new ArrayList<>();
                if (!TextUtils.isEmpty(mNowGoods.getProdid())) {
                    GoodsBean nowBean = DBManager.findOtherById(mNowGoods);
                    if (nowBean == null) {
                        nowBean = mNowGoods;
                    } else {
                        nowBean.setStock(nowBean.getStock() + mNowGoods.getStock());
                    }
                    goodsList.add(nowBean);
                }

                if (mLastGoods != null && !TextUtils.equals(mLastGoods.getProdid(), mNowGoods.getProdid())) {
                    GoodsBean lastBean = DBManager.findOtherById(mLastGoods);
                    GoodsBean goodsBean = new GoodsBean();
                    goodsBean.setProdid(mLastGoods.getProdid());
                    goodsBean.setStock(lastBean == null ? 0 : lastBean.getStock());
                    goodsList.add(goodsBean);
                }
                mListener.OnClickSaveGoods(mNowGoods, goodsList);
                break;
        }
    }

    @Override
    public void dismiss() {
        mNowGoods = null;
        mLastGoods = null;
        super.dismiss();
    }

    public interface OnClickChangeGoodsListener {
        void OnClickChangeGoods(GoodsBean nowGoods);

        void OnClickSaveGoods(GoodsBean nowGoods, List<GoodsBean> goodsList);
    }

    public void setOnClickChangeGoodsListener(OnClickChangeGoodsListener listener) {
        mListener = listener;
    }
}
