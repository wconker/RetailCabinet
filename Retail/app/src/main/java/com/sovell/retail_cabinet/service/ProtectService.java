package com.sovell.retail_cabinet.service;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;

import com.snbc.bvm.IProtectInterface;

public class ProtectService extends Service {

    private MyBinder mMyBinder;
    private MyConn mMyConn;

    @Override
    public IBinder onBind(Intent intent) {
        return mMyBinder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mMyBinder = new MyBinder();
        if (mMyConn == null) {
            mMyConn = new MyConn();
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        bindService(new Intent(ProtectService.this, MainService.class), mMyConn, Context.BIND_IMPORTANT);
        return START_STICKY;
    }

    private class MyBinder extends IProtectInterface.Stub {


        @Override
        public void getServiceName() throws RemoteException {

        }
    }

    private class MyConn implements ServiceConnection {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {

        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            startService(new Intent(ProtectService.this, MainService.class));
            bindService(new Intent(ProtectService.this, MainService.class), mMyConn, Context.BIND_IMPORTANT);
        }
    }
}
