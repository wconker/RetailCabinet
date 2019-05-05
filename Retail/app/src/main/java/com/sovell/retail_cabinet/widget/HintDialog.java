package com.sovell.retail_cabinet.widget;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.sovell.retail_cabinet.R;
import com.sovell.retail_cabinet.base.BaseDialog;
import com.sovell.retail_cabinet.manager.BVMManager;

import java.util.Locale;

import butterknife.BindView;
import butterknife.OnClick;

public class HintDialog extends BaseDialog {

    @BindView(R.id.dialog_hint_content)
    TextView mContentTv;
    @BindView(R.id.dialog_hint_status_ly)
    LinearLayout mStatusLy;
    @BindView(R.id.dialog_hint_status_dev)
    TextView mStatusDev;
    @BindView(R.id.dialog_hint_status_door1)
    TextView mStatusDoor1;
    @BindView(R.id.dialog_hint_status_door2)
    TextView mStatusDoor2;
    @BindView(R.id.dialog_hint_status_msg)
    TextView mStatusMsg;
    @BindView(R.id.dialog_hint_status_cabinet)
    TextView mStatusCabinet;

    public HintDialog(Context context) {
        super(context);
    }

    @Override
    public int getLayoutId() {
        return R.layout.dialog_hint;
    }

    @Override
    public void initView(Bundle savedInstanceState) {

    }

    public void setContent(String[] content) {
        StringBuilder builder = new StringBuilder();
        for (String str : content) {
            builder.append(String.format("%s\n", str));
        }
        mContentTv.setText(builder.toString());
        mContentTv.setVisibility(View.VISIBLE);
    }

    public void setStatus(int[] door, String msg, int[] goods, int state) {
        mStatusDoor1.setText(door[0] == 2 ? "开启" : "关闭");
        mStatusDoor2.setText(door[1] == 2 ? "开启" : "关闭");
        mStatusMsg.setText(msg);
        mStatusDev.setText(BVMManager.stateMsg(state));
        StringBuilder builder = new StringBuilder();
        for (int i = goods.length; i > 0; i--) {
            if (goods[i - 1] > 0) {
                builder.append(String.format(Locale.CHINA, "第%d层 %d列\n", i, goods[i - 1]));
            } else {
                builder.append("货道明细异常");
            }
        }
        mStatusCabinet.setText(builder.toString());
        mStatusLy.setVisibility(View.VISIBLE);
    }

    @OnClick(R.id.dialog_hint_sure)
    public void onClickHintDialog(View view) {
        dismiss();
    }

    @Override
    public void dismiss() {
        if (mContentTv != null) {
            mContentTv.setText("");
            mContentTv.setVisibility(View.GONE);
        }
        if (mStatusLy != null) {
            mStatusLy.setVisibility(View.GONE);
            mStatusDoor1.setText("");
            mStatusDoor2.setText("");
            mStatusMsg.setText("");
            mStatusCabinet.setText("");
        }
        super.dismiss();
    }
}
