package com.sovell.retail_cabinet.bean;

public class BannerAD {

    private int code;

    private int sub_code;

    private String msg;

    private double process_time;

    private String path;

    private String path_absolute;

    private int policy;

    private int interval;

    private int wait;

    private String showcase;

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
    public void setPath(String path){
        this.path = path;
    }
    public String getPath(){
        return this.path;
    }
    public void setPath_absolute(String path_absolute){
        this.path_absolute = path_absolute;
    }
    public String getPath_absolute(){
        return this.path_absolute;
    }
    public void setPolicy(int policy){
        this.policy = policy;
    }
    public int getPolicy(){
        return this.policy;
    }
    public void setInterval(int interval){
        this.interval = interval;
    }
    public int getInterval(){
        return this.interval;
    }
    public void setWait(int wait){
        this.wait = wait;
    }
    public int getWait(){
        return this.wait;
    }
    public void setShowcase(String showcase){
        this.showcase = showcase;
    }
    public String getShowcase(){
        return this.showcase;
    }
    public void setCode_all(int code_all){
        this.code_all = code_all;
    }
    public int getCode_all(){
        return this.code_all;
    }

}
