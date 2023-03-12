package com.veezean.idea.plugin.codereviewer.common;

/**
 * 异常封装类
 *
 * @author Veezean, 公众号 @架构悟道
 * @since 2021/4/26
 */
public class CodeReviewException extends RuntimeException {
    public CodeReviewException(String message) {
        super(message);
    }

    public CodeReviewException(String message, Throwable cause) {
        super(message, cause);
    }
}
