package com.sovell.retail_cabinet.https;

import com.sovell.retail_cabinet.utils.DeviceUtil;

import java.net.SocketTimeoutException;
import java.util.concurrent.TimeoutException;

import io.reactivex.Observer;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import retrofit2.HttpException;

/**
 * Http 响应错误信息的处理
 */
public abstract class RxProgress<T> implements Observer<T> {

    protected abstract void onOverSubscribe(Disposable d);

    protected abstract void onOverNext(T t);

    protected abstract void onOverError(int code, String msg);

    @Override
    public void onNext(T t) {
        onOverNext(t);
    }

    @Override
    public void onError(Throwable e) {
        e.printStackTrace();
        if (e instanceof TimeoutException || e instanceof SocketTimeoutException) {
            onOverError(RxException.NET_TIMEOUT, "服务器异常");
        } else if (!DeviceUtil.isNetConnect()) {
            onOverError(RxException.NET_BREAK, "网络断开");
        } else if (e instanceof RxException) {
            RxException ex = (RxException) e;
            onOverError(ex.getErrorCode(), ex.getMessage());
        } else if (e instanceof HttpException) {
            HttpException ex = (HttpException) e;
            onOverError(ex.code(), "HTTP访问错误(" + ex.code() + ")");//0425 修改提示语
        } else {
            onOverError(RxException.NET_ERROR, e.getMessage());
        }
    }

    @Override
    public void onComplete() {

    }

    @Override
    public void onSubscribe(@NonNull Disposable d) {
        onOverSubscribe(d);
    }
}
