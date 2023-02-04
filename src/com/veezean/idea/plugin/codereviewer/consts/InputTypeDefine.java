package com.veezean.idea.plugin.codereviewer.consts;

/**
 * 输入类型定义
 *
 * @author Wang Weiren
 * @since 2022/5/22
 */
public enum InputTypeDefine {
    TEXT("TEXT"),
    TEXT_AREA("TEXT_AREA"),
    COMBO_BOX("COMBO_BOX");

    private String value;

    InputTypeDefine(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
