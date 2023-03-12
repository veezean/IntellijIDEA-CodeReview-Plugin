package com.veezean.idea.plugin.codereviewer.model;

/**
 * 客户端查询评审信息的请求对象
 *
 * @author Veezean, 公众号 @架构悟道
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
