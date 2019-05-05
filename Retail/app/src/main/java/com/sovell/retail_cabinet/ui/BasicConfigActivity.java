package com.sovell.retail_cabinet.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.sovell.retail_cabinet.R;
import com.sovell.retail_cabinet.base.BaseActivity;
import com.sovell.retail_cabinet.bean.TermPairing;
import com.sovell.retail_cabinet.bean.TermSignIn;
import com.sovell.retail_cabinet.https.Api;
import com.sovell.retail_cabinet.manager.ActivityCollector;
import com.sovell.retail_cabinet.presenter.contract.SetContract;
import com.sovell.retail_cabinet.presenter.impl.SetPresenterImpl;
import com.sovell.retail_cabinet.utils.ConfigUtil;
import com.sovell.retail_cabinet.widget.CustomToast;
import com.sovell.retail_cabinet.widget.DefaultTitle;
import com.sovell.retail_cabinet.widget.InputDialog;
import com.sovell.retail_cabinet.widget.RadioDialog;
import com.sovell.retail_cabinet.widget.RingDialog;
import com.sovell.retail_cabinet.widget.SelectDialog;
import com.sovell.retail_cabinet.widget.ShopTypeDialog;

import java.util.Locale;

import butterknife.BindView;
import butterknife.BindViews;
import butterknife.OnClick;

public class BasicConfigActivity extends BaseActivity implements SetContract {

    @BindView(R.id.def_title)
    DefaultTitle mDefaultTitle;
    @BindView(R.id.basic_protect)
    ToggleButton mProtectBtn;
    @BindView(R.id.basic_time_tv)
    TextView mExitTimeTv;
    @BindView(R.id.basic_shop_type)
    RelativeLayout basicShopType;
    @BindView(R.id.basic_shop_type_tv)
    TextView basicShoTypeTv;
    @BindViews({R.id.basic_api_tv, R.id.basic_shop_tv, R.id.basic_term_tv, R.id.basic_key_tv})
    TextView[] mBasicTv;
    @BindView(R.id.basic_code_btn)
    Button basicCodeBtn;
    private boolean doModification = false;


    private int mExitTimeIndex;
    private int mShopTypeIndex;
    private boolean isFromSet;
    private InputDialog mInputDialog;
    private RadioDialog mRadioDialog;
    private ShopTypeDialog mShopTypeDialog;
    private SelectDialog mSelectDialog;
    private SetPresenterImpl mSetPresenter;

    public static void openActivity(Activity activity, boolean isFromSet) {
        Intent intent = new Intent(activity, BasicConfigActivity.class);
        intent.putExtra("isFromSet", isFromSet);
        activity.startActivity(intent);
    }

    @Override
    public int getLayoutId() {
        return R.layout.activity_basic_config;
    }

    @Override
    public void initPresenter() {
        mSetPresenter = new SetPresenterImpl(this);
    }

