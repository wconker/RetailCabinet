package com.sovell.retail_cabinet.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.text.TextUtils;

import com.sovell.retail_cabinet.app.RetailCabinetApp;
import com.sovell.retail_cabinet.bean.NetState;
import com.sovell.retail_cabinet.https.RxBus;

/**
 * 网络状态广播监听
 */

public class NetReceiver extends BroadcastReceiver {

    public static final String ACTION = "android.net.conn.CONNECTIVITY_CHANGE";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (TextUtils.equals(intent.getAction(), ConnectivityManager.CONNECTIVITY_ACTION)) {
            ConnectivityManager connect = (ConnectivityManager) RetailCabinetApp.Instance().getSystemService(Context.CONNECTIVITY_SERVICE);
            NetState netState = new NetState();
            //当拔出网线的时候getActiveNetworkInfo 为null
            if (connect != null && connect.getActiveNetworkInfo() != null) {
                netState.setConnect(connect.getActiveNetworkInfo().isAvailable());
                RxBus.get().post(netState);
            } else if (null == connect.getActiveNetworkInfo()){
                netState.setConnect(false);
                RxBus.get().post(netState);
            }
        }
    }
}
