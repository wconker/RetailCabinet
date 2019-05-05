package com.sovell.retail_cabinet.bean;

/**
 * 作者: 胡龙 on 2019/3/14.
 */

public class ShipmentBean {
    private int code;//自定义code
    private int machineCode;//机器状态码
    private String errorMessage;//错误信息
    private String seq;//流水号

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public int getMachineCode() {
        return machineCode;
    }

    public void setMachineCode(int machineCode) {
        this.machineCode = machineCode;
    }

    public String getSeq() {
        return seq;
    }

    public void setSeq(String seq) {
        this.seq = seq;
    }
}
