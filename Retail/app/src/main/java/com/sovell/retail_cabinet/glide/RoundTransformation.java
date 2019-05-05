package com.sovell.retail_cabinet.glide;

import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;

import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.CenterInside;

import java.security.MessageDigest;

import static com.sovell.retail_cabinet.glide.RoundTransformation.CornerType.ALL;

/**
 * 倒角变换类型
 */
public class RoundTransformation extends CenterInside {

    private CornerType mCornerType = ALL;
    private int mRadius;

    public RoundTransformation(int radius) {
        mRadius = radius;
    }

    public RoundTransformation(int radius, CornerType type) {
        mCornerType = type;
        mRadius = radius;
    }

    @Override
    protected Bitmap transform(BitmapPool pool, Bitmap toTransform, int outWidth, int outHeight) {
        Bitmap transform = super.transform(pool, toTransform, outWidth, outHeight);
        return roundCrop(pool, transform);
    }

    private Bitmap roundCrop(BitmapPool pool, Bitmap source) {
        if (source == null) return null;

        Bitmap result = pool.get(source.getWidth(), source.getHeight(), Bitmap.Config.ARGB_8888);

        if (result == null) {
            result = Bitmap.createBitmap(source.getWidth(), source.getHeight(), Bitmap.Config.ARGB_8888);
        }

        Canvas canvas = new Canvas(result);
        Paint paint = new Paint();
        paint.setShader(new BitmapShader(source, BitmapShader.TileMode.CLAMP, BitmapShader.TileMode.CLAMP));
        paint.setAntiAlias(true);
        drawRoundRect(canvas, paint, source.getWidth(), source.getHeight());
        return result;
    }

    public enum CornerType {
        ALL, LEFT_TOP, LEFT_BOTTOM, RIGHT_TOP, RIGHT_BOTTOM, LEFT, RIGHT, BOTTOM, TOP
    }

    private void drawRoundRect(Canvas canvas, Paint paint, float width, float height) {
        switch (mCornerType) {
            case LEFT_TOP:
                drawLeftTopCorner(canvas, paint, width, height);
                break;
            case LEFT_BOTTOM:
                drawLeftBottomCorner(canvas, paint, width, height);
                break;
            case RIGHT_TOP:
                drawRightTopCorner(canvas, paint, width, height);
                break;
            case RIGHT_BOTTOM:
                drawRightBottomCorner(canvas, paint, width, height);
                break;
            case LEFT:
                drawLeftCorner(canvas, paint, width, height);
                break;
            case RIGHT:
                drawRightCorner(canvas, paint, width, height);
                break;
            case BOTTOM:
                drawBottomCorner(canvas, paint, width, height);
                break;
            case TOP:
                drawTopCorner(canvas, paint, width, height);
                break;
            case ALL:
            default:
                canvas.drawRoundRect(new RectF(0, 0, width, height), mRadius, mRadius, paint);
                break;
        }
    }

    /**
     * 画左上角
     */
    private void drawLeftTopCorner(Canvas canvas, Paint paint, float width, float height) {
        canvas.drawRect(new RectF(mRadius, 0, width, height), paint);
        canvas.drawRect(new RectF(0, mRadius, mRadius, height), paint);
        canvas.drawArc(new RectF(0, 0, mRadius * 2, mRadius * 2), 180, 90, true, paint);
    }

    /**
     * 画左下角
     */
    private void drawLeftBottomCorner(Canvas canvas, Paint paint, float width, float height) {
        canvas.drawRect(new RectF(0, 0, width, height - mRadius), paint);
        canvas.drawRect(new RectF(mRadius, height - mRadius, width, height), paint);
        canvas.drawArc(new RectF(0, height - mRadius * 2, mRadius * 2, height), 90, 90, true, paint);
    }

    /**
     * 画右上角
     */
    private void drawRightTopCorner(Canvas canvas, Paint paint, float width, float height) {
        canvas.drawRect(new RectF(0, 0, width - mRadius, height), paint);
        canvas.drawRect(new RectF(width - mRadius, mRadius, width, height), paint);
        canvas.drawArc(new RectF(width - mRadius * 2, 0, width, mRadius * 2), 270, 90, true, paint);
    }

    /**
     * 画右下角
     */
    private void drawRightBottomCorner(Canvas canvas, Paint paint, float width, float height) {
        canvas.drawRect(new RectF(0, 0, width, height - mRadius), paint);
        canvas.drawRect(new RectF(0, height - mRadius, width - mRadius, height), paint);
        canvas.drawArc(new RectF(width - mRadius * 2, height - mRadius * 2, width, height), 0, 90, true, paint);
    }

    /**
     * 画左角
     */
    private void drawLeftCorner(Canvas canvas, Paint paint, float width, float height) {
        canvas.drawRect(new RectF(mRadius, 0, width, height), paint);
        canvas.drawRect(new RectF(0, mRadius, mRadius, height - mRadius), paint);
        canvas.drawArc(new RectF(0, 0, mRadius * 2, mRadius * 2), 180, 90, true, paint);
        canvas.drawArc(new RectF(0, height - mRadius * 2, mRadius * 2, height), 90, 90, true, paint);
    }

    /**
     * 画右角
     */
    private void drawRightCorner(Canvas canvas, Paint paint, float width, float height) {
        canvas.drawRect(new RectF(0, 0, width - mRadius, height), paint);
        canvas.drawRect(new RectF(width - mRadius, mRadius, width, height - mRadius), paint);
        canvas.drawArc(new RectF(width - mRadius * 2, 0, width, mRadius * 2), 270, 90, true, paint);
        canvas.drawArc(new RectF(width - mRadius * 2, height - mRadius * 2, width, height), 0, 90, true, paint);
    }

    /**
     * 画上角
     */
    private void drawTopCorner(Canvas canvas, Paint paint, float width, float height) {
        canvas.drawRect(new RectF(0, mRadius, width, height), paint);
        canvas.drawRect(new RectF(mRadius, 0, width - mRadius, mRadius), paint);
        canvas.drawArc(new RectF(0, 0, mRadius * 2, mRadius * 2), 180, 90, true, paint);
        canvas.drawArc(new RectF(width - mRadius * 2, 0, width, mRadius * 2), 270, 90, true, paint);
    }

    /**
     * 画下角
     */
    private void drawBottomCorner(Canvas canvas, Paint paint, float width, float height) {
        canvas.drawRect(new RectF(0, 0, width, height - mRadius), paint);
        canvas.drawRect(new RectF(mRadius, height - mRadius, width - mRadius, height), paint);
        canvas.drawArc(new RectF(0, height - mRadius * 2, mRadius * 2, height), 90, 90, true, paint);
        canvas.drawArc(new RectF(width - mRadius * 2, height - mRadius * 2, width, height), 0, 90, true, paint);
    }

    public String getId() {
        return getClass().getName() + Math.round(mRadius);
    }

    @Override
    public void updateDiskCacheKey(MessageDigest messageDigest) {

    }

}

