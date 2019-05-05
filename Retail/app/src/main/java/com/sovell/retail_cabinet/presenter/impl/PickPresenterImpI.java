package com.sovell.retail_cabinet.presenter.impl;

import android.annotation.SuppressLint;
import android.os.RemoteException;
import android.text.TextUtils;
import android.util.Log;

import com.sovell.retail_cabinet.bean.GoodsBean;
import com.sovell.retail_cabinet.bean.OrderBean;
import com.sovell.retail_cabinet.bean.OrderTakeBean;
import com.sovell.retail_cabinet.bean.ShipmentBean;
import com.sovell.retail_cabinet.https.RxException;
import com.sovell.retail_cabinet.https.RxProgress;
import com.sovell.retail_cabinet.manager.ApiManager;
import com.sovell.retail_cabinet.manager.BVMManager;
import com.sovell.retail_cabinet.manager.DBManager;
import com.sovell.retail_cabinet.presenter.contract.PickUpContract;
import com.sovell.retail_cabinet.utils.JsonUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

import static com.sovell.retail_cabinet.presenter.contract.TakeCode.OPERATION_IS_SUCCESSFUL;
import static com.sovell.retail_cabinet.presenter.contract.TakeCode.ORDER_DOES_NOT_EXIST;
import static com.sovell.retail_cabinet.presenter.contract.TakeCode.REPEAT_THE_PICKUP;
import static com.sovell.retail_cabinet.presenter.contract.TakeCode.TIME_FOR_PICKUP_HAS_EXPIRED;
import static com.sovell.retail_cabinet.presenter.impl.PayPresenterImpl.SHIPMENT_ERROR;
import static com.sovell.retail_cabinet.presenter.impl.PayPresenterImpl.SHIPMENT_SUCCESS;
import static com.sovell.retail_cabinet.presenter.impl.PayPresenterImpl.SHIPMENT_UNUSUAL;
import static com.sovell.retail_cabinet.presenter.impl.PayPresenterImpl.STOCK_ERROR;

public class PickPresenterImpI {
    private CompositeDisposable disposable;
    private CompositeDisposable mCompositeDisposable;//出货结果轮询器
    private PickUpContract pickUpContract;
    private final int PICKUP = 0;//已取货
    private final int NO_PICKUP = 1;//未取货
    private final int MACHINE_ERROR = 2;//机器错误
    private final int MYORDER = 10;//查询当日未取菜品

    public PickPresenterImpI(PickUpContract mPickUpContract) {
        pickUpContract = mPickUpContract;
        this.disposable = new CompositeDisposable();
        this.mCompositeDisposable = new CompositeDisposable();
    }

