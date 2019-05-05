package com.sovell.retail_cabinet.https;

import io.reactivex.Flowable;
import io.reactivex.processors.FlowableProcessor;
import io.reactivex.processors.PublishProcessor;

/**
 * 有背压
 */

public class RxBusBack {

    private final FlowableProcessor<Object> mBus;

    private RxBusBack() {
        mBus = PublishProcessor.create().toSerialized();
    }

    public static RxBusBack get() {
        return Holder.BUS;
    }

    public void post(Object obj) {
        mBus.onNext(obj);
    }

    public <T> Flowable<T> toFlowable(Class<T> tClass) {
        return mBus.ofType(tClass);
    }

    public Flowable<Object> toFlowable() {
        return mBus;
    }

    public boolean hasSubscribers() {
        return mBus.hasSubscribers();
    }

    private static class Holder {
        private static final RxBusBack BUS = new RxBusBack();
    }
}
