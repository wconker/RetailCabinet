package com.sovell.retail_cabinet.widget;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.sovell.retail_cabinet.R;
import com.sovell.retail_cabinet.base.BaseDialog;

import butterknife.BindView;
import butterknife.OnClick;

public class SelectDialog extends BaseDialog {

    //终端初始化
    public static final int MODE_INIT = 1;
    //开闸门
    public static final int MODE_DOOR = 2;
    //清空错误
    public static final int MODE_FAILED_CLEAR = 3;
    //退出基础配置界面
    public static final int MODE_FINISH_BASIC = 4;
    //清空全货到
    public static final int MODE_CLEAR = 5;

    @BindView(R.id.dialog_select_content)
    TextView mContentTv;

    private int mMode;
    private OnClickSelectDialogListener mListener;

    public SelectDialog(Context context) {
        super(context);
    }

    @Override
    public int getLayoutId() {
        return R.layout.dialog_select;
    }

    @Override
    public void initView(Bundle savedInstanceState) {

    }

    public void setContent(int mode, String content) {
        mMode = mode;
        mContentTv.setText(content);
    }

    @OnClick({R.id.dialog_select_cancel, R.id.dialog_select_sure})
    public void onClickSelectDialog(View view) {
        dismiss();
        mListener.OnClickSelectDialog(view.getId() == R.id.dialog_select_sure, mMode);
    }

    @Override
    public void dismiss() {
        if (mContentTv != null) {
            mContentTv.setText("");
        }
        super.dismiss();
    }

    public interface OnClickSelectDialogListener {
        void OnClickSelectDialog(boolean isClickSure, int mode);
    }

    public void setOnClickSelectDialogListener(OnClickSelectDialogListener listener) {
        mListener = listener;
    }
}
