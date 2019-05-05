package com.sovell.retail_cabinet.bean;

import java.util.ArrayList;
import java.util.List;

public class ProdBean {


    /**
     * code : 1
     * sub_code : 0
     * msg : success
     * process_time : 0.28
     * list : [{"cateid":"1","cateno":"101","catename":"饮料类","prods":[{"prodid":"2","prodno":"1001","prodname":"可口可乐","price":"2.5","unit":"份","pinyin":"kkkl","stock":"0","stock_threshold":"2"},{"prodid":"3","prodno":"1002","prodname":"阿萨姆奶茶","price":"2.5","unit":"份","pinyin":"asmnc","stock":"0","stock_threshold":"2"}]},{"cateid":"2","cateno":"102","catename":"零食","prods":[{"prodid":"3","prodno":"1002","prodname":"薯片","price":"2.5","unit":"份","pinyin":"sp","stock":"0","stock_threshold":"2"},{"prodid":"4","prodno":"1003","prodname":"饼干","price":"2.5","unit":"份","pinyin":"bg","stock":"0","stock_threshold":"2"}]}]
     * code_all : 1000
     */

    private int code;
    private int sub_code;
    private String msg;
    private double process_time;
    private int code_all;
    private List<TypeBean> list;

    public ProdBean() {
        this.list = new ArrayList<>();
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

    public int getCode_all() {
        return code_all;
    }

    public void setCode_all(int code_all) {
        this.code_all = code_all;
    }

    public List<TypeBean> getList() {
        if (list == null) {
            return new ArrayList<>();
        }
        return list;
    }

    public void setList(List<TypeBean> list) {
        this.list = list;
    }
}
