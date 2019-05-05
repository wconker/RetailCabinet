package com.sovell.retail_cabinet.service;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;

import com.snbc.bvm.IProtectInterface;
import com.sovell.retail_cabinet.app.RetailCabinetApp;
import com.sovell.retail_cabinet.https.RxProgress;
import com.sovell.retail_cabinet.ui.SplashActivity;
import com.sovell.retail_cabinet.utils.ConfigUtil;

import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;
import io.reactivex.functions.Predicate;
import io.reactivex.schedulers.Schedulers;

public class MainService extends Service {

    private MyBinder mMyBinder;
    private MyConn mMyConn;
    private CompositeDisposable mComposite;

    public static IProtectInterface mICat;

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
        init();
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
            mICat = IProtectInterface.Stub.asInterface(iBinder);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            startService(new Intent(MainService.this, ProtectService.class));
            bindService(new Intent(MainService.this, ProtectService.class), mMyConn, Context.BIND_IMPORTANT);
        }
    }

    private void init() {
        this.bindService(new Intent(MainService.this, ProtectService.class), mMyConn, Context.BIND_IMPORTANT);
        if (mComposite == null) {
            mComposite = new CompositeDisposable();
        }
        mComposite.clear();
        Observable.interval(5, TimeUnit.SECONDS)
                .observeOn(Schedulers.io())
                .flatMap(new Function<Long, ObservableSource<Long>>() {
                    @Override
                    public ObservableSource<Long> apply(@NonNull Long aLong) throws Exception {
                        if (RetailCabinetApp.Instance().getActivityCount() <= 0 && ConfigUtil.Instance().getBoolean(ConfigUtil.PROTECT)) {
                            Intent intent = new Intent(getApplicationContext(), SplashActivity.class);
                            intent.addCategory(Intent.CATEGORY_LAUNCHER);
                            intent.setAction(Intent.ACTION_MAIN);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
                            startActivity(intent);
                        }
                        return Observable.just(aLong);
                    }
                })
                .retry(new Predicate<Throwable>() {
                    @Override
                    public boolean test(@NonNull Throwable throwable) throws Exception {
                        return true;
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new RxProgress<Long>() {
                    @Override
                    protected void onOverSubscribe(Disposable d) {
                        mComposite.add(d);
                    }

                    @Override
                    protected void onOverNext(Long aLong) {

                    }

                    @Override
                    protected void onOverError(int code, String msg) {

                    }
                });
    }

    @Override
    public void onDestroy() {
        if (mComposite != null) {
            mComposite.clear();
        }
        super.onDestroy();
    }
}
