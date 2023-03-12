package com.veezean.idea.plugin.codereviewer.model;

import java.io.Serializable;
import java.util.Map;

/**
 * 批注信息缓存实体对象
 *
 * @author Veezean, 公众号 @架构悟道
 * @since 2019/9/30
 */
public class CodeReviewCommentCache implements Serializable {
    private static final long serialVersionUID = -1770117176436016023L;
    /**
     * 记录最后一次填写的review信息，用于添加评审意见的时候，记住上一次填写的author、选择的类别等信息
     */
    private ReviewComment lastCommentData;
    private Map<String, ReviewComment> comments;

    public ReviewComment getLastCommentData() {
        return lastCommentData;
    }

    public void setLastCommentData(ReviewComment lastCommentData) {
        this.lastCommentData = lastCommentData;
    }

    public Map<String, ReviewComment> getComments() {
        return comments;
    }

    public void setComments(Map<String, ReviewComment> comments) {
        this.comments = comments;
    }

}
