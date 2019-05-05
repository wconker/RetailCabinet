package com.sovell.retail_cabinet.manager;

public enum PayStatusEnum {

    BY_CARD(1,"请刷卡"),

    IN_PAYMENT(2,"支付中"),

    SHIPMENT(3,"出货中"),

    PICK_GOODS(4,"请取货"),

    SUCCESS(5,"支付成功"),

    FAIL(6,"支付失败");

    private int id;

    private String name;

    private PayStatusEnum(int id, String name) {
        this.setName(name);
        this.setId(id);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
