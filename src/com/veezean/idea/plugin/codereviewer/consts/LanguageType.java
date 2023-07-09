package com.veezean.idea.plugin.codereviewer.consts;

import java.util.stream.Stream;

/**
 * <类功能简要描述>
 *
 * @author Veezean
 * @since 2023/3/19
 */
public enum LanguageType {
    ENGLISH(0, "英文"),
    CHINESE(1, "中文");
    private int value;
    private String desc;
    LanguageType(int value, String desc) {
        this.value = value;
        this.desc = desc;
    }
    public int getValue() {
        return this.value;
    }
    public static LanguageType languageType(int type) {
        return Stream.of(values()).filter(languageType -> languageType.value == type).findFirst().orElse(ENGLISH);
    }
}
