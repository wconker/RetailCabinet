package com.sovell.retail_cabinet.base;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import com.sovell.retail_cabinet.manager.ActivityCollector;
import com.sovell.retail_cabinet.utils.DeviceUtil;
import com.sovell.retail_cabinet.widget.LoadingDialog;

import butterknife.ButterKnife;

/**
 * 所有Activity的父类
 * 初始化 Presenter
 * 回收资源
 */

public abstract class BaseActivity extends AppCompatActivity {

    public abstract int getLayoutId();

    public abstract void initPresenter();

    public abstract void initView(Bundle savedInstanceState);

    private LoadingDialog mLoadingDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        DeviceUtil.fullScreen(getWindow().getDecorView());
        super.onCreate(savedInstanceState);
        this.setContentView(this.getLayoutId());
        ButterKnife.bind(this);
        ActivityCollector.Instance().addActivity(this);
        mLoadingDialog = new LoadingDialog(this);
        this.initView(savedInstanceState);
        this.initPresenter();
    }

    public void baseShow() {
        if (!mLoadingDialog.isShowing()) {
            mLoadingDialog.show();
        }
    }

    public void baseHide() {
        if (mLoadingDialog.isShowing()) {
            mLoadingDialog.dismiss();
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        if (hasFocus) {
            DeviceUtil.fullScreen(getWindow().getDecorView());
        }
        super.onWindowFocusChanged(hasFocus);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        baseHide();
    }

    /**
     * 隐藏键盘
     */
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            View v = getCurrentFocus();
            if (isShouldHideInput(v, ev)) {
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm != null) {
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                }
            }
            return super.dispatchTouchEvent(ev);
        }
        // 必不可少，否则所有的组件都不会有TouchEvent了
        if (getWindow().superDispatchTouchEvent(ev)) {
            return true;
        }
        return onTouchEvent(ev);
    }

    /**
     * 获取EditText的位置
     */
    protected boolean isShouldHideInput(View v, MotionEvent event) {
        if (v instanceof EditText) {
            int[] leftTop = {0, 0};
            //获取输入框当前的location位置
            v.getLocationInWindow(leftTop);
            int left = leftTop[0];
            int top = leftTop[1];
            int bottom = top + v.getHeight();
            int right = left + v.getWidth();
            if (event.getX() > left && event.getX() < right
                    && event.getY() > top && event.getY() < bottom) {
                // 点击的是输入框区域，保留点击EditText的事件
                return false;
            } else {
                return true;
            }
        }
        return false;
    }
}
