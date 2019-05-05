package com.sovell.retail_cabinet.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.RemoteException;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.sovell.retail_cabinet.R;
import com.sovell.retail_cabinet.base.BaseActivity;
import com.sovell.retail_cabinet.https.RxProgress;
import com.sovell.retail_cabinet.manager.ActivityCollector;
import com.sovell.retail_cabinet.manager.BVMManager;
import com.sovell.retail_cabinet.utils.ConfigUtil;
import com.sovell.retail_cabinet.widget.CustomToast;
import com.sovell.retail_cabinet.widget.DefaultTitle;
import com.sovell.retail_cabinet.widget.RangeSlider;

import java.util.Locale;

import butterknife.BindView;
import butterknife.BindViews;
import butterknife.OnClick;
import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

public class TemperatureActivity extends BaseActivity {

    @BindView(R.id.def_title)
    DefaultTitle mDefaultTitle;
    @BindViews({R.id.temp_clod, R.id.temp_heat})
    Button[] mTempBtn;
    @BindView(R.id.temp_scope)
    RangeSlider mRangeSlider;
    @BindView(R.id.temp_current)
    TextView mCurrentTv;
    @BindView(R.id.temp_on_off)
    ToggleButton mTempOnOff;
    @BindView(R.id.temp_cold_ly)
    LinearLayout mColdModelLy;

    private int mOnTemp = 11;
    private int mOffTemp = 16;

    public static void openActivity(Activity activity) {
        Intent intent = new Intent(activity, TemperatureActivity.class);
        activity.startActivity(intent);
    }

    @Override
    public int getLayoutId() {
        return R.layout.activity_temperature;
    }

    @Override
    public void initPresenter() {

    }

    @Override
    public void initView(Bundle savedInstanceState) {
        try {
            int[] temp = BVMManager.currentTemp();
            if (temp[0] < 0) {
                mCurrentTv.setText("温控异常");
            } else {
                mCurrentTv.setText(String.format(Locale.CHINA, "%d%s", temp[0], "℃"));
            }


            int tempModel = BVMManager.currentTempModel();
            mTempOnOff.setChecked(tempModel == 1);
            mColdModelLy.setVisibility(tempModel == 1 ? View.VISIBLE : View.GONE);

            int[] tempCold = BVMManager.getColdTemp();
            if (tempCold.length > 1) {
                mOnTemp = tempCold[0] < 4 ? 11 : tempCold[1];
                mOffTemp = tempCold[1] > 25 ? 16 : tempCold[0];
            } else {
                CustomToast.show(BVMManager.errorMsg(tempCold[0]));
            }
            mRangeSlider.setStartingMinMax(mOnTemp, mOffTemp);
        } catch (RemoteException e) {
            e.printStackTrace();
        }

        mRangeSlider.setRangeSliderListener(new RangeSlider.RangeSliderListener() {
            @Override
            public void onMaxChanged(int newValue) {
                mOffTemp = newValue;
            }

            @Override
            public void onMinChanged(int newValue) {
                mOnTemp = newValue;
            }
        });
    }

    @OnClick({R.id.temp_back, R.id.temp_clod, R.id.temp_heat, R.id.temp_save, R.id.temp_on_off})
    public void onClickTemperature(View view) {
        switch (view.getId()) {
            case R.id.temp_back:
                ActivityCollector.Instance().finishActivity();
                break;
            case R.id.temp_clod:
                mTempBtn[0].setBackground(getResources().getDrawable(R.drawable.bg_red_corner));
                mTempBtn[1].setBackground(getResources().getDrawable(R.drawable.border_gray_d9));
                mTempBtn[0].setTextColor(getResources().getColor(R.color.white));
                mTempBtn[1].setTextColor(getResources().getColor(R.color.black_48));
                break;
            case R.id.temp_heat:
                mTempBtn[0].setBackground(getResources().getDrawable(R.drawable.border_gray_d9));
                mTempBtn[1].setBackground(getResources().getDrawable(R.drawable.bg_red_corner));
                mTempBtn[0].setTextColor(getResources().getColor(R.color.black_48));
                mTempBtn[1].setTextColor(getResources().getColor(R.color.white));
                break;
            case R.id.temp_save:
                setTemperatureCold();
                break;
            case R.id.temp_on_off:
                try {
                    int code = BVMManager.setHeatColdModel(mTempOnOff.isChecked() ? 1 : 0);
                    //保存手动控制制冷状态
                    ConfigUtil.Instance().saveInteger(ConfigUtil.FROZEN, mTempOnOff.isChecked() ? 1 : 0);
                    if (!BVMManager.isSuccess(code)) {
                        CustomToast.show(BVMManager.errorMsg(code));
                    } else {
                        mColdModelLy.setVisibility(mTempOnOff.isChecked() ? View.VISIBLE : View.GONE);
                    }
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                break;
        }
    }

    private void setTemperatureCold() {
        baseShow();
        Observable.just("")
                .observeOn(Schedulers.io())
                .flatMap(new Function<String, ObservableSource<String>>() {
                    @Override
                    public ObservableSource<String> apply(String s) throws Exception {
                        if (!mTempOnOff.isChecked()) {
                            return Observable.just("请先开启制冷模式");
                        } else if (mOffTemp - mOnTemp >= 4) {
                            int code = BVMManager.setHeatColdModel(1);
                            if (BVMManager.isSuccess(code)) {
                                code = BVMManager.setColdModel(1);
                                if (BVMManager.isSuccess(code)) {
                                    code = BVMManager.setColdTemp(mOffTemp, mOnTemp);
                                    if (BVMManager.isSuccess(code)) {
                                        return Observable.just("保存成功");
                                    }
                                }
                            }
                            return Observable.just(BVMManager.errorMsg(code));
                        } else {
                            return Observable.just("温度差不能低于4℃");
                        }
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new RxProgress<String>() {

                    @Override
                    protected void onOverSubscribe(Disposable d) {
                        baseHide();
                    }

                    @Override
                    protected void onOverNext(String s) {
                        CustomToast.show(s);
                    }

                    @Override
                    protected void onOverError(int code, String msg) {

                    }
                });
    }

    @Override
    protected void onDestroy() {
        if (mDefaultTitle != null) {
            mDefaultTitle.destroy();
        }
        super.onDestroy();
    }
}
