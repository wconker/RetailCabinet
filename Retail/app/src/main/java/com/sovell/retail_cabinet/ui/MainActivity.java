package com.sovell.retail_cabinet.ui;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;

import com.sovell.retail_cabinet.R;
import com.sovell.retail_cabinet.adapter.DishItemDecoration;
import com.sovell.retail_cabinet.adapter.ProdListAdapter;
import com.sovell.retail_cabinet.adapter.TabAdapter;
import com.sovell.retail_cabinet.base.BaseActivity;
import com.sovell.retail_cabinet.bean.GoodsBean;
import com.sovell.retail_cabinet.bean.TypeBean;
import com.sovell.retail_cabinet.glide.GlideImageLoader;
import com.sovell.retail_cabinet.manager.ActivityCollector;
import com.sovell.retail_cabinet.manager.BVMManager;
import com.sovell.retail_cabinet.manager.DBManager;
import com.sovell.retail_cabinet.manager.PayLogManager;
import com.sovell.retail_cabinet.presenter.impl.MainPresenterImpl;
import com.sovell.retail_cabinet.utils.ConfigUtil;
import com.sovell.retail_cabinet.widget.BannerWindow;
import com.sovell.retail_cabinet.widget.CustomToast;
import com.sovell.retail_cabinet.widget.DefaultTitle;
import com.sovell.retail_cabinet.widget.InputDialog;
import com.sovell.retail_cabinet.widget.MainSearchDialog;
import com.sovell.retail_cabinet.widget.PayDialog;
import com.youth.banner.Banner;
import com.youth.banner.BannerConfig;
import com.youth.banner.Transformer;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.OnClick;
import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

public class MainActivity extends BaseActivity {

    @BindView(R.id.main_tab_list)
    RecyclerView mCategoryList;
    @BindView(R.id.main_dish_list)
    RecyclerView mDishRv;
    @BindView(R.id.banner)
    Banner mBanner;
    @BindView(R.id.main_def_title)
    DefaultTitle mDefaultTitle;

    private BannerWindow mBannerWindow;
    private TabAdapter tabAdapter;
    private ProdListAdapter mDishAdapter;
    private MainSearchDialog mainSearchDialog;
    private PayDialog payDialog;
    private InputDialog mInputDialog;
    private MainPresenterImpl mMainPresenter;
    /*返回结果计时*/
    private Disposable mTimerResultDisposable;
    /*倒计时关闭页面(秒)*/
    private int mInterval;

    public static void openActivity(Activity activity, ArrayList<String> images) {
        Intent intent = new Intent(activity, MainActivity.class);
        intent.putStringArrayListExtra("images", images);
        activity.startActivity(intent);
    }

    @Override
    public int getLayoutId() {
        return R.layout.activity_main;
    }

