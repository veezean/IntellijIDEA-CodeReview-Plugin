package com.veezean.idea.plugin.codereviewer.service;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.project.Project;
import com.veezean.idea.plugin.codereviewer.util.Logger;

/**
 * <类功能简要描述>
 *
 * @author Wang Weiren
 * @since 2023/3/18
 */
public class ProjectLevelService implements Disposable {

    private Project project;

    public ProjectLevelService(final Project project) {
        this.project = project;
    }

    @Override
    public void dispose() {
        Logger.info("ProjectLevelService实例销毁:" + this);

    }
}
