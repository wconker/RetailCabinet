package com.sovell.retail_cabinet.widget;

import android.content.Context;
import android.graphics.PixelFormat;
import android.view.Gravity;
import android.view.WindowManager;

public class BannerWindow {

    private final WindowManager mWindowManager;
    private final WindowManager.LayoutParams mLayoutParams;
    private BannerView mBannerView;
    private boolean isShowing;

    public BannerWindow(Context context) {
        mWindowManager = (WindowManager) context.getSystemService(context.WINDOW_SERVICE);
        mLayoutParams = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_ATTACHED_DIALOG,
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL |
                        WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH |
                        WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                PixelFormat.TRANSLUCENT);
        mLayoutParams.gravity = Gravity.CENTER;
        mLayoutParams.windowAnimations = android.R.style.Animation_Translucent;

        mBannerView = new BannerView(context);
        mBannerView.setOnBannerViewListener(new BannerView.OnBannerViewListener() {
            @Override
            public void OnClickBannerView() {
                dismiss();
                if (onBannerViewListener == null) return;
                onBannerViewListener.onDismiss();
            }
        });
    }

    public synchronized void show() {
        if (!isShowing) {
            isShowing = true;
            mWindowManager.addView(mBannerView, mLayoutParams);
        }
    }

    public synchronized void dismiss() {
        if (isShowing) {
            isShowing = false;
            mWindowManager.removeView(mBannerView);
        }
    }

    private OnBannerViewListener onBannerViewListener;

    public interface OnBannerViewListener {
        void onDismiss();
    }

    public void setOnBannerViewListener(OnBannerViewListener onBannerViewListener) {
        this.onBannerViewListener = onBannerViewListener;
    }

}
