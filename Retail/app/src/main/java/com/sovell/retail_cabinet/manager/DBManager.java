package com.sovell.retail_cabinet.manager;

import android.database.Cursor;
import android.text.TextUtils;

import com.sovell.retail_cabinet.bean.GoodsBean;

import org.litepal.LitePal;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class DBManager {

    /**
     * 柜子的行与列
     * 例如：6,6,6,6,6,6（6行6列）
     *
     * @param cabinet 柜子的行列数据
     */
    public static List<List<GoodsBean>> initCabinet(int[] cabinet) {
        synchronized (DBManager.class) {
            LitePal.deleteAll(GoodsBean.class);
            List<List<GoodsBean>> cabinetList = new ArrayList<>();
            for (int row = 1; row <= cabinet.length; row++) {
                List<GoodsBean> rowList = new ArrayList<>();
                for (int column = 1; column <= cabinet[row - 1]; column++) {
                    GoodsBean localGoods = new GoodsBean();
                    localGoods.setRow(row);
                    localGoods.setColumn(column);
                    localGoods.setBoxid(String.format(Locale.CHINA, "%d-%d", row, column));
                    localGoods.save();
                    rowList.add(localGoods);
                }
                cabinetList.add(rowList);
            }
            return cabinetList;
        }
    }

    /**
     * 删除全部
     */
    public static void deleteAll() {
        synchronized (DBManager.class) {
            LitePal.deleteAll(GoodsBean.class);
        }
    }

    /**
     * 查询全部箱格
     */
    public static List<GoodsBean> findAll() {
        synchronized (DBManager.class) {
            List<GoodsBean> goodsList = LitePal.findAll(GoodsBean.class);
            if (goodsList == null) {
                goodsList = new ArrayList<>();
            }
            return goodsList;
        }
    }

    /**
     * 查询全部商品按类别转json
     */
    public static List<GoodsBean> findAllById() {
        synchronized (DBManager.class) {
            List<GoodsBean> goodsList = new ArrayList<>();
            Cursor cursor = LitePal.findBySQL("SELECT *, SUM(stock) AS stock FROM GoodsBean GROUP BY prodid");

            while (cursor.moveToNext()) {
                String prodid = cursor.getString(cursor.getColumnIndex("prodid"));
                String prodno = cursor.getString(cursor.getColumnIndex("prodno"));
                String prodname = cursor.getString(cursor.getColumnIndex("prodname"));
                int price = cursor.getInt(cursor.getColumnIndex("price"));
                String unit = cursor.getString(cursor.getColumnIndex("unit"));
                String pinyin = cursor.getString(cursor.getColumnIndex("pinyin"));
                int stock = cursor.getInt(cursor.getColumnIndex("stock"));
                int stock_threshold = cursor.getInt(cursor.getColumnIndex("stock_threshold"));
                int row = cursor.getInt(cursor.getColumnIndex("row_lpcolumn"));
                int column = cursor.getInt(cursor.getColumnIndex("column_lpcolumn"));
                String boxid = cursor.getString(cursor.getColumnIndex("boxid"));
                String cateid = cursor.getString(cursor.getColumnIndex("cateid"));
                String cateno = cursor.getString(cursor.getColumnIndex("cateno"));
                String catename = cursor.getString(cursor.getColumnIndex("catename"));
                String desc = cursor.getString(cursor.getColumnIndex("desc_lpcolumn"));
                if (TextUtils.isEmpty(prodid)) {
                    continue;
                }
                GoodsBean localGoods = new GoodsBean();
                localGoods.setProdid(prodid);
                localGoods.setProdno(prodno);
                localGoods.setProdname(prodname);
                localGoods.setPrice(price);
                localGoods.setUnit(unit);
                localGoods.setPinyin(pinyin);
                localGoods.setStock(stock);
                localGoods.setStock_threshold(stock_threshold);
                localGoods.setRow(row);
                localGoods.setColumn(column);
                localGoods.setBoxid(boxid);
                localGoods.setCateid(cateid);
                localGoods.setCateno(cateno);
                localGoods.setCatename(catename);
                localGoods.setDesc(desc);
                goodsList.add(localGoods);
            }
            cursor.close();
            return goodsList;
        }
    }

    /**
     * 根据ID查询全部商品
     *
     * @param goodsBean 商品
     */
    public static GoodsBean findOtherById(GoodsBean goodsBean) {
        synchronized (DBManager.class) {
            GoodsBean resultGoods = new GoodsBean();
            List<GoodsBean> goodsList = LitePal.where("prodid = ? and boxId is not ?", goodsBean.getProdid(), goodsBean.getBoxid()).find(GoodsBean.class);

            if (goodsList == null || goodsList.size() <= 0) {
                return null;
            }

            for (GoodsBean goods : goodsList) {
                resultGoods.setProdid(goods.getProdid());
                resultGoods.setStock(resultGoods.getStock() + goods.getStock());
            }

            return resultGoods;
        }
    }

    /**
     * 查询相同商品id的列表,并且库存大于0
     *
     * @param prodid 商品id
     */
    public static List<GoodsBean> findSameById(String prodid) {
        synchronized (DBManager.class) {
            return LitePal.where("prodid = ? and stock > 0", prodid).find(GoodsBean.class);
        }
    }

    /**
     * 更新柜子对应格子的商品数据
     *
     * @param goods 商品明细
     */
    public static void updateById(GoodsBean goods) {
        synchronized (DBManager.class) {
            if (goods != null) {
                goods.saveOrUpdate("boxid = ?", goods.getBoxid());
            }
        }
    }

}
