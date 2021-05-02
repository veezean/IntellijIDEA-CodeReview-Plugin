package com.veezean.idea.plugin.codereviewer.model;

/**
 * 通用response响应类
 *
 * @author Wang Weiren
 * @since 2021/4/25
 */
public class Response<T>{

    private int code;
    private String message;
    private T data;

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}
