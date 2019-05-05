package com.sovell.retail_cabinet.https;

import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Function;

public class RetryWhenDelay implements Function<Observable<Throwable>, ObservableSource<?>> {

    private final int mMaxRetries;
    private final int mRetryDelayMillis;
    private int mRetryCount;

    public RetryWhenDelay() {
        this.mMaxRetries = 2;
        this.mRetryDelayMillis = 3000;
    }

    public RetryWhenDelay(int maxRetries, int retryDelayMillis) {
        this.mMaxRetries = maxRetries;
        this.mRetryDelayMillis = retryDelayMillis;
    }

    @Override
    public ObservableSource<?> apply(@NonNull Observable<Throwable> throwableObservable) throws Exception {
        return throwableObservable
                .flatMap(new Function<Throwable, ObservableSource<?>>() {
                    @Override
                    public ObservableSource<?> apply(@NonNull Throwable throwable) throws Exception {
                        if (++mRetryCount <= mMaxRetries) {
                            return Observable.timer(mRetryDelayMillis, TimeUnit.MILLISECONDS);
                        }
                        return Observable.error(throwable);
                    }
                });
    }
}
