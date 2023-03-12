package com.veezean.idea.plugin.codereviewer.model;

import java.io.Serializable;

/**
 * 服务端项目简要信息
 *
 * @author Veezean, 公众号 @架构悟道
 * @since 2021/4/26
 */
public class ServerProjectShortInfo implements Serializable {
    private static final long serialVersionUID = -1770117176436016032L;

    private Long projectId;
    private String projectName;

    @Override
    public String toString() {
        return projectName;
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }
}
