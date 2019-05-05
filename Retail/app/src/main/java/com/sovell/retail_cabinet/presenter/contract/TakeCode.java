package com.sovell.retail_cabinet.presenter.contract;

public interface TakeCode {
    public final int OPERATION_IS_SUCCESSFUL = 1;
    public final int OTHER_FAILURE = 2;
    public final int THE_INCOMING_PARAMETER_IS_NOT_VALID = 3;
    public final int ORDER_DOES_NOT_EXIST = 4;
    public final int TIME_FOR_PICKUP_HAS_EXPIRED = 6;
    public final int REPEAT_THE_PICKUP = 42;

}
