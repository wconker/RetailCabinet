package com.sovell.retail_cabinet.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.RemoteException;
import android.text.TextUtils;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.ToggleButton;

import com.sovell.retail_cabinet.R;
import com.sovell.retail_cabinet.base.BaseActivity;
import com.sovell.retail_cabinet.bean.TermSignIn;
import com.sovell.retail_cabinet.https.RxProgress;
import com.sovell.retail_cabinet.manager.ActivityCollector;
import com.sovell.retail_cabinet.manager.ApiManager;
import com.sovell.retail_cabinet.manager.BVMManager;
import com.sovell.retail_cabinet.manager.DBManager;
import com.sovell.retail_cabinet.utils.ConfigUtil;
import com.sovell.retail_cabinet.widget.CustomToast;
import com.sovell.retail_cabinet.widget.DefaultTitle;
import com.sovell.retail_cabinet.widget.HintDialog;
import com.sovell.retail_cabinet.widget.SelectDialog;

import butterknife.BindView;
import butterknife.BindViews;
import butterknife.OnClick;
import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

public class DeviceDebugActivity extends BaseActivity {

    @BindView(R.id.def_title)
    DefaultTitle mDefaultTitle;
    @BindView(R.id.device_debug)
    ToggleButton mDebugBtn;
    @BindViews({R.id.device_light, R.id.device_temperature})
    RelativeLayout[] mDebugLy;

    private HintDialog mHintDialog;
    private SelectDialog mSelectDialog;

    public static void openActivity(Activity activity) {
        Intent intent = new Intent(activity, DeviceDebugActivity.class);
        activity.startActivity(intent);
    }

    @Override
    public int getLayoutId() {
        return R.layout.activity_device_debug;
    }

    @Override
    public void initPresenter() {

    }

    @Override
    public void initView(Bundle savedInstanceState) {
        mHintDialog = new HintDialog(this);
        mSelectDialog = new SelectDialog(this);
        mSelectDialog.setOnClickSelectDialogListener(new SelectDialog.OnClickSelectDialogListener() {
            @Override
            public void OnClickSelectDialog(boolean isClickSure, int mode) {
                try {
                    if (!isClickSure) {
                        return;
                    }
                    int code = 99;
                    if (mode == SelectDialog.MODE_INIT) {
                        initCabinet();
                    } else if (mode == SelectDialog.MODE_DOOR) {
                        code = BVMManager.openDoorAgain();
                    } else {
                        code = BVMManager.faultClean();
                    }
                    if (!BVMManager.isSuccess(code)) {
                        CustomToast.show(BVMManager.errorMsg(code));
                    }
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        });

        try {
            BVMManager.maintainState(true);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            try {
                //获取设备当前模式
                int code = BVMManager.deviceState();
                mDebugBtn.setChecked(code == 4);
                mDebugLy[0].setVisibility(code == 4 ? View.VISIBLE : View.GONE);
                mDebugLy[1].setVisibility(code == 4 ? View.VISIBLE : View.GONE);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    @OnClick({R.id.device_init, R.id.device_error, R.id.device_light, R.id.device_status, R.id.device_back,
            R.id.device_cabinet, R.id.device_temperature, R.id.device_debug, R.id.device_door, R.id.device_error_clear})
    public void onClickDevice(View view) {
        synchronized (this) {
            try {
                int code = 99;
                switch (view.getId()) {
                    case R.id.device_back:
                        ActivityCollector.Instance().finishActivity();
                        break;
                    case R.id.device_init:
                        mSelectDialog.show();
                        mSelectDialog.setContent(SelectDialog.MODE_INIT, "确认要初始化货柜？");
                        break;
                    case R.id.device_error:
                        String[] msg = BVMManager.faultQuery();
                        if (msg.length == 1 && TextUtils.isEmpty(msg[0])) {
                            CustomToast.show("无故障");
                        } else {
                            mHintDialog.show();
                            mHintDialog.setContent(msg);
                        }
                        break;
                    case R.id.device_error_clear:
                        mSelectDialog.show();
                        mSelectDialog.setContent(SelectDialog.MODE_FAILED_CLEAR, "确认要清空错误？");
                        break;
                    case R.id.device_light:
                        DeviceLightActivity.openActivity(this);
                        break;
                    case R.id.device_status:
                        deviceInfo();
                        break;
                    case R.id.device_cabinet:
                        final int[] goods = BVMManager.goodsDetail();
                        if (goods[0] < 0) {
                            CustomToast.show(BVMManager.errorMsg(goods[0]));
                        } else {
                            BVMManager.maintainState(false);
                            CabinetTestActivity.openActivity(this);
                        }
                        break;
                    case R.id.device_temperature:
                        TemperatureActivity.openActivity(this);
                        break;
                    case R.id.device_debug:
                        BVMManager.maintainState(mDebugBtn.isChecked());
                        code = BVMManager.deviceState();
                        mDebugBtn.setChecked(code == 4);
                        mDebugLy[0].setVisibility(code == 4 ? View.VISIBLE : View.GONE);
                        mDebugLy[1].setVisibility(code == 4 ? View.VISIBLE : View.GONE);
                        if (code < 10) {
                            code = 99;
                        }
                        break;
                    case R.id.device_door:
                        mSelectDialog.show();
                        mSelectDialog.setContent(SelectDialog.MODE_DOOR, "确认要开启闸门？");
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

    private void initCabinet() {
        baseShow();
        Observable.just("")
                .observeOn(Schedulers.io())
                .flatMap(new Function<String, ObservableSource<Integer>>() {
                    @Override
                    public ObservableSource<Integer> apply(String s) throws Exception {
                        Thread.sleep(2000);
                        int code = BVMManager.initXYRoad();
                        if (BVMManager.isSuccess(code)) {
                            DBManager.deleteAll();
                            ConfigUtil.Instance().saveBoolean(ConfigUtil.CABINET_INIT, false);
                            ApiManager.stockCheck(12, null)
                                    .subscribe(new RxProgress<TermSignIn>() {
                                        @Override
                                        protected void onOverSubscribe(Disposable d) {

                                        }

                                        @Override
                                        protected void onOverNext(TermSignIn termSignIn) {

                                        }

                                        @Override
                                        protected void onOverError(int code, String msg) {

                                        }
                                    });
                        }
                        return Observable.just(code);
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new RxProgress<Integer>() {

                    @Override
                    protected void onOverSubscribe(Disposable d) {

                    }

                    @Override
                    protected void onOverNext(Integer code) {
                        baseHide();
                        if (BVMManager.isSuccess(code)) {
                            CustomToast.show("初始化成功");
                        } else {
                            mHintDialog.show();
                            mHintDialog.setContent(new String[]{BVMManager.errorMsg(code)});
                        }
                    }

                    @Override
                    protected void onOverError(int code, String msg) {
                        baseHide();
                        CustomToast.show(msg);
                    }
                });
    }

    private void deviceInfo() {
        baseShow();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    final int[] door = BVMManager.doorState();
                    final String deviceMsg = BVMManager.deviceInfo();
                    final int[] goods = BVMManager.goodsDetail();
                    final int state = BVMManager.deviceState();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            baseHide();
                            mHintDialog.show();
                            mHintDialog.setStatus(door, deviceMsg, goods, state);
                        }
                    });
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    @Override
    protected void onDestroy() {
        if (mDefaultTitle != null) {
            mDefaultTitle.destroy();
        }
        if (mHintDialog != null) {
            mHintDialog.dismiss();
        }
        if (mSelectDialog != null) {
            mSelectDialog.dismiss();
        }
        super.onDestroy();
    }
}