    @Override
    public void initView(Bundle savedInstanceState) {
        Intent intent = getIntent();
        if (intent != null) {
            isFromSet = intent.getBooleanExtra("isFromSet", false);
        }

        mRadioDialog = new RadioDialog(this);
        mRadioDialog.setOnRadioDialogListener(new RadioDialog.OnRadioDialogListener() {
            @Override
            public void onRadioDialogResult(int index) {
                doModification = true;
                mExitTimeIndex = index;
                ConfigUtil.Instance().saveInteger(ConfigUtil.EXIT_TIME, index);
                mExitTimeTv.setText(String.format(Locale.CHINA, "%d%s", ConfigUtil.Instance().getExitSecond(mExitTimeIndex), "s"));
            }
        });


        mShopTypeDialog = new ShopTypeDialog(this);
        mShopTypeDialog.setOnRadioDialogListener(new ShopTypeDialog.OnRadioDialogListener() {
            @Override
            public void onRadioDialogResult(int index) {
                doModification = true;
                mShopTypeIndex = index;
                basicShoTypeTv.setText(String.format(Locale.CHINA, "%s", ConfigUtil.Instance().getShopType(mShopTypeIndex) == 41 ? "零售柜" : "净菜柜"));
            }
        });


        mInputDialog = new InputDialog(this);
        mInputDialog.setOnInputDialogListener(new InputDialog.OnInputDialogListener() {
            @Override
            public void onInputDialogContent(int inputMode, String content) {
                doModification = true;
                //没有通过验证的话提示且不允许退出
                if (inputMode == InputDialog.MODE_API) {
                    mBasicTv[0].setText(content);
                    basicCodeBtnBg(content);
                } else if (inputMode == InputDialog.MODE_SHOP) {
                    mBasicTv[1].setText(content);
                } else if (inputMode == InputDialog.MODE_TERM) {
                    mBasicTv[2].setText(content);
                } else if (inputMode == InputDialog.MODE_KEY) {
                    mBasicTv[3].setText(content);
                }
            }

            @Override
            public void onInputDialogPairing(String code) {
                String api = mBasicTv[0].getText().toString();
                mSetPresenter.termPairing(api, code, ConfigUtil.Instance().getShopType(mShopTypeIndex));
            }
        });

        mSelectDialog = new SelectDialog(this);
        mSelectDialog.setOnClickSelectDialogListener(new SelectDialog.OnClickSelectDialogListener() {
            @Override
            public void OnClickSelectDialog(boolean isClickSure, int mode) {
                if (isClickSure && mode == SelectDialog.MODE_FINISH_BASIC) {
                    String api = mBasicTv[0].getText().toString();
                    String shop = mBasicTv[1].getText().toString();
                    String term = mBasicTv[2].getText().toString();
                    String key = mBasicTv[3].getText().toString();
                    if (TextUtils.isEmpty(api) || TextUtils.isEmpty(shop) || TextUtils.isEmpty(term) || TextUtils.isEmpty(key)) {
                        CustomToast.show("请完善基础配置信息");
                    } else {
                        baseShow();
                        mSetPresenter.termSignIn(api, shop, term, key);
                        doModification = false;
                    }
                } else {
                    goBack();
                }
            }
        });

        mProtectBtn.setChecked(ConfigUtil.Instance().getBoolean(ConfigUtil.PROTECT));
        mExitTimeIndex = ConfigUtil.Instance().getInteger(ConfigUtil.EXIT_TIME);
        //没有值得话默认让它是零售柜
        mShopTypeIndex = ConfigUtil.Instance().getInteger(ConfigUtil.CV_TYPE) == ConfigUtil.CVDISK ? 1 : 0;

        mExitTimeTv.setText(String.format(Locale.CHINA, "%d%s", ConfigUtil.Instance().getExitSecond(mExitTimeIndex), "s"));

        basicShoTypeTv.setText(String.format(Locale.CHINA, "%s", ConfigUtil.Instance().getShopType(mShopTypeIndex) == 41 ? "零售柜" : "净菜柜"));

        String api = ConfigUtil.Instance().getString(ConfigUtil.API);

        basicCodeBtnBg(api);

        mBasicTv[0].setText(api);
        mBasicTv[1].setText(ConfigUtil.Instance().getString(ConfigUtil.SHOP, ""));
        mBasicTv[2].setText(ConfigUtil.Instance().getString(ConfigUtil.TERM, ""));
        mBasicTv[3].setText(ConfigUtil.Instance().getString(ConfigUtil.KEY));

        baseWhetherShowShopType();
    }


    private void baseWhetherShowShopType() {
        if (ConfigUtil.Instance().getInteger(ConfigUtil.CV_TYPE) != 0) {
            basicShopType.setVisibility(View.GONE);
        } else {
            basicShopType.setVisibility(View.VISIBLE);
        }
    }

    private void basicCodeBtnBg(String api) {
        if (TextUtils.isEmpty(api)) {
            basicCodeBtn.setClickable(false);
            basicCodeBtn.setBackground(getResources().getDrawable(R.drawable.bg_gray_a2_corner));
        } else {
            basicCodeBtn.setClickable(true);
            basicCodeBtn.setBackground(getResources().getDrawable(R.drawable.press_red));
        }
    }

    @OnClick({R.id.basic_api_ly, R.id.basic_shop_type, R.id.basic_shop_ly, R.id.basic_term_ly, R.id.basic_key_ly,
            R.id.basic_code_btn, R.id.basic_time_ly, R.id.basic_back, R.id.basic_protect})
    public void onClickBasic(View view) {
        switch (view.getId()) {
            case R.id.basic_protect:
                ConfigUtil.Instance().saveBoolean(ConfigUtil.PROTECT, mProtectBtn.isChecked());
                break;
            case R.id.basic_shop_type:
                mShopTypeDialog.show();
                mShopTypeDialog.initRadioView(mShopTypeIndex);
                break;
            case R.id.basic_api_ly:
                mInputDialog.show();
                mInputDialog.initDialog(InputDialog.MODE_API, mBasicTv[0].getText().toString());
                break;
            case R.id.basic_shop_ly:
                mInputDialog.show();
                mInputDialog.initDialog(InputDialog.MODE_SHOP, mBasicTv[1].getText().toString());
                break;
            case R.id.basic_term_ly:
                mInputDialog.show();
                mInputDialog.initDialog(InputDialog.MODE_TERM, mBasicTv[2].getText().toString());
                break;
            case R.id.basic_key_ly:
                mInputDialog.show();
                mInputDialog.initDialog(InputDialog.MODE_KEY, mBasicTv[3].getText().toString());
                break;
            case R.id.basic_code_btn:
                if (!TextUtils.isEmpty(mBasicTv[0].getText().toString())) {
                    mInputDialog.show();
                    mInputDialog.initDialog(InputDialog.MODE_CODE, "");
                }
                break;
            case R.id.basic_time_ly:
                mRadioDialog.show();
                mRadioDialog.initRadioView(mExitTimeIndex);
                break;
            case R.id.basic_back:
                String api = mBasicTv[0].getText().toString();
                String shop = mBasicTv[1].getText().toString();
                String term = mBasicTv[2].getText().toString();
                String key = mBasicTv[3].getText().toString();
                String apiLast = ConfigUtil.Instance().getApi("");
                String shopLast = ConfigUtil.Instance().getString(ConfigUtil.SHOP);
                String termLast = ConfigUtil.Instance().getString(ConfigUtil.TERM);
                String keyLast = ConfigUtil.Instance().getString(ConfigUtil.KEY);
                if (!ConfigUtil.Instance().getBoolean(ConfigUtil.BIND)) {
                    if (TextUtils.isEmpty(api) || TextUtils.isEmpty(shop) || TextUtils.isEmpty(term) || TextUtils.isEmpty(key)) {
                        CustomToast.show("请完善基础配置信息");
                    } else {
                        baseShow();
                        mSetPresenter.termSignIn(api, shop, term, key);
                    }
                } else {
                    if (TextUtils.equals(api, apiLast) &&
                            TextUtils.equals(shop, shopLast) &&
                            TextUtils.equals(term, termLast) &&
                            TextUtils.equals(key, keyLast)) {
                        goBack();
                    } else {
                        if (!doModification) {
                            goBack();
                        } else {
                            mSelectDialog.show();
                            mSelectDialog.setContent(SelectDialog.MODE_FINISH_BASIC, "是否保存配置信息？");
                        }
                    }
                }
                break;
        }

    }

