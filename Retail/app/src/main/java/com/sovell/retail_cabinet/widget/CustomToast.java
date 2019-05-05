package com.sovell.retail_cabinet.widget;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.sovell.retail_cabinet.R;
import com.sovell.retail_cabinet.app.RetailCabinetApp;

public class CustomToast {

    private static final CustomToast INSTANCE = new CustomToast();
    private static Toast mToast;
    private static TextView mToastTv;
    private static View mToastRoot;

    public CustomToast() {
        //加载Toast布局
        mToastRoot = LayoutInflater.from(RetailCabinetApp.Instance()).inflate(R.layout.layout_toast, null);
        mToastTv = mToastRoot.findViewById(R.id.toast_tv);
        //Toast的初始化
        mToast = new Toast(RetailCabinetApp.Instance());
        mToast.setGravity(Gravity.CENTER, 0, 0);
        mToast.setDuration(Toast.LENGTH_LONG);
        mToast.setView(mToastRoot);
    }

    public static void show(String msg) {
        mToastTv.setText(msg);
        mToast.show();
    }

}
