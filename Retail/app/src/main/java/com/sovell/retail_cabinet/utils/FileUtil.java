package com.sovell.retail_cabinet.utils;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.support.v4.content.FileProvider;

import com.sovell.retail_cabinet.app.RetailCabinetApp;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DecimalFormat;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;

/**
 * 文件管理类
 */

public class FileUtil {

    /*存储照片的地址*/
    public static final String DOWNLOAD_PICTURE = Environment.getExternalStorageDirectory().toString() + "/Pictures/";
    /*存储下载apk地址*/
    public static final String DOWNLOAD_PAK = Environment.getExternalStorageDirectory().toString() + "/Download/";

    /**
     * 获取SD卡路径
     */
    public static String getSDPath() {
        boolean sdCardExist = Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
        if (sdCardExist) {
            return Environment.getExternalStorageDirectory().toString() + File.separator;
        }
        return "";
    }

    /**
     * 获取缓存路径
     */
    public static String getCachePath() {
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())
                || !Environment.isExternalStorageRemovable()) {
            if (RetailCabinetApp.Instance().getExternalCacheDir() != null) {
                return RetailCabinetApp.Instance().getExternalCacheDir().getPath();
            }
        } else {
            return RetailCabinetApp.Instance().getCacheDir().getPath();
        }
        return "";
    }

    /**
     * 删除文件夹中的APK
     *
     * @param path 路径
     */
    public static void deleteDirApk(String path) {
        File dir = new File(path);
        if (!dir.exists() || !dir.isDirectory()) return;
        for (File file : dir.listFiles()) {
            if (file.isFile() && file.getName().endsWith(".apk")) {
                boolean result = file.delete();
            }
        }
    }

    /**
     * 保存文件到本地
     */
    public static boolean saveResponseBody(ResponseBody body, String filePath) {
        File futureStudioIconFile = new File(filePath);
        InputStream inputStream = null;
        OutputStream outputStream = null;
        try {
            byte[] fileReader = new byte[1024];
            inputStream = body.byteStream();
            outputStream = new FileOutputStream(futureStudioIconFile);
            while (true) {
                int read = inputStream.read(fileReader);
                if (read == -1) {
                    break;
                }
                outputStream.write(fileReader, 0, read);
            }
            outputStream.flush();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
                if (outputStream != null) {
                    outputStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    /**
     * 复制文件
     *
     * @param source 输入文件
     * @param target 输出文件
     */
    public static void copy(File source, File target) {
        FileInputStream fileInputStream = null;
        FileOutputStream fileOutputStream = null;
        try {
            fileInputStream = new FileInputStream(source);
            fileOutputStream = new FileOutputStream(target);
            byte[] buffer = new byte[1024];
            while (fileInputStream.read(buffer) > 0) {
                fileOutputStream.write(buffer);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (fileInputStream != null) {
                    fileInputStream.close();
                }
                if (fileOutputStream != null) {
                    fileOutputStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 安装APK
     */
    public static void openApkFile(Context context, File file) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (Build.VERSION.SDK_INT >= 24) {
            Uri apkUri = FileProvider.getUriForFile(context, "com.sovell.retail_cabinet.FileProvider", file);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.setDataAndType(apkUri, "application/vnd.android.package-archive");
        } else {
            intent.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive");
        }
        context.startActivity(intent);
    }

    /**
     * 下载时，查看文件大小
     */
    public static String getDataSize(long var0) {
        DecimalFormat var2 = new DecimalFormat("###.00");
        return var0 < 1024L ? var0 + "bytes" : (var0 < 1048576L ? var2.format((double) ((float) var0 / 1024.0F))
                + "KB" : (var0 < 1073741824L ? var2.format((double) ((float) var0 / 1024.0F / 1024.0F))
                + "MB" : (var0 < 0L ? var2.format((double) ((float) var0 / 1024.0F / 1024.0F / 1024.0F))
                + "GB" : "error")));
    }

    /**
     * 封装上传图片、文件
     */
    public static List<MultipartBody.Part> getFileParts(List<File> fileList) {
        if (fileList == null || fileList.size() < 1) {
            return null;
        }
        MultipartBody.Builder builder = null;
        for (File file : fileList) {
            builder = new MultipartBody.Builder().setType(MultipartBody.FORM);//表单类型
            RequestBody imageBody = RequestBody.create(MediaType.parse("multipart/form-data"), file);
            builder.addFormDataPart("img", file.getName(), imageBody);//imgfile 后台接收图片流的参数名
        }
        return builder.build().parts();
    }
}
