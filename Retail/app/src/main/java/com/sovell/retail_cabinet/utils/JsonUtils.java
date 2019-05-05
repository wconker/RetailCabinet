package com.sovell.retail_cabinet.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializeConfig;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.alibaba.fastjson.serializer.SimpleDateFormatSerializer;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * json工具类
 */
public class JsonUtils {

    private static SerializeConfig mapping = new SerializeConfig();
    private static String dateFormat;

    static {
        dateFormat = "yyyy-MM-dd HH:mm:ss";
        mapping.put(Date.class, new SimpleDateFormatSerializer(dateFormat));
        mapping.put(Timestamp.class, new SimpleDateFormatSerializer(dateFormat));
    }

    /**
     * 将对象转换为json
     */
    public static String convertObjectToJson(Object obj) {
        return JSON.toJSONString(obj, mapping);
    }

    /**
     * 将Map格式的Json转换成Map对象
     */
    @SuppressWarnings("unchecked")
    public static Map<String, Object> convertJsonToObject(String json) {
        return JSON.parseObject(json, Map.class);
    }

    /**
     * 将Json转换成指定类的对象
     */
    public static <T> T convertJsonToObject(String json, Class<T> classOfT) {
        return JSON.parseObject(json, classOfT);
    }

    /**
     * json格式化
     */
    public static String jsonFormatter(Object obj) {
        return JSON.toJSONString(obj, mapping, SerializerFeature.PrettyFormat);
        //return JSON.toJSONString(obj,true);
    }

    /**
     * 将Json转换成List
     */
    public static <T> List<T> convertJsonToList(String json, Class<T> classOfT) {
        return JSON.parseArray(json, classOfT);
    }
}