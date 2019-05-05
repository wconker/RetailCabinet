package com.sovell.retail_cabinet.bean;

public class StockBean {

    private String prodid;

    private int stock;

    public String getProdid() {
        return prodid;
    }

    public void setProdid(String prodid) {
        this.prodid = prodid;
    }

    public int getStock() {
        return stock;
    }

    public void setStock(int stock) {
        this.stock = stock;
    }

    @Override
    public boolean equals(Object arg0) {
        StockBean bean = (StockBean) arg0;
        return prodid.equals(bean.prodid);
    }

    @Override
    public int hashCode() {
        return prodid.hashCode();
    }
}
