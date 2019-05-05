package com.sovell.retail_cabinet.adapter;

import android.content.Context;
import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.sovell.retail_cabinet.utils.DataUtils;


/**
 * 列表分割距离
 */
public class DishItemDecoration extends RecyclerView.ItemDecoration {

    private int space;
    private int count;

    public DishItemDecoration(Context context, int count) {
        this.space = DataUtils.px2dip(context, 24);
        this.count = count;
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        int position = parent.getChildLayoutPosition(view) + 1;
        outRect.bottom = space;

        if (position == 1 || position % 3 == 1) {
            outRect.left = space;
            outRect.right = space / count;
        } else if (position == 2 || position % 3 == 2) {
            outRect.left = space - (space / count);
            outRect.right = 2 * (space / count);
        } else if (position == 3 || position % 3 == 0) {
            outRect.left = space - 2*(space / count);
            outRect.right = 3 * (space / count);
        }
    }
}
