package com.sovell.retail_cabinet.bean;

public class OrderTakeBean {
    /**
     * code : 1
     * sub_code : 0
     * msg : success
     * process_time : 0.078
     * seq : 636816950268627409
     * create_date : 2018-12-29 15:43:46
     * code_all : 1000
     */

    private int code;
    private int sub_code;



    private  int type;
    private String msg;
    private double process_time;
    private long seq;
    private String create_date;
    private int code_all;

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

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

    public long getSeq() {
        return seq;
    }

    public void setSeq(long seq) {
        this.seq = seq;
    }

    public String getCreate_date() {
        return create_date;
    }

    public void setCreate_date(String create_date) {
        this.create_date = create_date;
    }

    public int getCode_all() {
        return code_all;
    }

    public void setCode_all(int code_all) {
        this.code_all = code_all;
    }
}
