package com.veezean.idea.plugin.codereviewer.model;

import java.util.HashMap;
import java.util.Map;

/**
 * 与客户端间交互的评审意见请求对象
 *
 * @author Veezean
 * @since 2021/4/26
 */
public class CommentBody {

    private String id;
    private long dataVersion;
    private Map<String, ValuePair> values;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public long getDataVersion() {
        return dataVersion;
    }

    public void setDataVersion(long dataVersion) {
        this.dataVersion = dataVersion;
    }

    public Map<String, ValuePair> getValues() {
        return values;
    }

    public void setValues(Map<String, ValuePair> values) {
        this.values = values;
    }

    public void convertAndSetValues(Map<String, ValuePair> valueMaps) {
        this.values = new HashMap<>();
        this.values.putAll(valueMaps);
    }
}
