package com.sovell.retail_cabinet.base;

import com.google.gson.Gson;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * 所有对象的父类
 * 使用Gson转换Http返回的JSON
 * code     服务端响应码
 * message  服务端响应信息
 */
public class BaseBean<T> {

    private int code;

    private String message;

    private T result;

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMsg() {
        return message;
    }

    public void setMsg(String msg) {
        this.message = msg;
    }

    public T getResult() {
        return result;
    }

    public void setResult(T result) {
        this.result = result;
    }

    public boolean success() {
        return code == 200 || code == 1;
    }

    static BaseBean fromJson(String json, Class clazz) {
        Gson gson = new Gson();
        Type objectType = type(BaseBean.class, clazz);
        return gson.fromJson(json, objectType);
    }

    public String toJson(Class<T> clazz) {
        Gson gson = new Gson();
        Type objectType = type(BaseBean.class, clazz);
        return gson.toJson(this, objectType);
    }

    private static ParameterizedType type(final Class raw, final Type... args) {
        return new ParameterizedType() {
            public Type getRawType() {
                return raw;
            }

            public Type[] getActualTypeArguments() {
                return args;
            }

            public Type getOwnerType() {
                return null;
            }
        };
    }

}
