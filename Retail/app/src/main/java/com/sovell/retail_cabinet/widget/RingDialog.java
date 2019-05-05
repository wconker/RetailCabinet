package com.sovell.retail_cabinet.widget;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.sovell.retail_cabinet.R;
import com.sovell.retail_cabinet.base.BaseDialog;
import com.sovell.retail_cabinet.utils.BusinessHelpUtil;
import com.sovell.retail_cabinet.utils.ConfigUtil;

import butterknife.BindView;

public class RingDialog extends BaseDialog {
    @BindView(R.id.cv_book_start)
    TextView cv_book_start;
    @BindView(R.id.cv_pick_time)
    TextView cv_pick_time;
    @BindView(R.id.cv_the_screen_finish)
    LinearLayout theScreenFinish;
    @BindView(R.id.cv_the_screen)
    LinearLayout theScreen;

    public RingDialog(Context context) {
        super(context);
    }

    @Override
    public int getLayoutId() {
        return R.layout.view_ring_dialog;
    }

    @Override
    public void initView(Bundle savedInstanceState) {
        setCanceledOnTouchOutside(true);
    }

    public void setBookDuringTime() {

        ControlDisplayHide(true);

        //预订时间
        cv_book_start.setText(BusinessHelpUtil.getBookingTime());
        //取货时间
        cv_pick_time.setText(BusinessHelpUtil.getTickTime());

    }

    public void setBookFinish(){
        ControlDisplayHide(false);
    }


    private void ControlDisplayHide(boolean show) {
        if (show) {
            theScreenFinish.setVisibility(View.GONE);
            theScreen.setVisibility(View.VISIBLE);
        } else {
            theScreenFinish.setVisibility(View.VISIBLE);
            theScreen.setVisibility(View.GONE);
        }
    }

}
