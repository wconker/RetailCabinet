package com.sovell.retail_cabinet.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.sovell.retail_cabinet.R;

@SuppressLint("ViewConstructor")
public class BannerView extends FrameLayout {

    private OnBannerViewListener mListener;

    public BannerView(@NonNull Context context) {
        super(context);
        View view = LayoutInflater.from(context).inflate(R.layout.layout_banner_view, this);

        ImageView imageView = view.findViewById(R.id.banner_view);
        imageView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) {
                    mListener.OnClickBannerView();
                }
            }
        });
    }

    public interface OnBannerViewListener {
        void OnClickBannerView();
    }

    public void setOnBannerViewListener(OnBannerViewListener listener) {
        mListener = listener;
    }

}
