package com.sovell.retail_cabinet.https;

import com.google.gson.reflect.TypeToken;
import com.sovell.retail_cabinet.base.BaseBean;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.ObservableSource;
import io.reactivex.ObservableTransformer;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Function;

/**
 * RxJava 处理http响应的数据
 */
public class RxThread {

    public static <T> ObservableTransformer<BaseBean<T>, T> ioAndMain(final Class<T> clazz) {
        return new ObservableTransformer<BaseBean<T>, T>() {
            @Override
            public ObservableSource<T> apply(Observable<BaseBean<T>> upstream) {
                return upstream.flatMap(new Function<BaseBean<T>, ObservableSource<T>>() {
                    @Override
                    public ObservableSource<T> apply(BaseBean<T> baseResult) throws Exception {
                        if (baseResult.success()) {
                            if (baseResult.getResult() == null) {
                                baseResult.setResult(clazz.newInstance());
                            }
                            return createData(baseResult.getResult());
                        } else {
                            return Observable.error(new RxException(baseResult.getCode(), baseResult.getMsg()));
                        }
                    }
                });
            }
        };
    }

    public static <T> ObservableTransformer<BaseBean<T>, T> ioAndMain(final TypeToken<T> clazz) {
        return new ObservableTransformer<BaseBean<T>, T>() {
            @Override
            public ObservableSource<T> apply(Observable<BaseBean<T>> upstream) {
                return upstream.flatMap(new Function<BaseBean<T>, ObservableSource<T>>() {
                    @Override
                    public ObservableSource<T> apply(BaseBean<T> baseResult) throws Exception {
                        if (baseResult.success()) {
                            if (baseResult.getResult() == null) {
                                if (clazz.getRawType().equals(List.class)) {
                                    baseResult.setResult((T) new ArrayList());
                                } else {
                                    baseResult.setResult((T) clazz.getRawType().newInstance());
                                }
                            }
                            return createData(baseResult.getResult());
                        } else {
                            return Observable.error(new RxException(baseResult.getCode(), baseResult.getMsg()));
                        }
                    }
                });
            }
        };
    }

    /**
     * 创建成功的数据
     */
    private static <T> Observable<T> createData(final T data) {
        return Observable.create(new ObservableOnSubscribe<T>() {
            @Override
            public void subscribe(@NonNull ObservableEmitter<T> o) {
                try {
                    o.onNext(data);
                    o.onComplete();
                } catch (Exception e) {
                    o.onError(e);
                }
            }
        });
    }
}
