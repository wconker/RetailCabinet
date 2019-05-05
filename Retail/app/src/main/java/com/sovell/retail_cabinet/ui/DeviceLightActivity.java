package com.sovell.retail_cabinet.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.RemoteException;
import android.view.View;
import android.widget.ToggleButton;

import com.sovell.retail_cabinet.R;
import com.sovell.retail_cabinet.base.BaseActivity;
import com.sovell.retail_cabinet.manager.ActivityCollector;
import com.sovell.retail_cabinet.manager.BVMManager;
import com.sovell.retail_cabinet.widget.CustomToast;
import com.sovell.retail_cabinet.widget.DefaultTitle;

import butterknife.BindView;
import butterknife.BindViews;
import butterknife.OnClick;

public class DeviceLightActivity extends BaseActivity {

    @BindView(R.id.def_title)
    DefaultTitle mDefaultTitle;
    @BindViews({R.id.light_box_1, R.id.light_box_2, R.id.light_take})
    ToggleButton[] mLightBtn;

    public static void openActivity(Activity activity) {
        Intent intent = new Intent(activity, DeviceLightActivity.class);
        activity.startActivity(intent);
    }

    @Override
    public int getLayoutId() {
        return R.layout.activity_device_light;
    }

    @Override
    public void initPresenter() {

    }

    @Override
    public void initView(Bundle savedInstanceState) {
        try {
            boolean[] lightState = BVMManager.lightState();
            mLightBtn[0].setChecked(lightState[0]);
            mLightBtn[1].setChecked(lightState[1]);
            mLightBtn[2].setChecked(lightState[2]);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    @OnClick({R.id.light_box_1, R.id.light_box_2, R.id.light_take, R.id.light_back})
    public void onClickLight(View view) {
        synchronized (this) {
            try {
                int code = 99;
                switch (view.getId()) {
                    case R.id.light_back:
                        ActivityCollector.Instance().finishActivity();
                        break;
                    case R.id.light_box_1:
                        code = BVMManager.lightOnOff(1, mLightBtn[0].isChecked());
                        break;
                    case R.id.light_box_2:
                        code = BVMManager.lightOnOff(2, mLightBtn[1].isChecked());
                        break;
                    case R.id.light_take:
                        code = BVMManager.lightOnOff(3, mLightBtn[2].isChecked());
                        break;
                }
                if (!BVMManager.isSuccess(code)) {
                    CustomToast.show(BVMManager.errorMsg(code));
                }
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onDestroy() {
        if (mDefaultTitle != null) {
            mDefaultTitle.destroy();
        }
        super.onDestroy();
    }
}
