package com.sovell.retail_cabinet.base;

public class PassAccountBean {

    private String name;

    private String group_id;

    private String id;

    private String account_id;

    private String status;

    public void setName(String name){
        this.name = name;
    }
    public String getName(){
        return this.name;
    }
    public void setGroup_id(String group_id){
        this.group_id = group_id;
    }
    public String getGroup_id(){
        return this.group_id;
    }
    public void setId(String id){
        this.id = id;
    }
    public String getId(){
        return this.id;
    }
    public void setAccount_id(String account_id){
        this.account_id = account_id;
    }
    public String getAccount_id(){
        return this.account_id;
    }
    public String getStatus() {
        return status;
    }
    public void setStatus(String status) {
        this.status = status;
    }
}
