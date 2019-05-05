package com.sovell.retail_cabinet.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;

import com.sovell.retail_cabinet.R;
import com.sovell.retail_cabinet.adapter.CabinetAdapter;
import com.sovell.retail_cabinet.adapter.StockAdapter;
import com.sovell.retail_cabinet.base.BaseActivity;
import com.sovell.retail_cabinet.bean.GoodsBean;
import com.sovell.retail_cabinet.bean.ProdBean;
import com.sovell.retail_cabinet.bean.TermSignIn;
import com.sovell.retail_cabinet.https.RxException;
import com.sovell.retail_cabinet.https.RxProgress;
import com.sovell.retail_cabinet.manager.ActivityCollector;
import com.sovell.retail_cabinet.manager.BVMManager;
import com.sovell.retail_cabinet.manager.DBManager;
import com.sovell.retail_cabinet.presenter.contract.CabinetContract;
import com.sovell.retail_cabinet.presenter.impl.CabinetPresenterImpl;
import com.sovell.retail_cabinet.utils.ConfigUtil;
import com.sovell.retail_cabinet.widget.CustomToast;
import com.sovell.retail_cabinet.widget.DefaultTitle;
import com.sovell.retail_cabinet.widget.GoodsListDialog;
import com.sovell.retail_cabinet.widget.SelectDialog;
import com.sovell.retail_cabinet.widget.StockDialog;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;
import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

/**
 * 上货功能
 */
public class CabinetActivity extends BaseActivity implements CabinetContract {

    @BindView(R.id.def_title)
    DefaultTitle mDefaultTitle;
    @BindView(R.id.cabinet_list)
    RecyclerView mCabinetList;

    private GoodsListDialog mGoodsListDialog;
    private StockDialog mStockDialog;
    private SelectDialog mSelectDialog;

    private CabinetAdapter mCabinetAdapter;
    private StockAdapter mStockAdapter;
    private CabinetPresenterImpl mCabinetPresenter;

    public static void openActivity(Activity activity) {
        Intent intent = new Intent(activity, CabinetActivity.class);
        activity.startActivity(intent);
    }

    @Override
    public int getLayoutId() {
        return R.layout.activity_cabinet;
    }

    @Override
    public void initPresenter() {
        mCabinetPresenter = new CabinetPresenterImpl(this);
    }

