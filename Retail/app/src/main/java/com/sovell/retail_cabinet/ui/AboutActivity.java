package com.sovell.retail_cabinet.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.sovell.retail_cabinet.R;
import com.sovell.retail_cabinet.base.BaseActivity;
import com.sovell.retail_cabinet.bean.VersionInfo;
import com.sovell.retail_cabinet.https.RxProgress;
import com.sovell.retail_cabinet.manager.ActivityCollector;
import com.sovell.retail_cabinet.manager.ApiManager;
import com.sovell.retail_cabinet.utils.DeviceUtil;
import com.sovell.retail_cabinet.widget.CustomToast;
import com.sovell.retail_cabinet.widget.DefaultTitle;

import butterknife.BindView;
import butterknife.OnClick;
import io.reactivex.disposables.Disposable;

public class AboutActivity extends BaseActivity {

    @BindView(R.id.def_title)
    DefaultTitle mDefaultTitle;
    @BindView(R.id.about_version)
    TextView mVersionTv;

    public static void openActivity(Activity activity) {
        Intent intent = new Intent(activity, AboutActivity.class);
        activity.startActivity(intent);
    }

    @Override
    public int getLayoutId() {
        return R.layout.activity_about;
    }

    @Override
    public void initPresenter() {

    }

    @Override
    public void initView(Bundle savedInstanceState) {
        mVersionTv.setText(DeviceUtil.versionName());
    }

    @OnClick({R.id.about_back, R.id.about_update})
    public void onClickAbout(View view) {
        if (view.getId() == R.id.about_back) {
            ActivityCollector.Instance().finishActivity();
        } else {
            baseShow();
            ApiManager.checkVersion()
                    .subscribe(new RxProgress<VersionInfo>() {
                        @Override
                        protected void onOverSubscribe(Disposable d) {
                            baseHide();
                        }

                        @Override
                        protected void onOverNext(VersionInfo versionInfo) {
                            if (versionInfo.getVersionCode() > DeviceUtil.versionCode()) {
                                ApiManager.updateApp(AboutActivity.this, versionInfo.getApkName());
                            } else {
                                CustomToast.show("已是最新版本");
                            }
                        }

                        @Override
                        protected void onOverError(int code, String msg) {
                            CustomToast.show("检测更新失败");
                        }
                    });
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
