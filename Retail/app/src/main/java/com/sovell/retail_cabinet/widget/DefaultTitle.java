package com.sovell.retail_cabinet.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.sovell.retail_cabinet.R;
import com.sovell.retail_cabinet.bean.TimeBean;
import com.sovell.retail_cabinet.https.RxBus;
import com.sovell.retail_cabinet.utils.ConfigUtil;
import com.sovell.retail_cabinet.utils.FormatUtil;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnLongClick;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;

public class DefaultTitle extends RelativeLayout {

    @BindView(R.id.def_title_name)
    TextView mTitleName;
    @BindView(R.id.def_title_time)
    TextView mTitleTime;
    @BindView(R.id.def_title_phone)
    TextView mTitlePhone;
    @BindView(R.id.rl_def_parent)
    RelativeLayout rlDefParent;

    private Disposable mDisposable;
    private OnLongClickTitleListener mListener;

    public DefaultTitle(Context context) {
        super(context);
    }

    public DefaultTitle(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public DefaultTitle(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public DefaultTitle(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        View view = LayoutInflater.from(context).inflate(R.layout.layout_def_title, this);
        ButterKnife.bind(this);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.DefaultTitle);
        boolean isBgTransparent = a.getBoolean(R.styleable.DefaultTitle_bgTransparent, false);
        a.recycle();

        if (isBgTransparent) {
            rlDefParent.setBackground(ContextCompat.getDrawable(context, R.drawable.bg_change_black));
        } else {
            rlDefParent.setBackgroundColor(getResources().getColor(R.color.black_48));
        }
        mTitleName.setText(ConfigUtil.Instance().getString(ConfigUtil.TERM_NAME));
        mTitlePhone.setText(String.format("%s%s", "客服电话 ", ConfigUtil.Instance().getString(ConfigUtil.PHONE)));
        mTitleTime.setText(FormatUtil.getHourStr());

        mDisposable = RxBus.get().toObservable(TimeBean.class)
                .subscribe(new Consumer<TimeBean>() {
                    @Override
                    public void accept(TimeBean timeBean) throws Exception {
                        mTitleTime.setText(timeBean.getTime());
                    }
                });
    }

    public void setTitleTime(String time) {
        mTitleTime.setText(time);
    }

    @OnLongClick(R.id.def_title_name)
    public boolean onLongClickDefTitle(View view) {
        if (mListener != null) {
            mListener.OnLongClickTitle();
        }
        return true;
    }

    public interface OnLongClickTitleListener {
        void OnLongClickTitle();
    }

    public void setOnLongClickTitleListener(OnLongClickTitleListener listener) {
        mListener = listener;
    }

    public void destroy() {
        if (mDisposable != null) {
            mDisposable.dispose();
        }
    }
}
