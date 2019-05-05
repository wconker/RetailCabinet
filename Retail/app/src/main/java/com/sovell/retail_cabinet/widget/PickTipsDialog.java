package com.sovell.retail_cabinet.widget;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.sovell.retail_cabinet.R;
import com.sovell.retail_cabinet.base.BaseDialog;
import com.sovell.retail_cabinet.manager.PickStatusEnum;

import butterknife.BindView;
import butterknife.OnClick;

import static com.sovell.retail_cabinet.manager.PickStatusEnum.PICK_WAIT;


public class PickTipsDialog extends BaseDialog {


    @BindView(R.id.cv_pick_fail_content)
    TextView cv_pick_fail_content;

    @BindView(R.id.cv_pick_ll_loading)
    LinearLayout cv_pick_ll_loading;
    @BindView(R.id.tv_close_confirm)
    TextView tv_close_confirm;

    @BindView(R.id.cv_pick_ll_wait)
    LinearLayout cv_pick_ll_wait;

    @BindView(R.id.cv_pick_ll_fail)
    LinearLayout cv_pick_ll_fail;

    public PickTipsDialog(Context context) {
        super(context);

    }


    @Override
    public int getLayoutId() {
        return R.layout.item_cv_pick_tips;
    }

    public void setContext(String tipContent) {
        if (cv_pick_fail_content != null)
            cv_pick_fail_content.setText(tipContent);
    }

    public void setTipState(PickStatusEnum pickStatus, String setContext) {
        setContext(setContext);
        cv_pick_ll_loading.setVisibility(View.GONE);
        cv_pick_ll_fail.setVisibility(View.GONE);
        cv_pick_ll_wait.setVisibility(View.GONE);
        switch (pickStatus) {
            case PICK_WAIT:
                setDialogOutsideClick(false);
                cv_pick_ll_wait.setVisibility(View.VISIBLE);
                break;
            case PICK_SHIPMENT:
                setDialogOutsideClick(false);
                cv_pick_ll_loading.setVisibility(View.VISIBLE);
                break;
            case PICK_SUCCESS:

                break;
            case PICK_FAIL:
                setDialogOutsideClick(true);
                cv_pick_ll_fail.setVisibility(View.VISIBLE);
                break;
        }
    }

    @OnClick(R.id.tv_close_confirm)
    void onBack() {
        if (isShowing())
            dismiss();
    }

    @Override
    public void initView(Bundle savedInstanceState) {
        setDialogOutsideClick(true);
    }

    public void setDialogOutsideClick(boolean flag) {

        setCanceledOnTouchOutside(flag);
        setCancelable(flag);
    }
}
