package com.veezean.idea.plugin.codereviewer.consts;

import com.intellij.openapi.util.Key;

/**
 * 常量类
 *
 * @author Veezean
 * @since 2021/4/26
 */
public class Constants {

    public static final int ADD_COMMENT = 0;
    public static final int DETAIL_COMMENT = 1;
    public static final String UNCONFIRMED = "unconfirmed";

    public static final String CODE_REVIEW_HELPER_MARKER = "CODE_REVIEW_HELPER_MARKER";
    public static final String CODE_REVIEW_HELPER_MARKER_UNCONFIRMED = "CODE_REVIEW_HELPER_MARKER_UNCONFIRMED";

    public static final Key<Object> HIGHTLIGHT_MARKER = Key.create(CODE_REVIEW_HELPER_MARKER);

}
