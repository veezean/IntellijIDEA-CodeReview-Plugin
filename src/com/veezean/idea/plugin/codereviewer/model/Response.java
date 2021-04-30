package com.veezean.idea.plugin.codereviewer.model;

import lombok.Data;

/**
 * 通用response响应类
 *
 * @author Wang Weiren
 * @since 2021/4/25
 */
@Data
public class Response<T>{

    private int code;
    private String message;
    private T data;

}
