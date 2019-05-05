package com.sovell.retail_cabinet.widget;

import android.content.Context;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.sovell.retail_cabinet.R;
import com.sovell.retail_cabinet.manager.PayStatusEnum;
import com.sovell.retail_cabinet.utils.ConfigUtil;
import com.sovell.retail_cabinet.utils.FormatUtil;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class PaymentForCV extends RelativeLayout {
    @BindView(R.id.tv_success_price)
    TextView tvSuccessPrice;
    @BindView(R.id.tv_success_confirm)
    TextView tvSuccessConfirm;
    @BindView(R.id.ll_success)
    LinearLayout llSuccess;
    @BindView(R.id.tv_fail_price)
    TextView tvFailPrice;
    @BindView(R.id.tv_fail_confirm)
    TextView tvFailConfirm;
    @BindView(R.id.cv_show_pick_time)
    TextView cv_show_pick_time;
    @BindView(R.id.ll_fail)
    LinearLayout llFail;
    @BindView(R.id.tv_fail_content)
    TextView tvFailContent;
    @BindView(R.id.ll_fail_price)
    LinearLayout llFailPrice;

    private Context context;

    public PaymentForCV(Context context) {
        super(context);
    }

    public PaymentForCV(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public PaymentForCV(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public PaymentForCV(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        this.context = context;
        View view = LayoutInflater.from(context).inflate(R.layout.layout_payment_cv, this);
        ButterKnife.bind(this);
        setPickTime();
    }

    @OnClick({R.id.tv_success_confirm, R.id.tv_fail_confirm, R.id.tv_fail_repay})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.tv_fail_repay:
                if (onCloseListener != null)
                    onCloseListener.onConfirmClose(R.id.tv_fail_repay);
                break;
            case R.id.tv_success_confirm:
            case R.id.tv_fail_confirm:
                if (onCloseListener == null) return;
                onCloseListener.onConfirmClose(R.id.tv_fail_confirm);
                break;
        }
    }

    /**
     * 设置支付的各个状态
     */
    public void setStatePayment(PayStatusEnum state, boolean isPay, String msg, String amt, int price) {
        switch (state) {
            case SUCCESS:
                setSuccessVisib(View.VISIBLE);
                tvSuccessPrice.setText("￥" + FormatUtil.div(String.valueOf(price), "100"));
                break;
            case FAIL:
                setFailVisib(View.VISIBLE);
                tvFailPrice.setText("￥" + FormatUtil.div(String.valueOf(price), "100"));
                llFailPrice.setVisibility(isPay ? View.VISIBLE : View.GONE);
                tvFailContent.setText(msg);
                break;
        }
    }

    public void setPickTime() {
        cv_show_pick_time.setText("请于" + ConfigUtil.Instance().getString(ConfigUtil.CV_MEAL_PICKUP_BEGINS) +
                "-" + ConfigUtil.Instance().getString(ConfigUtil.CV_END_OF_MEAL_PICKUP) + "取货");
    }

    public void setSuccessTimerText(String text) {
        tvSuccessConfirm.setText(text);
    }

    public void setFailTimerText(String text) {
        tvFailConfirm.setText(text);
    }

    public void setSuccessVisib(int visib) {
        llSuccess.setVisibility(visib);
    }

    public void setFailVisib(int visib) {
        llFail.setVisibility(visib);
    }

    public interface OnCloseListenerForCV {
        void onConfirmClose(int id);
    }

    private OnCloseListenerForCV onCloseListener;

    public void setOnCloseListener(OnCloseListenerForCV onCloseListener) {
        this.onCloseListener = onCloseListener;
    }
}