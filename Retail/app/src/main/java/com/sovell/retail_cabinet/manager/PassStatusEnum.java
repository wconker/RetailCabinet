package com.sovell.retail_cabinet.manager;

public enum PassStatusEnum {

    ACCEPTED(1, "处理中", "accepted"),

    NORMAL(2, "支付成功", "normal"),

    FAIL(3, "支付失败", "fail"),

    REFUNDING(4, "退款中", "refunding"),

    REFUND(5, "部分退款", "refund"),

    CLOSE(6, "关闭", "closed");

    private int id;

    private String chName;

    private String enName;

    PassStatusEnum(int id, String chName, String enName) {
        this.id = id;
        this.chName = chName;
        this.enName = enName;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getChName() {
        return chName;
    }

    public void setChName(String chName) {
        this.chName = chName;
    }

    public String getEnName() {
        return enName;
    }

    public void setEnName(String enName) {
        this.enName = enName;
    }
}
