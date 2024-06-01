package com.veezean.idea.plugin.codereviewer.consts;

import com.veezean.idea.plugin.codereviewer.action.element.*;
import com.veezean.idea.plugin.codereviewer.model.Column;
import org.apache.commons.lang.StringUtils;

import java.util.Arrays;

/**
 * 输入类型定义
 *
 * @author Veezean
 * @since 2022/5/22
 */
public enum InputTypeDefine {
    TEXT("TEXT", new TextFieldCreator()),
    TEXT_AREA("TEXTAREA", new TextAreaCreator()),
    TEXT_AREA_CODE("TEXTAREA_CODE", new EditorTextFieldCreator()),
    COMBO_BOX("COMBO_BOX", new CombBoxCreator()),
    DATE("DATE", new DateSelectCreator());

    private String value;
    private IElementCreator elementCreator;

    InputTypeDefine(String value, IElementCreator elementCreator) {
        this.value = value;
        this.elementCreator = elementCreator;
    }

    public String getValue() {
        return value;
    }

    public IElementCreator getElementCreator() {
        return elementCreator;
    }

    public static IElementCreator getElementCreator(Column column) {
        // IDEA插件端，针对代码区域textarea进行特殊处理
        // 这边类型不定义新的类型，主要是为了兼容服务端实现，服务端无需感知两种不同类型，所以配置层面，始终都是一个TEXT_AREA类型
        String inputType = column.getInputType();
        if (TEXT_AREA.getValue().equals(inputType) && "content".equals(column.getColumnCode())) {
            inputType = TEXT_AREA_CODE.getValue();
        }
        final String columnInputType = inputType;

        return Arrays.stream(values()).filter(inputTypeDefine -> StringUtils.equals(inputTypeDefine.getValue(), columnInputType))
                .findFirst()
                .orElse(TEXT)
                .getElementCreator();
    }

    public static boolean isComboBox(String inputType) {
        return COMBO_BOX.getValue().equals(inputType);
    }

    public static boolean isTextArea(String inputType) {
        return TEXT_AREA.getValue().equals(inputType);
    }

    public static boolean isDateSelector(String inputType) {
        return DATE.getValue().equals(inputType);
    }
}
