package com.veezean.idea.plugin.codereviewer.model;

import lombok.Data;

import java.util.List;

/**
 * <类功能简要描述>
 *
 * @author Wang Weiren
 * @since 2021/4/26
 */
@Data
public class CommitComment {
    private String projectKey;
    private String commitUser;
    private List<Comment> comments;
}
