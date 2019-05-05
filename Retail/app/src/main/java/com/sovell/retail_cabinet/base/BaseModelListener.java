package com.sovell.retail_cabinet.base;

import io.reactivex.disposables.Disposable;

public interface BaseModelListener<T> {

    void onSuccess(T t);

    void onFailed(int code, String msg);

    void onDisposable(Disposable disposable);

}
