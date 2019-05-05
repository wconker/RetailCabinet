package com.sovell.retail_cabinet.glide;

import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.widget.ImageView;

import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.sovell.retail_cabinet.R;
import com.sovell.retail_cabinet.utils.DeviceUtil;
import com.sovell.retail_cabinet.utils.FileUtil;

import java.io.File;

/**
 * diskCacheStrategy()方法基本上就是Glide硬盘缓存功能的一切，它可以接收五种参数：
 * <p>
 * DiskCacheStrategy.NONE： 表示不缓存任何内容。
 * DiskCacheStrategy.DATA： 表示只缓存原始图片。
 * DiskCacheStrategy.RESOURCE： 表示只缓存转换过后的图片。
 * DiskCacheStrategy.ALL ： 表示既缓存原始图片，也缓存转换过后的图片。
 * DiskCacheStrategy.AUTOMATIC： 表示让Glide根据图片资源智能地选择使用哪一种缓存策略（默认选项）。
 * <p>
 * override(Target.SIZE_ORIGINAL)
 * 可以使用Target.SIZE_ORIGINAL关键字，加载一张图片的原始尺寸
 */
public class GlideUtil {

    private RequestOptions mRequestOptions;

    private static class GlideUtilHolder {
        private static final GlideUtil INSTANCE = new GlideUtil();
    }

    public static GlideUtil Instance() {
        return GlideUtilHolder.INSTANCE;
    }

    public GlideUtil() {
        mRequestOptions = new RequestOptions()
                .centerCrop() //4/26修改
                .placeholder(R.drawable.ic_def_big)
                .error(R.drawable.ic_def_big);
    }

    /**
     * 加载网络图片
     */
    public void loadImage(Context context, Object obj, ImageView imageView) {
        GlideApp.with(context)
                .load(obj)
                .apply(mRequestOptions)
                .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                .into(imageView);
    }

    /**
     * 加载圆形图片
     */
    public void loadCircleImage(Context context, Object obj, ImageView imageView) {
        GlideApp.with(context)
                .load(obj)
                .apply(mRequestOptions)
                .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                .into(imageView);
    }

    /**
     * 加载圆角图片
     */
    public void loadRoundImage(Context context, Object obj, ImageView imageView, RoundTransformation transformation) {
        GlideApp.with(context)
                .load(obj)
                .apply(mRequestOptions)
                .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                .transform(transformation)
                .into(imageView);
    }

    /**
     * 下载图片
     */
    public void download(Context context, final String url) {
        GlideApp.with(context)
                .asFile()
                .load(url)
                .into(new SimpleTarget<File>() {
                    @Override
                    public void onResourceReady(@NonNull File resource, @Nullable Transition<? super File> transition) {
                        File parent = new File(FileUtil.DOWNLOAD_PICTURE);
                        if (!parent.exists()) {
                            boolean result = parent.mkdirs();
                        }
                        File child = new File(parent, DeviceUtil.stringToMD5(url) + ".jpg");
                        FileUtil.copy(resource, child);
                    }
                });
    }

    /**
     * 清除View的缓存
     */
    public void clearView(Context context, ImageView imageView) {
        GlideApp.with(context).clear(imageView);
    }

    /**
     * 清除所有缓存
     */
    public void clearMemoryCache(Context context) {
        GlideApp.get(context).clearMemory();
    }

    /**
     * 清除磁盘存储（必须异步执行）
     */
    public static void clearDiskCache(final Context context) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                GlideApp.get(context).clearDiskCache();
                return null;
            }
        }.execute();
    }
}
