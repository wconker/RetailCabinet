package com.sovell.retail_cabinet.event;

public interface OnShopCountClickListener {
    void AddClick(int count, int price);

    void SubClick(int count, int price);
}
