package com.sovell.retail_cabinet.https;

import com.sovell.retail_cabinet.base.PassAccountBean;
import com.sovell.retail_cabinet.base.PassPayBean;
import com.sovell.retail_cabinet.base.PassTokenBean;
import com.sovell.retail_cabinet.bean.BannerAD;
import com.sovell.retail_cabinet.bean.CardInfoBean;
import com.sovell.retail_cabinet.bean.OrderBean;
import com.sovell.retail_cabinet.bean.OrderTakeBean;
import com.sovell.retail_cabinet.bean.PayResultBean;
import com.sovell.retail_cabinet.bean.ProdBean;
import com.sovell.retail_cabinet.bean.RefundBean;
import com.sovell.retail_cabinet.bean.TermPairing;
import com.sovell.retail_cabinet.bean.TermSignIn;
import com.sovell.retail_cabinet.bean.VersionInfo;

import java.util.List;
import java.util.Map;

import io.reactivex.Observable;
import okhttp3.ResponseBody;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.Field;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;
import retrofit2.http.Streaming;
import retrofit2.http.Url;

/**
 * retrofit2 Http接口定义的类
 */
public interface ApiService {

    @FormUrlEncoded
    @POST(HttpsAddress.TERM_PAIRING)
    Observable<TermPairing> termPairing(@FieldMap Map<String, Object> params);

    @FormUrlEncoded
    @POST(HttpsAddress.TERM_SIGN_IN)
    Observable<TermSignIn> termSignIn(@FieldMap Map<String, Object> params);

    @FormUrlEncoded
    @POST(HttpsAddress.TERM_SIGN_OUT)
    Observable<TermSignIn> termSignOut(@FieldMap Map<String, Object> params);

    @FormUrlEncoded
    @POST(HttpsAddress.TERM_KEEP)
    Observable<TermSignIn> termKeep(@FieldMap Map<String, Object> params);

    @FormUrlEncoded
    @POST(HttpsAddress.TERM_STATUS)
    Observable<TermSignIn> termStatus(@FieldMap Map<String, Object> params);

    @FormUrlEncoded
    @POST(HttpsAddress.STOCK_CHECK)
    Observable<TermSignIn> stockCheck(@FieldMap Map<String, Object> params);

    @FormUrlEncoded
    @POST(HttpsAddress.PROD_LIST)
    Observable<ProdBean> prodList(@FieldMap Map<String, Object> params);

    @FormUrlEncoded
    @POST(HttpsAddress.CARD_GET)
    Observable<CardInfoBean> cardGet(@FieldMap Map<String, Object> params);

    @FormUrlEncoded
    @POST(HttpsAddress.ORDER_TRADE)
    Observable<PayResultBean> orderTrade(@FieldMap Map<String, Object> params);

    @FormUrlEncoded
    @POST(HttpsAddress.ORDER_REFUND)
    Observable<RefundBean> orderRefund(@FieldMap Map<String, Object> params);

    @FormUrlEncoded
    @POST(HttpsAddress.SET_SHOWCASE)
    Observable<BannerAD> showcase(@FieldMap Map<String, Object> params);

    @FormUrlEncoded
    @POST(HttpsAddress.ORDER_LIST)
    Observable<OrderBean> orderList(@FieldMap Map<String, Object> params);

    @FormUrlEncoded
    @POST(HttpsAddress.ORDER_TAKE)
    Observable<OrderTakeBean> orderTake(@FieldMap Map<String, Object> params);

    @FormUrlEncoded
    @POST(HttpsAddress.DISH_CHECK)
    Observable<OrderBean> dishCheck(@FieldMap Map<String, Object> params);


    /*----------PASS接口----------*/
    @FormUrlEncoded
    @POST(HttpsAddress.PASS_TOKEN)
    Observable<PassTokenBean> passToken(@Header("Authorization") String authorization, @Field("grant_type") String grant);

    @GET(HttpsAddress.PASS_ACCOUNT)
    Observable<List<PassAccountBean>> passAccount(@Header("Authorization") String authorization);

    @GET(HttpsAddress.PASS_CHECK)
    Observable<PassPayBean> passCheck(@Header("Authorization") String authorization, @Path("seq") String seq, @Query("timeout") String timeout);

    @POST(HttpsAddress.PASS_PAY_SCAN)
    Observable<PassPayBean> passPayQRCode(@Header("Authorization") String authorization, @Body Map<String, Object> params);

    @DELETE(HttpsAddress.PASS_REFUND)
    Observable<PassPayBean> passRefund(@Header("Authorization") String authorization, @Path("seq") String seq, @Query("sign") String sign);


    @GET
    Observable<VersionInfo> checkVersion(@Url String url);

    @Streaming
    @GET
    Observable<ResponseBody> updateApp(@Url String url);
}
