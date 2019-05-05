package com.sovell.retail_cabinet.bean;

/**
 * 作者: 胡龙 on 2019/2/28.
 */

public class PayResultBean {

    /**
     * code : 1
     * sub_code : 0
     * msg : success
     * process_time : 0.015
     * seq : 636815182712202900
     * amt : 100.00
     * amt_dues : 100.00
     * code_all : 1000
     */

    private int code;
    private int sub_code;
    private String msg;
    private double process_time;
    private String seq;
    private String amt;
    private String amt_dues;
    private int code_all;

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public int getSub_code() {
        return sub_code;
    }

    public void setSub_code(int sub_code) {
        this.sub_code = sub_code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public double getProcess_time() {
        return process_time;
    }

    public void setProcess_time(double process_time) {
        this.process_time = process_time;
    }

    public String getSeq() {
        return seq;
    }

    public void setSeq(String seq) {
        this.seq = seq;
    }

    public String getAmt() {
        return amt;
    }

    public void setAmt(String amt) {
        this.amt = amt;
    }

    public String getAmt_dues() {
        return amt_dues;
    }

    public void setAmt_dues(String amt_dues) {
        this.amt_dues = amt_dues;
    }

    public int getCode_all() {
        return code_all;
    }

    public void setCode_all(int code_all) {
        this.code_all = code_all;
    }
}
