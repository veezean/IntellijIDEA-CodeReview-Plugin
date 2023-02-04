package com.veezean.idea.plugin.codereviewer.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 字段下拉框枚举值定义
 *
 * @author Wang Weiren
 * @since 2022/5/21
 */
public class ColumnValueEnums implements Serializable {
    private int version;
    private Map<String, List<String>> valueEnums = new HashMap<>();

    public Map<String, List<String>> getValueEnums() {
        return valueEnums;
    }

    public void setValueEnums(Map<String, List<String>> valueEnums) {
        this.valueEnums = valueEnums;
    }

    public void putEnum(String key, List<String> values) {
        this.valueEnums.put(key, values);
    }

    public  boolean keyExist(String key) {
        return this.valueEnums.containsKey(key);
    }

    public List<String> getValuesByCode(String code) {
        List<String> values = valueEnums.get(code);
        if (values == null) {
            return new ArrayList<>();
        }
        return values;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }
}
