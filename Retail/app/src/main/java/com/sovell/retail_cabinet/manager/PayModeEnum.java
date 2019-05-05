package com.sovell.retail_cabinet.manager;

/**
 * 支付方式的枚举类
 */
public enum PayModeEnum {
    /*现金支付*/
    MONEY("money", "卡", 1),

    /*微信支付*/
    WEI_CHAT("wechat", "微信", 31),

    /*支付宝支付*/
    ALI_PAY("alipay", "支付宝", 32);


    PayModeEnum(String value, String name, int code) {
        this.name = name;
        this.value = value;
        this.code = code;
    }

    private String value;

    private String name;

    private int code;

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }
}
