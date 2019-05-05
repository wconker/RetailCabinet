package com.sovell.retail_cabinet.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;

import com.sovell.retail_cabinet.R;
import com.sovell.retail_cabinet.adapter.CabinetAdapter;
import com.sovell.retail_cabinet.adapter.StockAdapter;
import com.sovell.retail_cabinet.base.BaseActivity;
import com.sovell.retail_cabinet.bean.GoodsBean;
import com.sovell.retail_cabinet.https.RxException;
import com.sovell.retail_cabinet.https.RxProgress;
import com.sovell.retail_cabinet.manager.ActivityCollector;
import com.sovell.retail_cabinet.manager.BVMManager;
import com.sovell.retail_cabinet.manager.DBManager;
import com.sovell.retail_cabinet.utils.JsonUtils;
import com.sovell.retail_cabinet.widget.CustomToast;
import com.sovell.retail_cabinet.widget.DefaultTitle;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.OnClick;
import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

public class CabinetTestActivity extends BaseActivity {

    @BindView(R.id.def_title)
    DefaultTitle mDefaultTitle;
    @BindView(R.id.cabinet_test_list)
    RecyclerView mTestList;

    private CabinetAdapter mCabinetAdapter;

    public static void openActivity(Activity activity) {
        Intent intent = new Intent(activity, CabinetTestActivity.class);
        activity.startActivity(intent);
    }

    @Override
    public int getLayoutId() {
        return R.layout.activity_cabinet_test;
    }

    @Override
    public void initPresenter() {

    }

    @Override
    public void initView(Bundle savedInstanceState) {
        mCabinetAdapter = new CabinetAdapter(this, StockAdapter.TYPE_TEST);
        mTestList.setLayoutManager(new LinearLayoutManager(this));
        mTestList.setAdapter(mCabinetAdapter);
        mCabinetAdapter.setOnClickGoodsItemListener(new CabinetAdapter.OnClickGoodsItemListener() {
            @Override
            public void OnClickGoodsItemStock(StockAdapter adapter, GoodsBean goods, int position, int index) {

            }

            @Override
            public void OnClickGoodsItemTest(GoodsBean goods, int position, int index) {
                takeGoods(goods, position, index);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        initCabinet();
    }

    @OnClick(R.id.cabinet_test_back)
    public void onClickCabinetTest(View view) {
        ActivityCollector.Instance().finishActivity();
    }

    private void initCabinet() {
        baseShow();
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
                        baseHide();
                    }

                    @Override
                    public void onOverError(int code, String msg) {
                        baseHide();
                        CustomToast.show(msg);
                    }
                });
    }

    private void takeGoods(final GoodsBean goods, final int position, final int index) {
        baseShow();
        Observable.just("")
                .observeOn(Schedulers.io())
                .flatMap(new Function<String, ObservableSource<Integer>>() {
                    @Override
                    public ObservableSource<Integer> apply(String s) throws Exception {
                        String param = BVMManager.takeGoodsParam(position, index, goods.getPrice(), null);
                        String response = BVMManager.takeGoods(param);
                        Map<String, Object> responseMap = JsonUtils.convertJsonToObject(response);
                        Log.e("responseMap", responseMap.toString());
                        int code = Integer.parseInt(String.valueOf(responseMap.get("shipresult")));
                        if (code == 0) {
                            goods.setStock(goods.getStock() - 1);
                            DBManager.updateById(goods);
                        }
                        return Observable.just(code);
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new RxProgress<Integer>() {
                    @Override
                    protected void onOverSubscribe(Disposable d) {

                    }

                    @Override
                    protected void onOverNext(Integer code) {
                        if (code != 0) {
                            CustomToast.show(BVMManager.errorMsg(code));
                        }
                        baseHide();
                    }

                    @Override
                    protected void onOverError(int code, String msg) {
                        baseHide();
                        CustomToast.show(msg);
                    }
                });
    }

    @Override
    protected void onDestroy() {
        if (mDefaultTitle != null) {
            mDefaultTitle.destroy();
        }
        super.onDestroy();
    }
}
