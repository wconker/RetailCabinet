package com.sovell.retail_cabinet.widget;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.TextView;

import com.sovell.retail_cabinet.R;
import com.sovell.retail_cabinet.base.BaseDialog;

import butterknife.BindView;
import butterknife.BindViews;
import butterknife.OnClick;

public class InputDialog extends BaseDialog {

    public static final int MODE_API = 1;
    public static final int MODE_SHOP = 2;
    public static final int MODE_TERM = 3;
    public static final int MODE_KEY = 4;
    public static final int MODE_CODE = 5;
    public static final int MODE_ENTER_SET = 6;

    @BindView(R.id.dialog_input)
    PwdEditText mInputEdt;
    @BindView(R.id.dialog_title)
    TextView mTitleTv;
    @BindView(R.id.dialog_hint)
    TextView mHintTv;
    @BindViews({R.id.dialog_cancel, R.id.dialog_sure})
    Button[] mDialogBtn;

    private int mInputMode;
    private OnInputDialogListener mListener;

    public InputDialog(Context context) {
        super(context);
    }

    @Override
    public int getLayoutId() {
        return R.layout.dialog_input;
    }

    @Override
    public void initView(Bundle savedInstanceState) {

    }

    @OnClick({R.id.dialog_cancel, R.id.dialog_sure})
    public void onClickInputDialog(View view) {
        if (view.getId() == R.id.dialog_sure) {
            String content = mInputEdt.getText().toString();
            if (TextUtils.isEmpty(content)) {
                CustomToast.show("内容不能为空");
            } else if (mInputMode == MODE_CODE) {
                setClickAble(false);
                mHintTv.setText("正在配对...");
                mListener.onInputDialogPairing(content);
            } else {
                mListener.onInputDialogContent(mInputMode, content);
                dismiss();
            }
        } else {
            dismiss();
        }
    }

    private void setClickAble(boolean able) {
        mDialogBtn[0].setClickable(able);
        mDialogBtn[1].setClickable(able);
    }

    @Override
    public void dismiss() {
        if (mInputEdt != null) {
            hideKeyboard();
            mInputEdt.setText("");
            mHintTv.setText("");
            setClickAble(true);
        }
        super.dismiss();
    }

    public void setHintText(String hint) {
        mHintTv.setText(hint);
        setClickAble(true);
    }

    /**
     * 初始化输入框
     *
     * @param mode    模式
     * @param content 内容
     */
    public void initDialog(int mode, String content) {
        mInputMode = mode;
        if (mode == MODE_API) {
            mTitleTv.setText("API地址");
            mInputEdt.setHint("请输入");
            mInputEdt.setInputType(InputType.TYPE_CLASS_TEXT);
            if (TextUtils.isEmpty(content)) {
                content = "http://";
            }
        } else if (mode == MODE_SHOP) {
            mTitleTv.setText("餐厅编号");
            mInputEdt.setHint("请输入");
            mInputEdt.setInputType(InputType.TYPE_CLASS_NUMBER);
        } else if (mode == MODE_TERM) {
            mTitleTv.setText("终端编号");
            mInputEdt.setHint("请输入");
            mInputEdt.setInputType(InputType.TYPE_CLASS_NUMBER);
        } else if (mode == MODE_KEY) {
            mTitleTv.setText("通讯密钥");
            mInputEdt.setHint("请输入");
            mInputEdt.setInputType(InputType.TYPE_CLASS_TEXT);
        } else if (mode == MODE_CODE) {
            mTitleTv.setText("配对码");
            mInputEdt.setHint("请输入6位配对码");
            mInputEdt.setInputType(InputType.TYPE_CLASS_NUMBER);
            mInputEdt.setFilters(new InputFilter[]{new InputFilter.LengthFilter(6)});
        } else if (mode == MODE_ENTER_SET) {
            mTitleTv.setText("设置密码");
            mInputEdt.setHint("请输入设置密码");
            mInputEdt.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        }
        mInputEdt.setText(content);
    }

    /**
     * 隐藏软键盘
     */
    private void hideKeyboard() {
        View view = getCurrentFocus();
        if (view instanceof TextView) {
            InputMethodManager mInputMethodManager = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            mInputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.RESULT_UNCHANGED_SHOWN);
        }
    }

    public interface OnInputDialogListener {
        void onInputDialogContent(int inputMode, String content);

        void onInputDialogPairing(String code);
    }

    public void setOnInputDialogListener(OnInputDialogListener listener) {
        mListener = listener;
    }
}
