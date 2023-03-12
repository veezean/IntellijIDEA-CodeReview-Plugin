package com.veezean.idea.plugin.codereviewer.model;

import java.util.List;

/**
 * 与客户端之间交互的提交评审意见的对象
 *
 * @author Veezean, 公众号 @架构悟道
 * @since 2021/4/26
 */
public class CommitComment {
    private Long projectId;
    private List<CommentReqBody> comments;

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public List<CommentReqBody> getComments() {
        return comments;
    }

    public void setComments(List<CommentReqBody> comments) {
        this.comments = comments;
    }
}
