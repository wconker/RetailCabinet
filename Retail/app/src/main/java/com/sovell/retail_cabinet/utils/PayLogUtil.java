package com.sovell.retail_cabinet.utils;

import android.os.Environment;
import android.text.TextUtils;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class PayLogUtil {

    private static final String ORDER_PATH = Environment.getExternalStorageDirectory().toString() + "/零售柜订单/";
    /*PASS退款订单文件*/
    public static final String REFUND = ORDER_PATH + "退款订单.txt";

    /*PASS退款订单文件*/
    public static final String PASS_REFUND = ORDER_PATH + "Pass退款.txt";
    /*pass异常订单文件*/
    public static final String PASS_UNUSUAL = ORDER_PATH + "Pass异常订单.txt";
    /*智盘上传订单文件*/
    public static final String DISH_PAY = ORDER_PATH + "智盘未入账.txt";
    /*智盘异常订单文件*/
    public static final String DISH_UNUSUAL = ORDER_PATH + "智盘异常订单.txt";


    //生成文件
    private static File makeFile(String fileName) {
        File file = null;
        if (makeDirectory()) {
            try {
                file = new File(fileName);
                if (!file.exists()) {
                    boolean result = file.createNewFile();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return file;
    }

    //生成文件夹
    private static boolean makeDirectory() {
        File file1 = new File(ORDER_PATH);
        if (!file1.exists()) {
            return file1.mkdir();
        }
        return true;
    }

    //写入文件
    private synchronized static <T> boolean writeFile(String fileName, List<T> logList) {
        BufferedWriter bw = null;
        try {
            File file = new File(fileName);
            bw = new BufferedWriter(new FileWriter(file));
            bw.write(new Gson().toJson(logList));
            bw.flush();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (bw != null) {
                try {
                    bw.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return false;
    }

    //删除订单日志
    public static <T> boolean deleteLog(String fileName, T object, Class<T> tClass) {
        synchronized (PayLogUtil.class) {
            if (object == null) {
                return false;
            }
            List<T> logList = readPayLog(fileName, tClass);
            if (logList != null && logList.contains(object)) {
                logList.remove(object);
                return writeFile(fileName, logList);
            }
            return false;
        }
    }

    //保存订单
    public static <T> boolean saveLog(String fileName, T object, Class<T> tClass) {
        synchronized (PayLogUtil.class) {
            if (object == null) {
                return false;
            }
            List<T> logList = readPayLog(fileName, tClass);
            if (logList != null && !logList.contains(object)) {
                logList.add(object);
                return writeFile(fileName, logList);
            }
            return false;
        }
    }

    //查询订单
    public static <T> List<T> readPayLog(String fileName, Class<T> tClass) {
        synchronized (PayLogUtil.class) {
            List<T> payLogBeen = new ArrayList<>();
            File file = makeFile(fileName);
            if (file == null) {
                return payLogBeen;
            }

            BufferedReader br = null;
            try {
                br = new BufferedReader(new FileReader(file));
                String readLine;
                StringBuilder buffer = new StringBuilder();
                while ((readLine = br.readLine()) != null) {
                    buffer.append(readLine);
                }
                String logJson = buffer.toString();
                if (!TextUtils.isEmpty(logJson)) {
                    List<T> logList = new Gson().fromJson(logJson, new TypeImpl(tClass));
                    if (logList != null) {
                        payLogBeen.addAll(logList);
                    }
                }
                return payLogBeen;
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (br != null) {
                    try {
                        br.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            return payLogBeen;
        }
    }

    //Gson Type
    private static class TypeImpl implements ParameterizedType {
        Class clazz;

        public TypeImpl(Class clz) {
            clazz = clz;
        }

        @Override
        public Type[] getActualTypeArguments() {
            return new Type[]{clazz};
        }

        @Override
        public Type getRawType() {
            return List.class;
        }

        @Override
        public Type getOwnerType() {
            return null;
        }
    }

}
