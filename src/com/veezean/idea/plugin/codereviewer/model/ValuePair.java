package com.veezean.idea.plugin.codereviewer.model;

import com.veezean.idea.plugin.codereviewer.util.Logger;
import org.apache.commons.lang.StringUtils;

import java.io.Serializable;
import java.util.Objects;

/**
 * <类功能简要描述>
 *
 * @author Veezean
 * @since 2023/6/6
 */
public class ValuePair implements Serializable {
    private static final long serialVersionUID = 2224498255026085417L;
    private String value;
    private String showName;

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getShowName() {
        return showName;
    }

    public void setShowName(String showName) {
        this.showName = showName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ValuePair that = (ValuePair) o;
        return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return StringUtils.isNotEmpty(showName) ? showName : value;
    }

    public String getStringValue() {
        if (StringUtils.isEmpty(this.showName) || StringUtils.equals(this.value, this.showName)) {
            return this.value;
        }
        return this.value + "|" + this.showName;
    }

    /**
     * 根据传入的字符串进行构建pair对象，传入的字符串可能是有分隔符|的字段
     * 注意：该方法适用于那种k-v的场景，对于可能随便填写字符串的场景(比如comment或者是content)，不要调用此字段
     *
     * @param stringValue
     * @return
     */
    public static ValuePair buildPairSupportSplit(String stringValue) {
        ValuePair valuePair = new ValuePair();
        if (StringUtils.isEmpty(stringValue)) {
            return valuePair;
        }
        String[] split = stringValue.split("\\|");
        if (split.length == 1) {
            return ValuePair.buildPair(split[0], null);
        }
        if (split.length == 2) {
            return ValuePair.buildPair(split[0], split[1]);
        }

        Logger.error("pair数据错误：" + stringValue);
        return valuePair;
    }

    public static ValuePair buildRawPair(String stringValue) {
        ValuePair valuePair = new ValuePair();
        if (StringUtils.isEmpty(stringValue)) {
            return valuePair;
        }
        return ValuePair.buildPair(stringValue, null);
    }

    public static ValuePair buildPair(String value, String showName) {
        ValuePair valuePair = new ValuePair();
        valuePair.setValue(value);
        valuePair.setShowName(showName);
        return valuePair;
    }
}
