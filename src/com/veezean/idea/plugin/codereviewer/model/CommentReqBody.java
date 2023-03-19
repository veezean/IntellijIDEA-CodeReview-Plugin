package com.veezean.idea.plugin.codereviewer.model;

import java.util.HashMap;
import java.util.Map;

/**
 * 与客户端间交互的评审意见请求对象
 *
 * @author Veezean, 公众号 @架构悟道
 * @since 2021/4/26
 */
public class CommentReqBody {

    // 本地存储的每条评审信息
    private Map<String, String> propValues = new HashMap<>();

    public Map<String, String> getPropValues() {
        return propValues;
    }

    public void setPropValues(Map<String, String> propValues) {
        this.propValues = propValues;
    }
}
