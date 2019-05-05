package com.sovell.retail_cabinet.adapter;

import android.content.Context;
import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.view.View;

public class ItemDecoration extends RecyclerView.ItemDecoration {

    private Context context;

    public ItemDecoration(Context context) {
        this.context = context;
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        int position = parent.getChildLayoutPosition(view) + 1;

        outRect.bottom = px2dip(context, 24);
        if (position == 1 || position == 2 || position == 3) {
            outRect.top = px2dip(context, 24);
        }
        if (position % 3 == 1) {
            outRect.left = px2dip(context, 24);
            outRect.right = px2dip(context, 8);
        } else if (position % 3 == 2) {
            outRect.left = px2dip(context, 16);
            outRect.right = px2dip(context, 16);
        } else {
            outRect.left = px2dip(context, 8);
            outRect.right = px2dip(context, 24);
        }
    }

    private static int px2dip(Context context, int px) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (px / scale + 0.5f);
    }
}
