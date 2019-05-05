package com.sovell.retail_cabinet.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.RemoteException;
import android.view.View;

import com.sovell.retail_cabinet.R;
import com.sovell.retail_cabinet.base.BaseActivity;
import com.sovell.retail_cabinet.manager.ActivityCollector;
import com.sovell.retail_cabinet.manager.BVMManager;
import com.sovell.retail_cabinet.utils.ConfigUtil;
import com.sovell.retail_cabinet.widget.CustomToast;
import com.sovell.retail_cabinet.widget.DefaultTitle;
import com.sovell.retail_cabinet.widget.RingDialog;

import butterknife.BindView;
import butterknife.OnClick;

public class SetActivity extends BaseActivity {

    @BindView(R.id.def_title)
    DefaultTitle mDefaultTitle;

    public static void openActivity(Activity activity) {
        Intent intent = new Intent(activity, SetActivity.class);
        activity.startActivity(intent);
    }

    @Override
    public int getLayoutId() {
        return R.layout.activity_set;
    }

    @Override
    public void initPresenter() {

    }

    @Override
    public void initView(Bundle savedInstanceState) {
        try {
            BVMManager.initSetKey();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    @OnClick({R.id.set_putaway_ly, R.id.set_debug_ly, R.id.set_basics_ly, R.id.set_about_ly, R.id.set_back})
    public void onClickSet(View view) {
        switch (view.getId()) {
            case R.id.set_back:
                if (ConfigUtil.Instance().getBoolean(ConfigUtil.CABINET_INIT)) {
                    ActivityCollector.Instance().finishAllActivity();
                    SplashActivity.openActivity(SetActivity.this);
                } else {
                    CustomToast.show("请进入货柜维护，完成货柜初始化");
                }
                break;
            case R.id.set_putaway_ly:
                if (ConfigUtil.Instance().getBoolean(ConfigUtil.BIND)) {
                    CabinetActivity.openActivity(this);
                } else {
                    CustomToast.show("设备未绑定");
                }
                break;
            case R.id.set_debug_ly:
                try {
                    final int[] goods = BVMManager.goodsDetail();
                    if (goods[0] < 0) {
                        CustomToast.show(BVMManager.errorMsg(goods[0]));
                    } else {
                        DeviceDebugActivity.openActivity(this);
                    }
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                break;
            case R.id.set_basics_ly:
                BasicConfigActivity.openActivity(this, true);
                break;
            case R.id.set_about_ly:
                AboutActivity.openActivity(this);
                break;
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
