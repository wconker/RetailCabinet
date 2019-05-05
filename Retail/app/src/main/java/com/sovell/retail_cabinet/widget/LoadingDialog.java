package com.sovell.retail_cabinet.widget;

import android.content.Context;
import android.os.Bundle;

import com.sovell.retail_cabinet.R;
import com.sovell.retail_cabinet.base.BaseDialog;

/**
 * 加载中的dialog
 */

public class LoadingDialog extends BaseDialog {

    public LoadingDialog(Context context) {
        super(context);
    }

    @Override
    public int getLayoutId() {
        return R.layout.dialog_loading;
    }

    @Override
    public void initView(Bundle savedInstanceState) {

    }

}