    @Override
    public void onPairingSuccess(TermPairing termPairing) {
        mBasicTv[1].setText(termPairing.getShop());
        mBasicTv[2].setText(termPairing.getTerm());
        mBasicTv[3].setText(termPairing.getAppkey());
        mInputDialog.dismiss();
    }

    @Override
    public void onPairingFailed(int code, String msg) {
        //弹框提示去除前面的code只展示信息
        mInputDialog.setHintText(String.format(Locale.CHINA, "%s", msg));
    }

    @Override
    public void onSignInSuccess(TermSignIn termSignIn) {
        //这样写方便，再p层还需要传递参数
        if (ConfigUtil.Instance().getShopType(mShopTypeIndex) != termSignIn.getType()) {
            CustomToast.show("终端类型错误");
            baseHide();
            return;
        } else {
            ConfigUtil.Instance().saveInteger(ConfigUtil.CV_TYPE, ConfigUtil.Instance().getShopType(mShopTypeIndex));
        }

        String api = mBasicTv[0].getText().toString();
        String key = mBasicTv[3].getText().toString();
        ConfigUtil.Instance().saveString(ConfigUtil.SHOP, termSignIn.getShop());
        ConfigUtil.Instance().saveString(ConfigUtil.TERM, termSignIn.getTerm());
        ConfigUtil.Instance().saveString(ConfigUtil.TERM_NAME, termSignIn.getShop_name());
        ConfigUtil.Instance().saveString(ConfigUtil.AUTH_KEY, termSignIn.getAuthkey());
        ConfigUtil.Instance().saveString(ConfigUtil.API, api);
        ConfigUtil.Instance().saveString(ConfigUtil.KEY, key);
        ConfigUtil.Instance().saveString(ConfigUtil.PHONE, termSignIn.getOper_json().getName());
        ConfigUtil.Instance().saveInteger(ConfigUtil.COLD_MAX, termSignIn.getTemp_json().getColdmax());
        ConfigUtil.Instance().saveInteger(ConfigUtil.COLD_MIN, termSignIn.getTemp_json().getColdmin());
        ConfigUtil.Instance().saveInteger(ConfigUtil.KEEP_INTERVAL, termSignIn.getInterval());
        ConfigUtil.Instance().saveBoolean(ConfigUtil.BIND, true);
        Api.setNullForInstance();
        baseHide();
        goBack();
    }

    @Override
    public void onSignInFailed(int code, String msg) {
        baseHide();
        CustomToast.show(String.format(Locale.CHINA, "%s", msg));
    }

    private void goBack() {
        if (!ConfigUtil.Instance().getBoolean(ConfigUtil.BIND)) {
            CustomToast.show("设备未绑定");
        } else if (isFromSet) {
            ActivityCollector.Instance().finishActivity();
        } else {
            ActivityCollector.Instance().finishAllActivity();
            SetActivity.openActivity(this);
        }
    }

    @Override
    protected void onDestroy() {
        if (mDefaultTitle != null) {
            mDefaultTitle.destroy();
        }
        mSetPresenter.cancelRequest();
        if (mInputDialog != null) {
            mInputDialog.dismiss();
        }
        if (mRadioDialog != null) {
            mRadioDialog.dismiss();
        }
        if (mSelectDialog != null) {
            mSelectDialog.dismiss();
        }
        super.onDestroy();
    }
}
