package com.veezean.idea.plugin.codereviewer.model;

import java.util.List;

/**
 * 与客户端之间交互的提交评审意见的对象
 *
 * @author Veezean, 公众号 @架构悟道
 * @since 2021/4/26
 */
public class CommitComment {
    private List<CommentBody> comments;

    public List<CommentBody> getComments() {
        return comments;
    }

    public void setComments(List<CommentBody> comments) {
        this.comments = comments;
    }
}