    @Override
    public void initView(Bundle savedInstanceState) {

    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoodsListDialog = new GoodsListDialog(this);
        mGoodsListDialog.setOnSelectNewGoodsListener(new GoodsListDialog.OnSelectNewGoodsListener() {
            @Override
            public void OnSelectNewGoods(GoodsBean goodsBean) {
                mGoodsListDialog.dismiss();
                mStockDialog.show();
                mStockDialog.setGoodsBean(goodsBean);
            }
        });

        mStockDialog = new StockDialog(this);
        mStockDialog.setOnClickChangeGoodsListener(new StockDialog.OnClickChangeGoodsListener() {
            @Override
            public void OnClickChangeGoods(GoodsBean nowGoods) {
                mGoodsListDialog.show();
                mGoodsListDialog.setBoxId(nowGoods.getRow(), nowGoods.getColumn());
            }

            @Override
            public void OnClickSaveGoods(GoodsBean nowGoods, List<GoodsBean> goodsList) {
                baseShow();
                mCabinetPresenter.stockCheck(nowGoods, goodsList);
            }
        });

        mSelectDialog = new SelectDialog(this);
        mSelectDialog.setOnClickSelectDialogListener(new SelectDialog.OnClickSelectDialogListener() {
            @Override
            public void OnClickSelectDialog(boolean isClickSure, int mode) {
                if (isClickSure && mode == SelectDialog.MODE_CLEAR) {
                    clearStock();
                }
            }
        });

        mCabinetAdapter = new CabinetAdapter(this, StockAdapter.TYPE_ADD);
        mCabinetList.setLayoutManager(new LinearLayoutManager(this));
        mCabinetList.setAdapter(mCabinetAdapter);
        mCabinetAdapter.setOnClickGoodsItemListener(new CabinetAdapter.OnClickGoodsItemListener() {
            @Override
            public void OnClickGoodsItemStock(StockAdapter adapter, GoodsBean goodsBean, int position, int index) {
                mStockAdapter = adapter;
                if (!TextUtils.isEmpty(goodsBean.getProdname())) {
                    mStockDialog.show();
                    mStockDialog.setGoodsBean(goodsBean);
                } else {
                    mGoodsListDialog.show();
                    mGoodsListDialog.setBoxId(position, index);
                }
            }

            @Override
            public void OnClickGoodsItemTest(GoodsBean goodsBean, int position, int index) {

            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        baseShow();
        initCabinet();
    }

    @OnClick({R.id.cabinet_back, R.id.cabinet_clear})
    public void onClickGoods(View view) {
        if (view.getId() == R.id.cabinet_back) {
            ActivityCollector.Instance().finishActivity();
        } else {
            mSelectDialog.show();
            mSelectDialog.setContent(SelectDialog.MODE_CLEAR, "确定清空全货道？");
        }
    }

    private void initCabinet() {
        Observable.just("")
                .observeOn(Schedulers.io())
                .flatMap(new Function<String, ObservableSource<List<List<GoodsBean>>>>() {
                    @Override
                    public ObservableSource<List<List<GoodsBean>>> apply(@NonNull String s) throws Exception {
                        //查询本地商品数据库
                        List<GoodsBean> goodsList = DBManager.findAll();
                        //设备的箱格数据，共有几行几列
                        int[] cabinet = BVMManager.goodsDetail();
                        if (goodsList.size() <= 0) {
                            if (cabinet[0] < 0) {
                                return Observable.error(new RxException(RxException.NET_ERROR, BVMManager.errorMsg(cabinet[0])));
                            } else {
                                //第一次安装并保存数据
                                return Observable.just(DBManager.initCabinet(cabinet));
                            }
                        } else {
                            //将本地商品数据，按行分组
                            List<List<GoodsBean>> cabinetList = new ArrayList<>();
                            for (int i = 0; i < cabinet.length; i++) {
                                cabinetList.add(new ArrayList<GoodsBean>());
                            }
                            for (GoodsBean goods : goodsList) {
                                cabinetList.get(goods.getRow() - 1).add(goods);
                            }
                            //并按每列的箱格号排序
                            for (List<GoodsBean> list : cabinetList) {
                                Collections.sort(list, new Comparator<GoodsBean>() {
                                    public int compare(GoodsBean o1, GoodsBean o2) {
                                        return o1.getColumn() - o2.getColumn();
                                    }
                                });
                            }
                            return Observable.just(cabinetList);
                        }
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new RxProgress<List<List<GoodsBean>>>() {
                    @Override
                    public void onOverSubscribe(Disposable d) {

                    }

                    @Override
                    public void onOverNext(List<List<GoodsBean>> cabinetList) {
                        mCabinetAdapter.setCabinetList(cabinetList);
                        mCabinetPresenter.getProdList();
                        ConfigUtil.Instance().saveBoolean(ConfigUtil.CABINET_INIT, true);
                    }

                    @Override
                    public void onOverError(int code, String msg) {
                        baseHide();
                        CustomToast.show(msg);
                    }
                });
    }

    private void clearStock() {
        baseShow();
        Observable.just("")
                .observeOn(Schedulers.io())
                .flatMap(new Function<String, ObservableSource<List<List<GoodsBean>>>>() {
                    @Override
                    public ObservableSource<List<List<GoodsBean>>> apply(String s) throws Exception {
                        List<List<GoodsBean>> cabinetList = new ArrayList<>(mCabinetAdapter.getCabinetList());
                        for (List<GoodsBean> rowList : cabinetList) {
                            for (GoodsBean goods : rowList) {
                                goods.setStock(0);
                                DBManager.updateById(goods);
                            }
                        }
                        return Observable.just(cabinetList);
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new RxProgress<List<List<GoodsBean>>>() {
                    @Override
                    public void onOverSubscribe(Disposable d) {
                    }

                    @Override
                    public void onOverNext(List<List<GoodsBean>> cabinetList) {
                        mCabinetAdapter.notifyDataSetChanged();
                        baseHide();
                    }

                    @Override
                    public void onOverError(int code, String msg) {
                        baseHide();
                    }
                });
    }

    @Override
    public void onProdListSuccess(ProdBean prodBean) {
        mGoodsListDialog.setProdList(prodBean.getList());
        baseHide();
    }

    @Override
    public void onProdListFailed(int code, String msg) {
        baseHide();
        CustomToast.show(msg);
    }

    @Override
    public void onStockUpdateSuccess(GoodsBean goods, TermSignIn termSignIn) {
        baseHide();
        mStockAdapter.updateGoods(goods);
        mStockDialog.dismiss();
    }

    @Override
    public void onStockUpdateFailed(int code, String msg) {
        baseHide();
        CustomToast.show(msg);
    }

    @Override
    protected void onDestroy() {
        if (mDefaultTitle != null) {
            mDefaultTitle.destroy();
        }
        if (mStockDialog != null) {
            mStockDialog.dismiss();
        }
        if (mGoodsListDialog != null) {
            mGoodsListDialog.dismiss();
        }
        if (mSelectDialog != null) {
            mSelectDialog.dismiss();
        }
        mCabinetPresenter.cancelRequest();
        super.onDestroy();
    }
}
