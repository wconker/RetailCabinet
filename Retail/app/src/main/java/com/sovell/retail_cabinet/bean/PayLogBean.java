package com.sovell.retail_cabinet.bean;

public class PayLogBean {

    private String invoice;//订单号
    private String msg;//撤单原因-可选参数
    private String seq;//流水号
    private String date;//时间

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getInvoice() {
        return invoice;
    }

    public void setInvoice(String invoice) {
        this.invoice = invoice;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public String getSeq() {
        return seq;
    }

    public void setSeq(String seq) {
        this.seq = seq;
    }

    @Override
    public boolean equals(Object arg0) {
        PayLogBean bean = (PayLogBean) arg0;
        return invoice.equals(bean.invoice);
    }

    @Override
    public int hashCode() {
        return invoice.hashCode();
    }
}
