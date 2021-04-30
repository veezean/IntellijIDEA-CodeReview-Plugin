package com.veezean.idea.plugin.codereviewer.model;

import lombok.Data;

/**
 * <类功能简要描述>
 *
 * @author Wang Weiren
 * @since 2021/4/26
 */
@Data
public class ProjectEntity {
    private String projectKey;
    private String projectName;

    @Override
    public String toString() {
        return projectName;
    }
}
