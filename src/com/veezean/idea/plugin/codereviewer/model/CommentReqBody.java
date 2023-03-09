package com.veezean.idea.plugin.codereviewer.model;

import java.util.HashMap;
import java.util.Map;

/**
 * <类功能简要描述>
 *
 * @author Wang Weiren
 * @since 2021/4/26
 */
public class CommentReqBody {
    // 用于记录DB中唯一ID，供后续扩展更新场景使用
    private Long entityUniqueId = -1L;

    // 本地存储的每条评审信息
    private Map<String, String> propValues = new HashMap<>();

    public long getEntityUniqueId() {
        return entityUniqueId;
    }

    public void setEntityUniqueId(long entityUniqueId) {
        this.entityUniqueId = entityUniqueId;
    }

    public Map<String, String> getPropValues() {
        return propValues;
    }

    public void setPropValues(Map<String, String> propValues) {
        this.propValues = propValues;
    }
}
