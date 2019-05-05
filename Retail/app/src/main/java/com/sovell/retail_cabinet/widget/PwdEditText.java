package com.sovell.retail_cabinet.widget;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.MotionEvent;

import com.sovell.retail_cabinet.R;

public class PwdEditText extends android.support.v7.widget.AppCompatEditText {

    private Drawable mRightDrawable;

    public PwdEditText(Context context) {
        this(context, null);
    }

    public PwdEditText(Context context, AttributeSet attrs) {
        //这里构造方法也很重要，不加这个很多属性不能再XML里面定义
        this(context, attrs, android.R.attr.editTextStyle);
    }

    public PwdEditText(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        //获取EditText的DrawableRight,假如没有设置我们就使用默认的图片,左上右下
        mRightDrawable = getCompoundDrawables()[2];

        if (mRightDrawable == null) {
            mRightDrawable = getResources().getDrawable(R.drawable.ic_clear);
        }

        if (mRightDrawable != null) {
            mRightDrawable.setBounds(0, 0, 25, 25);
            String content = getText().toString().trim();
            if (!TextUtils.isEmpty(content)) {
                //初始化内容不为空，则不隐藏右侧图标
                setRightIconVisible(true);
                setSelection(content.length());
            } else {
                setRightIconVisible(false);//隐藏右侧图标
            }

            //设置输入框里面内容发生改变的监听
            addTextChangedListener(new TextWatcher() {
                /**
                 * 当输入框里面内容发生变化的时候回调的方法
                 */
                @Override
                public void onTextChanged(CharSequence s, int start, int count, int after) {
                    //如果是带有清除功能的类型，当文本内容发生变化的时候，根据内容的长度是否为0进行隐藏或显示
                    setRightIconVisible(s.length() > 0);

                    if (textListener != null) {
                        textListener.onTextChanged(s, start, count, after);
                    }
                }

                @Override
                public void beforeTextChanged(CharSequence s, int start, int count,
                                              int after) {
                    if (textListener != null) {
                        textListener.beforeTextChanged(s, start, count, after);
                    }
                }

                @Override
                public void afterTextChanged(Editable s) {
                    if (textListener != null) {
                        textListener.afterTextChanged(s);
                    }
                }

            });
        }
    }


    /**
     * 因为我们不能直接给EditText设置点击事件，所以我们用记住我们按下的位置来模拟点击事件
     * 当我们按下的位置 在  EditText的宽度 - 图标到控件右边的间距 - 图标的宽度  和
     * EditText的宽度 - 图标到控件右边的间距之间我们就算点击了图标，竖直方向就没有考虑
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_UP) {
            if (getCompoundDrawables()[2] != null) {
                boolean isTouched = event.getX() > (getWidth() - getTotalPaddingRight())
                        && (event.getX() < ((getWidth() - getPaddingRight())));

                if (isTouched) {
                    this.setText("");
                }
            }
        }

        return super.onTouchEvent(event);
    }


    /**
     * 设置右侧图标的显示与隐藏，调用setCompoundDrawables为EditText绘制上去
     *
     * @param visible
     */
    protected void setRightIconVisible(boolean visible) {
        Drawable right = visible ? mRightDrawable : null;
        setCompoundDrawables(getCompoundDrawables()[0],
                getCompoundDrawables()[1], right, getCompoundDrawables()[3]);
    }


    private TextListener textListener;

    public void addTextListener(TextListener textListener) {
        this.textListener = textListener;
    }

    /**
     * 输入框文本变化的回调，如果需要进行多一些操作判断，则设置此listen替代TextWatcher
     */
    public interface TextListener {

        void onTextChanged(CharSequence s, int start, int count, int after);

        void beforeTextChanged(CharSequence s, int start, int count, int after);

        void afterTextChanged(Editable s);
    }

}
