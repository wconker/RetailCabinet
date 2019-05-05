package com.sovell.retail_cabinet.widget;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.sovell.retail_cabinet.R;
import com.sovell.retail_cabinet.base.BaseDialog;

import butterknife.BindView;
import butterknife.BindViews;
import butterknife.OnClick;

public class RadioDialog extends BaseDialog {

    @BindView(R.id.dialog_radio_group)
    RadioGroup mTimeGroup;
    @BindViews({R.id.dialog_radio_90, R.id.dialog_radio_60, R.id.dialog_radio_30})
    RadioButton[] mTimeRadioBtn;

    private int mIndex;
    private OnRadioDialogListener mListener;

    public RadioDialog(Context context) {
        super(context);
    }

    @Override
    public int getLayoutId() {
        return R.layout.dialog_radio;
    }

    @Override
    public void initView(Bundle savedInstanceState) {
        mTimeGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, @IdRes int checkedId) {
                for (int i = 0; i < mTimeRadioBtn.length; i++) {
                    if (checkedId == mTimeRadioBtn[i].getId()) {
                        mIndex = i;
                    }
                }
            }
        });
    }

    public void initRadioView(int index) {
        mTimeGroup.check(mTimeRadioBtn[index].getId());
    }

    @OnClick({R.id.dialog_radio_cancel, R.id.dialog_radio_sure})
    public void onClickValueDia(View view) {
        if (view.getId() == R.id.dialog_radio_sure && mListener != null) {
            mListener.onRadioDialogResult(mIndex);
        }
        dismiss();
    }

    public interface OnRadioDialogListener {
        void onRadioDialogResult(int index);

    }

    public void setOnRadioDialogListener(OnRadioDialogListener listener) {
        mListener = listener;
    }
}