    @Override
    public void initPresenter() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    BVMManager.initSetKey();
                    BVMManager.maintainState(false);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                final List<GoodsBean> goodsList = DBManager.findAllById();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        initMainViewData(goodsList);
                        mMainPresenter = new MainPresenterImpl();
                        mMainPresenter.intervalKeep();
                        mMainPresenter.uploadTermStatus();
                        mMainPresenter.stockUpload(goodsList);
                    }
                });
            }
        }).start();
        PayLogManager.getInstance().intervalRefund();
        //pass退款轮询
        PayLogManager.getInstance().passIntervalRefund();
        startTimer();
    }

    @Override
    public void initView(Bundle savedInstanceState) {
        mInterval = ConfigUtil.Instance().getExitSecond(ConfigUtil.Instance().getInteger(ConfigUtil.EXIT_TIME));
        ArrayList<String> imageList = new ArrayList<>();
        Intent intent = getIntent();
        if (intent != null) {
            imageList = intent.getStringArrayListExtra("images");
        }

        if (imageList != null && imageList.size() > 0) {
            mBanner.setImages(imageList);
        } else {
            List<Integer> images = new ArrayList<>();
            images.add(R.drawable.ic_ad_big);
            mBanner.setImages(images);
        }
        mBanner.setImageLoader(new GlideImageLoader());
        mBanner.setDelayTime(ConfigUtil.Instance().getInteger(ConfigUtil.AD_INTERVAL) * 1000);
        mBanner.setBannerAnimation(Transformer.Accordion);
        mBanner.setBannerStyle(BannerConfig.CIRCLE_INDICATOR);
        mBanner.setIndicatorGravity(BannerConfig.CENTER);
        mBanner.start();

        mBannerWindow = new BannerWindow(this);
        mBannerWindow.setOnBannerViewListener(new BannerWindow.OnBannerViewListener() {
            @Override
            public void onDismiss() {
                startTimer();
            }
        });

        mainSearchDialog = new MainSearchDialog(this);
        mainSearchDialog.setOnReadCardDialogListener(new MainSearchDialog.OnSearchClickListener() {
            @Override
            public void onSearchClick(GoodsBean goodsBean) {
                //获取全部商品
                List<GoodsBean> prodsList = tabAdapter.getTabLists().get(0).getProds();
                for (GoodsBean bean : prodsList) {
                    if (TextUtils.equals(bean.getProdid(), goodsBean.getProdid())) {
                        mainSearchDialog.dismiss();
                        payDialog.setGoodData(bean);
                        break;
                    }
                }
            }

            @Override
            public void onTouchEvent() {
                startTimer();
            }
        });
        payDialog = new PayDialog(this);
        payDialog.setOnPayDidlogListener(new PayDialog.OnPayDialogListener() {
            @Override
            public void onPayDialog(int funFlag) {

            }

            @Override
            public void onNotifyView(GoodsBean goodsBean) {
                mDishAdapter.notifyDataSetChanged();
            }
        });
        payDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                startTimer();
            }
        });

        mInputDialog = new InputDialog(this);
        mInputDialog.setOnInputDialogListener(new InputDialog.OnInputDialogListener() {
            @Override
            public void onInputDialogContent(int inputMode, String content) {
                if (TextUtils.equals(content, ConfigUtil.ENTER_SET_PWD)) {
                    mMainPresenter.cancelRequest();
                    PayLogManager.getInstance().close();
                    closeTimer();
                    ActivityCollector.Instance().finishAllActivity();
                    SetActivity.openActivity(MainActivity.this);
                } else {
                    CustomToast.show("密码错误");
                }
            }

            @Override
            public void onInputDialogPairing(String code) {

            }
        });

        mDefaultTitle.setOnLongClickTitleListener(new DefaultTitle.OnLongClickTitleListener() {
            @Override
            public void OnLongClickTitle() {
                mInputDialog.show();
                mInputDialog.initDialog(InputDialog.MODE_ENTER_SET, "");
            }
        });

        initRecyclerView();
    }

    private void initRecyclerView() {
        //分类菜单栏
        tabAdapter = new TabAdapter(this);
        tabAdapter.setOnItemClickListener(new TabAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(TypeBean dishBean, int position) {
                tabAdapter.setSelectPos(position);
                //刷新菜品
                mDishAdapter.setProdList(dishBean.getProds());
            }
        });
        LinearLayoutManager manager = new LinearLayoutManager(this);
        manager.setOrientation(LinearLayoutManager.HORIZONTAL);
        mCategoryList.setLayoutManager(manager);
        mCategoryList.setAdapter(tabAdapter);

        //商品栏
        mDishAdapter = new ProdListAdapter(this, ProdListAdapter.MIAN_RV);
        mDishAdapter.setOnClickProdItemListener(new ProdListAdapter.OnClickProdItemListener() {
            @Override
            public void onClickProdItem(GoodsBean goodsBean, int position) {
                //菜品点击事件
                if (goodsBean.getStock() > 0) {
                    payDialog.setGoodData(goodsBean);
                    closeTimer();
                }
            }
        });
        GridLayoutManager dishManager = new GridLayoutManager(this, 3);
        mDishRv.setLayoutManager(dishManager);
        mDishRv.addItemDecoration(new DishItemDecoration(this, 3));
        ((DefaultItemAnimator) mDishRv.getItemAnimator()).setSupportsChangeAnimations(false);
        mDishRv.setAdapter(mDishAdapter);
    }

    private void initMainViewData(List<GoodsBean> goodsList) {
        List<TypeBean> typeList = new ArrayList<>();
        TypeBean typeBean = new TypeBean();
        typeBean.setCatename("全部");
        typeBean.setProds(goodsList);
        typeList.add(typeBean);
        for (GoodsBean goodsBean : goodsList) {
            TypeBean typeGoodsBean = new TypeBean();
            typeGoodsBean.setCateid(goodsBean.getCateid());
            if (!typeList.contains(typeGoodsBean)) {
                typeGoodsBean.setCatename(goodsBean.getCatename());
                typeGoodsBean.setCateno(goodsBean.getCateno());
                typeGoodsBean.setProds(new ArrayList<GoodsBean>());
                typeList.add(typeGoodsBean);
            }
        }
        for (TypeBean typeBean1 : typeList) {
            if (TextUtils.equals(typeBean1.getCatename(), "全部")) continue;
            for (GoodsBean goodsBean : goodsList) {
                if (TextUtils.equals(typeBean1.getCateid(), goodsBean.getCateid())) {
                    typeBean1.getProds().add(goodsBean);
                }
            }
        }

        tabAdapter.setTabLists(typeList);
        tabAdapter.onFirstClick(0);
    }

    @OnClick(R.id.ll_main_search)
    public void onViewClicked(View view) {
        mainSearchDialog.setListData();
        mainSearchDialog.show();
    }

    /**
     * 启动定时器
     */
    public void startTimer() {
        closeTimer();
        final int countTime = mInterval; //总时间
        Observable.interval(0, 1, TimeUnit.SECONDS)
                .take(countTime + 1)//设置总共发送的次数
                .map(new Function<Long, Long>() {
                    @Override
                    public Long apply(Long aLong) throws Exception {
                        return countTime - aLong;
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Long>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        mTimerResultDisposable = d;
                    }

                    @Override
                    public void onNext(Long value) {//计时中

                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onComplete() {//计时结束
                        closeTimer();
                        if (mainSearchDialog.isShowing()) {
                            mainSearchDialog.dismiss();
                        }
                        mBannerWindow.show();
                    }
                });
    }

    /**
     * 关闭定时器
     */
    public void closeTimer() {
        if (mTimerResultDisposable != null) {
            mTimerResultDisposable.dispose();
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        startTimer();
        return super.dispatchTouchEvent(ev);
    }

    @Override
    protected void onDestroy() {
        if (mDefaultTitle != null) {
            mDefaultTitle.destroy();
        }
        if (mMainPresenter != null) {
            mMainPresenter.cancelRequest();
        }
        if (mBannerWindow != null) {
            mBannerWindow.dismiss();
        }
        if (payDialog != null) {
            payDialog.onDestroy();
        }
        if (mainSearchDialog != null) {
            mainSearchDialog.dismiss();
        }
        if (mInputDialog != null) {
            mInputDialog.dismiss();
        }
        PayLogManager.getInstance().close();
        closeTimer();
        super.onDestroy();
    }
}
