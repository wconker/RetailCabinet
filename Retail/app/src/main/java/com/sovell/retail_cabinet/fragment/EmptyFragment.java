package com.sovell.retail_cabinet.fragment;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.sovell.retail_cabinet.R;
import com.sovell.retail_cabinet.base.BaseFragment;

/**
 * A simple {@link Fragment} subclass.
 */
public class EmptyFragment extends BaseFragment {


    public static EmptyFragment getInstance() {
        return new EmptyFragment();
    }

    public EmptyFragment() {
        // Required empty public constructor
    }


    @Override
    public int getLayoutId() {
        return R.layout.fragment_empty;
    }

    @Override
    public void initPresenter() {

    }

    @Override
    public void initView(View view, Bundle savedInstanceState) {

    }


    @Override
    public void screenTouch() {

    }
}
