package com.sovell.retail_cabinet.bean;

public class TermPairing {

    private int code;

    private int sub_code;

    private String msg;

    private double process_time;

    private String shop;

    private String term;

    private String appkey;

    private int code_all;

    public void setCode(int code){
        this.code = code;
    }
    public int getCode(){
        return this.code;
    }
    public void setSub_code(int sub_code){
        this.sub_code = sub_code;
    }
    public int getSub_code(){
        return this.sub_code;
    }
    public void setMsg(String msg){
        this.msg = msg;
    }
    public String getMsg(){
        return this.msg;
    }
    public void setProcess_time(double process_time){
        this.process_time = process_time;
    }
    public double getProcess_time(){
        return this.process_time;
    }
    public void setShop(String shop){
        this.shop = shop;
    }
    public String getShop(){
        return this.shop;
    }
    public void setTerm(String term){
        this.term = term;
    }
    public String getTerm(){
        return this.term;
    }
    public void setAppkey(String appkey){
        this.appkey = appkey;
    }
    public String getAppkey(){
        return this.appkey;
    }
    public void setCode_all(int code_all){
        this.code_all = code_all;
    }
    public int getCode_all(){
        return this.code_all;
    }
}
