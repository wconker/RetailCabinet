package com.sovell.retail_cabinet.bean;

import org.litepal.annotation.Column;
import org.litepal.crud.LitePalSupport;

import java.util.Locale;

/**
 * 作者: 胡龙 on 2019/2/26.
 * 商品类
 */
public class GoodsBean extends LitePalSupport implements Cloneable {
    /**
     * prodid : 2
     * prodno : 1001
     * prodname : 可口可乐
     * price : 2.5
     * unit : 份
     * pinyin : kkkl
     * stock : 0
     * stock_threshold : 2
     */

    //商品id
    private String prodid;
    //商品编码
    private String prodno;
    //商品名称
    private String prodname;
    //零售价 – 分为单位
    private int price;
    //单位
    private String unit;
    //拼音码
    private String pinyin;
    //商品描述
    private String desc;
    //当前库存
    private int stock;
    //最小库存阀值
    private int stock_threshold;
    //行
    private int row;
    //列
    private int column;
    //箱格ID（例如：1-2（第1行第2列））
    @Column(unique = true)
    private String boxid;
    //直属类别id
    private String cateid;
    //直属类别编码
    private String cateno;
    //直属类别名称
    private String catename;

    //2019.3.9 购物车中购买数量
    private int buycount = 1;

    //2019.3.11 当前的取货的状态
    //1 待取 2 已取 3 错误
    private int state = 1;


    //2019.3.11 服务器流水号
    private String seq;


    //2019.3.11 每个商品项id
    private int detailid;


    //2019.3.11 净菜柜最大可预订数量
    private int stock_max;

    public GoodsBean() {

    }

    public GoodsBean(int row, int column) {
        this.row = row;
        this.column = column;
        this.boxid = String.format(Locale.CHINA, "%d-%d", row, column);
        this.prodid = "";
        this.prodno = "";
        this.prodname = "";
        this.price = 0;
        this.unit = "";
        this.pinyin = "";
        this.stock = 0;
        this.stock_threshold = 0;
        this.cateid = "";
        this.cateno = "";
        this.catename = "";
        this.desc = "";
    }

    public int getStock_max() {
        return stock_max;
    }

    public void setStock_max(int stock_max) {
        this.stock_max = stock_max;
    }
    public int getDetailid() {
        return detailid;
    }

    public void setDetailid(int detailid) {
        this.detailid = detailid;
    }

    public String getSeq() {
        return seq;
    }

    public void setSeq(String seq) {
        this.seq = seq;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public String getProdid() {
        return prodid;
    }

    public void setProdid(String prodid) {
        this.prodid = prodid;
    }

    public String getProdno() {
        return prodno;
    }

    public void setProdno(String prodno) {
        this.prodno = prodno;
    }

    public String getProdname() {
        return prodname;
    }

    public void setProdname(String prodname) {
        this.prodname = prodname;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public String getPinyin() {
        return pinyin;
    }

    public void setPinyin(String pinyin) {
        this.pinyin = pinyin;
    }

    public int getStock() {
        return stock;
    }

    public void setStock(int stock) {
        this.stock = stock;
    }

    public int getStock_threshold() {
        return stock_threshold;
    }

    public void setStock_threshold(int stock_threshold) {
        this.stock_threshold = stock_threshold;
    }

    public int getRow() {
        return row;
    }

    public void setRow(int row) {
        this.row = row;
    }

    public int getColumn() {
        return column;
    }

    public void setColumn(int column) {
        this.column = column;
    }

    public String getBoxid() {
        return boxid;
    }

    public void setBoxid(String boxid) {
        this.boxid = boxid;
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

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public int getBuycount() {
        return buycount;
    }

    public void setBuycount(int buycount) {
        this.buycount = buycount;
    }

    @Override
    public boolean equals(Object arg0) {
        GoodsBean bean = (GoodsBean) arg0;
        return boxid.equals(bean.boxid);
    }

    @Override
    public int hashCode() {
        return boxid.hashCode();
    }

    @Override
    public Object clone() {
        GoodsBean bean = null;
        try {
            bean = (GoodsBean) super.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        return bean;
    }
}
