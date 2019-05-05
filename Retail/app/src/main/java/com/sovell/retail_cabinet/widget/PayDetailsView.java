package com.sovell.retail_cabinet.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.sovell.retail_cabinet.R;
import com.sovell.retail_cabinet.bean.GoodsBean;
import com.sovell.retail_cabinet.glide.GlideUtil;
import com.sovell.retail_cabinet.glide.RoundTransformation;
import com.sovell.retail_cabinet.https.HttpsAddress;
import com.sovell.retail_cabinet.manager.PayModeEnum;
import com.sovell.retail_cabinet.manager.PayStatusEnum;
import com.sovell.retail_cabinet.utils.ConfigUtil;
import com.sovell.retail_cabinet.utils.FormatUtil;

import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * 结算详情
 */
public final class PayDetailsView extends RelativeLayout {
    @BindView(R.id.iv_goods_img)
    ImageView ivGoodsImg;
    @BindView(R.id.tv_pay_close)
    TextView tvPayClose;
    @BindView(R.id.tv_goods_name)
    TextView tvGoodsName;
    @BindView(R.id.tv_goods_desc)
    TextView tvGoodsDesc;
    @BindView(R.id.tv_goods_price)
    TextView tvGoodsPrice;
    @BindView(R.id.tv_pay_price)
    TextView tvPayPrice;
    @BindView(R.id.ll_pay_by_card)
    LinearLayout llPayByCard;
    @BindView(R.id.progressBar)
    ProgressBar progressBar;
    @BindView(R.id.tv_pay_content)
    TextView tvPayContent;
    @BindView(R.id.rl_pay_processing)
    RelativeLayout rlPayProcessing;
    @BindView(R.id.ll_pay_take_out)
    LinearLayout llPayTakeOut;
    @BindView(R.id.ll_pay_details)
    LinearLayout llPayDetails;
    @BindView(R.id.rl_qrloading)
    RelativeLayout rlQrloading;
    @BindView(R.id.qr_code)
    ImageView qrCode;
    @BindView(R.id.iv_ali_badge)
    ImageView ivAliBadge;
    @BindView(R.id.iv_wechat_badge)
    ImageView ivWechatBadge;
    @BindView(R.id.rl_qr_pay)
    RelativeLayout rlQrPay;
    @BindView(R.id.iv_card_pay)
    ImageView ivCardPay;
    @BindView(R.id.tv_wanna_qr)
    TextView tvWannaQr;
    @BindView(R.id.ll_alpay)
    LinearLayout llAlpay;
    @BindView(R.id.ll_wechat)
    LinearLayout llWechat;


    private String mApiAddress;
    private RoundTransformation mTransformation;
    private Context context;

    public PayDetailsView(Context context) {
        super(context);
    }

