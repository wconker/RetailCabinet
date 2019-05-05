package com.sovell.retail_cabinet.base;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;

import com.sovell.retail_cabinet.R;
import com.sovell.retail_cabinet.utils.DeviceUtil;

import butterknife.ButterKnife;

public abstract class BaseDialog extends Dialog {

    public BaseDialog(Context context) {
        super(context, R.style.DialogStyle);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getWindow() != null) {
            DeviceUtil.fullScreen(getWindow().getDecorView());
        }
        setContentView(getLayoutId());
        ButterKnife.bind(this);
        setCanceledOnTouchOutside(false);
        setCancelable(false);
        initView(savedInstanceState);
    }

    public abstract int getLayoutId();

    public abstract void initView(Bundle savedInstanceState);
}
