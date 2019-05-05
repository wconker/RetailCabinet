package com.sovell.retail_cabinet.base;


import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.sovell.retail_cabinet.event.OnScreenTouchListener;
import com.sovell.retail_cabinet.ui.CVMainActivity;

import butterknife.ButterKnife;

/**
 * 所有Fragment的父类
 * 初始化 Presenter
 * 回收资源
 */

public abstract class BaseFragment extends Fragment implements OnScreenTouchListener {

    protected View rootView;

    public abstract int getLayoutId();

    public abstract void initPresenter();

    protected Context mContext;

    protected CVMainActivity mainActivity;
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.mContext = context;
        //获得父Activity实例
        mainActivity = (CVMainActivity) mContext;

        mainActivity.setOnScreenTouch(this);
    }


    public abstract void initView(View view, Bundle savedInstanceState);

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(getLayoutId(), container, false);
        return rootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);
        this.initView(view, savedInstanceState);
        this.initPresenter();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }




    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }
}
