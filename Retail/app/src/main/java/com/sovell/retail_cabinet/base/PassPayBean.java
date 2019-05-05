package com.sovell.retail_cabinet.base;

public class PassPayBean {

    private String seq;

    private String account_id;

    private String trade_id;

    private String revision;

    private String title;

    private String notify_uri;

    private long create_time;

    private String status;

    private String id;

    public void setSeq(String seq) {
        this.seq = seq;
    }

    public String getSeq() {
        return this.seq;
    }

    public void setAccount_id(String account_id) {
        this.account_id = account_id;
    }

    public String getAccount_id() {
        return this.account_id;
    }

    public void setTrade_id(String trade_id) {
        this.trade_id = trade_id;
    }

    public String getTrade_id() {
        return this.trade_id;
    }

    public void setRevision(String revision) {
        this.revision = revision;
    }

    public String getRevision() {
        return this.revision;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTitle() {
        return this.title;
    }

    public void setNotify_uri(String notify_uri) {
        this.notify_uri = notify_uri;
    }

    public String getNotify_uri() {
        return this.notify_uri;
    }

    public void setCreate_time(long create_time) {
        this.create_time = create_time;
    }

    public long getCreate_time() {
        return this.create_time;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getStatus() {
        return this.status;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return this.id;
    }
}
