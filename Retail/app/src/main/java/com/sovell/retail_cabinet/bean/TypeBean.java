package com.sovell.retail_cabinet.bean;

import java.util.ArrayList;
import java.util.List;

/**
 * 作者: 胡龙 on 2019/2/26.
 * 商品分类
 */

public class TypeBean {
    /**
     * cateid : 1
     * cateno : 101
     * catename : 饮料类
     * prods : [{"prodid":"2","prodno":"1001","prodname":"可口可乐","price":"2.5","unit":"份","pinyin":"kkkl","stock":"0","stock_threshold":"2"},{"prodid":"3","prodno":"1002","prodname":"阿萨姆奶茶","price":"2.5","unit":"份","pinyin":"asmnc","stock":"0","stock_threshold":"2"}]
     */

    private String cateid;
    private String cateno;
    private String catename;
    private List<GoodsBean> prods;

    public TypeBean() {
        this.prods = new ArrayList<>();
    }

    public String getCateid() {
        return cateid;
    }

    public void setCateid(String cateid) {
        this.cateid = cateid;
    }

    public String getCateno() {
        return cateno;
    }

    public void setCateno(String cateno) {
        this.cateno = cateno;
    }

    public String getCatename() {
        return catename;
    }

    public void setCatename(String catename) {
        this.catename = catename;
    }

    public List<GoodsBean> getProds() {
        if (prods == null) {
            return new ArrayList<>();
        }
        return prods;
    }

    public void setProds(List<GoodsBean> prods) {
        this.prods = prods;
    }

    @Override
    public boolean equals(Object arg0) {
        TypeBean bean = (TypeBean) arg0;
        return cateid.equals(bean.cateid);
    }

    @Override
    public int hashCode() {
        return cateid.hashCode();
    }
}
