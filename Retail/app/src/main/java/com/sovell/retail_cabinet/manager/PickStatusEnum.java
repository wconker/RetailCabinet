package com.sovell.retail_cabinet.manager;

public enum PickStatusEnum {

    PICK_SUCCESS(1, "出货成功"),

    PICK_FAIL(2, "出货失败"),

    PICK_WAIT(3, "等待取货"),

    PICK_SHIPMENT(4, "等待出货");

    private int id;

    private String name;

    private PickStatusEnum(int id, String name) {
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