    //取货之前的调用，服务器返回是否可以取货
    public void VerifyBeforeTakingDelivery(final GoodsBean goodsBean, int type, String errorMsg) {
        if (goodsBean.getProdid().isEmpty() || goodsBean.getSeq() == null) {
            return;
        }
        ApiManager.orderTake(bean2json(goodsBean), type, errorMsg)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new RxProgress<OrderTakeBean>() {
                    @Override
                    protected void onOverSubscribe(Disposable d) {
                        disposable.add(d);
                    }

                    @Override
                    protected void onOverNext(OrderTakeBean orderTakeBean) {
                        if (orderTakeBean.getCode() == OPERATION_IS_SUCCESSFUL ||
                                orderTakeBean.getCode() == REPEAT_THE_PICKUP) {
                            pickUpContract.orderTakeSuccess(orderTakeBean, goodsBean);
                        } else {
                            String msg = "";
                            if (orderTakeBean.getCode() == TIME_FOR_PICKUP_HAS_EXPIRED) {
                                if (orderTakeBean.getSub_code() == 5) {
                                    msg = "未到取货开始时间";
                                } else if (orderTakeBean.getSub_code() == 6) {
                                    msg = "已过取货截止时间";
                                }
                            }
                            if (orderTakeBean.getCode() == ORDER_DOES_NOT_EXIST) {
                                if (orderTakeBean.getSub_code() == 1) {
                                    msg = "订单不存在";
                                } else if (orderTakeBean.getSub_code() == 2) {
                                    msg = "订单状态异常";
                                }
                            }
                            pickUpContract.orderTakeFail(orderTakeBean.getCode(), msg);
                        }
                    }

                    @Override
                    protected void onOverError(int code, String msg) {
                        pickUpContract.orderTakeFail(code, msg);
                    }
                });
    }

    public void VerificationCardInformation(String cid, int pageNum) {
        ApiManager.orderList(MYORDER, cid, pageNum)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new RxProgress<OrderBean>() {
                    @Override
                    protected void onOverSubscribe(Disposable d) {
                        disposable.add(d);
                    }

                    @Override
                    protected void onOverNext(OrderBean orderBean) {
                        pickUpContract.getOrderSuccess(orderBean);
                    }

                    @Override
                    protected void onOverError(int code, String msg) {
                        pickUpContract.getOrderFail(code, msg);
                    }
                });
    }


    @SuppressLint("CheckResult")
    public void PickupMethod(final GoodsBean mGoodsBean) {
        Observable.just("")
                .observeOn(Schedulers.io())
                .flatMap(new Function<String, ObservableSource<ShipmentBean>>() {
                             @Override
                             public ObservableSource<ShipmentBean> apply(String seq) throws Exception {
                                 ShipmentBean shipmentBean = new ShipmentBean();
                                 shipmentBean.setSeq(seq);
                                 try {
                                     //机器内所有该id的商品
                                     List<GoodsBean> goodsBeanList = DBManager.findSameById(mGoodsBean.getProdid());
                                     if (goodsBeanList != null && goodsBeanList.size() != 0) {
                                         //从第一个开始拿
                                         GoodsBean goodsBean = goodsBeanList.get(0);
                                         //    取货指令
                                         String param = BVMManager.takeGoodsParam(goodsBean.getRow(), goodsBean.getColumn(), goodsBean.getPrice(), null);
                                         String response = BVMManager.takeGoods(param);
                                         Map<String, Object> responseMap = JsonUtils.convertJsonToObject(response);
                                         Log.e("responseMap", responseMap.toString());
                                         int machineCode = Integer.parseInt(String.valueOf(responseMap.get("shipresult")));
                                         if (machineCode == 0) {
                                             goodsBean.setStock(goodsBean.getStock() - 1);
                                             DBManager.updateById(goodsBean);
                                             mGoodsBean.setStock(mGoodsBean.getStock() - 1);
                                             shipmentBean.setCode(SHIPMENT_SUCCESS);
                                             shipmentBean.setMachineCode(machineCode);
                                             shipmentBean.setErrorMessage("出货成功");
                                             return Observable.just(shipmentBean);
                                         } else {
                                             int[] temp = BVMManager.currentTemp();
                                             String errorMsg = BVMManager.errorMsg(machineCode);
                                             ApiManager.termStatus(temp[0], String.valueOf(machineCode), errorMsg);
                                             if (machineCode == -1203) {
                                                 //空货道,需要清空库存并向服务器上报信息
                                                 Log.e("空货道", "空货道错误");
                                                 mGoodsBean.setStock(mGoodsBean.getStock() - goodsBean.getStock());
                                                 goodsBean.setStock(0);
                                                 DBManager.updateById(goodsBean);
                                             } else if (machineCode == -1202) {
                                                 //货斗有货
                                                 BVMManager.openDoorAgain();
                                             }
                                             shipmentBean.setCode(SHIPMENT_ERROR);
                                             shipmentBean.setMachineCode(machineCode);
                                             shipmentBean.setErrorMessage(errorMsg);//"出货失败"
                                             return Observable.just(shipmentBean);
                                         }
                                     } else {
                                         //进行退款
                                         shipmentBean.setCode(STOCK_ERROR);
                                         shipmentBean.setErrorMessage("本地商品库存不足");
                                         return Observable.just(shipmentBean);
                                     }
                                 } catch (RemoteException e) {
                                     shipmentBean.setCode(SHIPMENT_UNUSUAL);
                                     shipmentBean.setErrorMessage("商品出货失败,机器异常");
                                     return Observable.just(shipmentBean);
                                 }
                             }
                         }
                ).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new RxProgress<ShipmentBean>() {
                    @Override
                    protected void onOverSubscribe(Disposable d) {
                        disposable.add(d);
                    }

                    @Override
                    protected void onOverNext(ShipmentBean shipmentBean) {
                        //出货成功后轮训取货
                        if (shipmentBean.getCode() == SHIPMENT_SUCCESS) {
                            pickGoodsInterval();
                        } else {
                            pickUpContract.pickGoodFail(shipmentBean.getCode(), shipmentBean.getErrorMessage());
                        }
                    }

                    @Override
                    protected void onOverError(int code, String msg) {
                        pickUpContract.pickGoodFail(code, msg);
                    }
                });

    }

    /**
     * 取货轮询
     */
    private void pickGoodsInterval() {
        pickUpContract.pickGoodWait();
        mCompositeDisposable.clear();
        Observable.interval(500, TimeUnit.MILLISECONDS)
                .observeOn(Schedulers.io())
                .flatMap(new Function<Long, ObservableSource<Integer>>() {
                    @Override
                    public ObservableSource<Integer> apply(@NonNull Long aLong) throws Exception {
                        int[] doorState = BVMManager.doorState();
                        //doorState[0] < 0:异常；doorState[1] == 2:开门
                        //BVMManager.errorMsg(doorState[0]);
                        //todo 这里如果抛异常该如何处理
                        if (doorState[0] < 0) {//异常
                            //清除错误
                            BVMManager.faultClean();
                            return Observable.error(new RxException(MACHINE_ERROR, BVMManager.errorMsg(doorState[0])));
                        } else {
                            if (doorState[1] == 1) {
                                BVMManager.faultClean();
                                String[] faultQuery = BVMManager.faultQuery();
                                if (TextUtils.isEmpty(faultQuery[0])) {
                                    //无错误信息(已取货)
                                    return Observable.just(PICKUP);
                                } else if (faultQuery[0].contains("40FA")) {
                                    //货斗有货
                                    BVMManager.openDoorAgain();
                                    return Observable.just(NO_PICKUP);
                                } else {
                                    //提交设备错误信息
                                    int[] temp = BVMManager.currentTemp();
                                    String[] error = faultQuery[0].split(":");
                                    if (error.length > 1) {
                                        ApiManager.termStatus(temp[0], error[0], error[1]);
                                    }
                                    return Observable.error(new RxException(MACHINE_ERROR, faultQuery[0]));
                                }
                            }
                            return Observable.just(NO_PICKUP);
                        }
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new RxProgress<Integer>() {
                    @Override
                    protected void onOverSubscribe(Disposable d) {
                        mCompositeDisposable.add(d);
                    }

                    @Override
                    protected void onOverNext(Integer source) {
                        if (source == PICKUP) {//已取货
                            mCompositeDisposable.clear();
                            pickUpContract.pickGoodSuccess("已取货");

                        } else if (source == MACHINE_ERROR) {//机器错误
                            mCompositeDisposable.clear();
                        }
                    }

                    @Override
                    protected void onOverError(int code, String msg) {
                        mCompositeDisposable.clear();
                        // setStatePayment(FAIL, true, msg);
                    }
                });
    }

    public void cancelRequest() {
        if (disposable != null) {
            disposable.clear();
        }
    }


    public String bean2json(GoodsBean bean) {
        JSONArray array = new JSONArray();
        JSONObject object = new JSONObject();
        try {
            object.put("seq", bean.getSeq());
            object.put("prodid", bean.getProdid() + "");
            object.put("detailid", bean.getDetailid() + "");
            array.put(object);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return array.toString();
    }

    //构造返回中没有但是需要用到的对象
    public List<GoodsBean> NetValue2GoodBean(OrderBean orderBean) {
        List<GoodsBean> goodsBeans = new ArrayList<>();
        for (int i = 0; i < orderBean.getList().size(); i++) {
            List<OrderBean.ListBean.ProdsBean> listBean = orderBean.getList().get(i).getProds();
            long seq = orderBean.getList().get(i).getSeq();
            //筛选出只有可以取货的状态的货
            if (orderBean.getList().get(i).getState() == 1 || orderBean.getList().get(i).getState() == 3) {
                for (int j = 0; j < listBean.size(); j++) {
                    GoodsBean good = new GoodsBean();
                    //测试 固定id=3
                    good.setProdid(listBean.get(j).getProdid());//
                    good.setProdname(listBean.get(j).getProdname());
                    good.setSeq(String.valueOf(seq));
                    good.setProdno(listBean.get(j).getProdno());
                    //测试 固定1
                    good.setState(listBean.get(j).getState());
                    good.setDetailid(listBean.get(j).getDetailid());
                    goodsBeans.add(good);
                }
            }
        }
        return goodsBeans;
    }
}