    public PayDetailsView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public PayDetailsView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public PayDetailsView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        String api = ConfigUtil.Instance().getApi("");
        String shop = ConfigUtil.Instance().getString(ConfigUtil.SHOP);
        this.mApiAddress = String.format("%s%s?shop=%s&id=", api, HttpsAddress.PROD_PICTURE, shop);
        this.mTransformation = new RoundTransformation(4, RoundTransformation.CornerType.LEFT);
        this.context = context;
        View view = LayoutInflater.from(context).inflate(R.layout.layout_pay_details, this);
        ButterKnife.bind(this);
    }

    @OnClick({R.id.tv_pay_close, R.id.ll_wechat, R.id.ll_alpay, R.id.tv_wanna_qr})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.tv_pay_close:
                if (onCloseListener == null) return;
                onCloseListener.onClose();
                break;
            case R.id.tv_wanna_qr:
                if (ivCardPay.getVisibility() == GONE) {
                    tvWannaQr.setText("我要扫码支付");
                    ivCardPay.setVisibility(VISIBLE);
                    rlQrPay.setVisibility(GONE);
                } else {
                    tvWannaQr.setText("我要刷卡支付");
                    if (onPayModelChangeListener != null) {
                        switchPayModel(R.id.ll_alpay);//默认alipay
                        ivCardPay.setVisibility(GONE);
                        rlQrPay.setVisibility(VISIBLE);
                        onPayModelChangeListener.onChange(PayModeEnum.ALI_PAY.getValue());
                    }
                }
                break;
            case R.id.ll_wechat:
                if (onPayModelChangeListener != null) {
                    switchPayModel(view.getId());
                    onPayModelChangeListener.onChange(PayModeEnum.WEI_CHAT.getValue());
                }
                break;
            case R.id.ll_alpay:
                if (onPayModelChangeListener != null) {
                    switchPayModel(view.getId());
                    onPayModelChangeListener.onChange(PayModeEnum.ALI_PAY.getValue());
                }
                break;
        }
    }


    //切换的样式事件变化
    private void switchPayModel(int Rid) {
        //显示等待信息
        QRLoadingVisit(true);
        ivAliBadge.setVisibility(GONE);
        ivWechatBadge.setVisibility(GONE);
        //切换图标
        if (Rid == R.id.ll_alpay) {
            llAlpay.setBackground(getResources().getDrawable(R.drawable.border_white));
            llWechat.setBackground(getResources().getDrawable(R.drawable.border_black));
            ivAliBadge.setVisibility(VISIBLE);
        } else {
            llWechat.setBackground(getResources().getDrawable(R.drawable.border_white));
            llAlpay.setBackground(getResources().getDrawable(R.drawable.border_black));
            ivWechatBadge.setVisibility(VISIBLE);
        }


    }

    /**
     * 设置支付的各个状态
     */
    public void setState(PayStatusEnum state) {
        llPayByCard.setVisibility(View.GONE);
        rlPayProcessing.setVisibility(View.GONE);
        llPayTakeOut.setVisibility(View.GONE);
        switch (state) {
            case BY_CARD:
                llPayDetails.setVisibility(View.VISIBLE);
                llPayByCard.setVisibility(View.VISIBLE);
                tvPayClose.setVisibility(View.VISIBLE);
                break;
            case IN_PAYMENT:
                tvPayClose.setVisibility(View.GONE);
                llPayDetails.setVisibility(View.VISIBLE);
                rlPayProcessing.setVisibility(View.VISIBLE);
                tvPayContent.setText(R.string.in_payment);
                break;
            case SHIPMENT:
                tvPayClose.setVisibility(View.GONE);
                llPayDetails.setVisibility(View.VISIBLE);
                rlPayProcessing.setVisibility(View.VISIBLE);
                tvPayContent.setText(R.string.in_shipment);
                break;
            case PICK_GOODS:
                tvPayClose.setVisibility(View.GONE);
                llPayDetails.setVisibility(View.VISIBLE);
                llPayTakeOut.setVisibility(View.VISIBLE);
                break;

        }
    }

    public void showContent(GoodsBean goodsBean) {
        String url = String.format(Locale.CHINA, "%s%s", mApiAddress, goodsBean.getProdid());
        GlideUtil.Instance().loadRoundImage(context, url, ivGoodsImg, mTransformation);
        tvGoodsName.setText(goodsBean.getProdname());
        tvGoodsDesc.setText(goodsBean.getDesc());
        tvGoodsPrice.setText("￥" + FormatUtil.div(String.valueOf(goodsBean.getPrice()), "100"));
        tvPayPrice.setText("￥" + FormatUtil.div(String.valueOf(goodsBean.getPrice()), "100"));
    }

    public void setTimerText(String text) {
        tvPayClose.setText(text);
    }

    public void setViewVisibility(int visib) {
        llPayDetails.setVisibility(visib);
    }

    public interface OnCloseListener {
        void onClose();
    }

    public interface OnPayModelChangeListener {
        void onChange(String model);
    }

    private OnCloseListener onCloseListener;

    public void setOnCloseListener(OnCloseListener onCloseListener) {
        this.onCloseListener = onCloseListener;
    }

    private OnPayModelChangeListener onPayModelChangeListener;

    //设置切换回掉
    public void setOnPayModelChangeListener(OnPayModelChangeListener onPayModelChangeListener) {
        this.onPayModelChangeListener = onPayModelChangeListener;
    }


    //设置二维码
    public void setQRCode(Bitmap qrPay) {
        if (qrPay != null) {
            qrCode.setImageBitmap(qrPay);
            QRLoadingVisit(false);
        }
    }

    //等待二维码的转动动画
    public void QRLoadingVisit(boolean show) {
        rlQrloading.setVisibility(show ? VISIBLE : GONE);
    }
}
