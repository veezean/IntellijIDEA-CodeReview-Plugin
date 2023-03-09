package com.veezean.idea.plugin.codereviewer.model;

/**
 * <类功能简要描述>
 *
 * @author Wang Weiren
 * @since 2021/6/5
 */
public class ReviewQueryParams {
    private Long projectId;
    /**
     * 全部、我提交的、我确认的
     */
    private String type = "全部";

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
