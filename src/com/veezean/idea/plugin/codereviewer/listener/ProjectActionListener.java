package com.veezean.idea.plugin.codereviewer.listener;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.project.ProjectManagerListener;
import com.veezean.idea.plugin.codereviewer.service.ProjectLevelService;
import com.veezean.idea.plugin.codereviewer.util.Logger;
import org.jetbrains.annotations.NotNull;

/**
 * <类功能简要描述>
 *
 * @author Wang Weiren
 * @since 2023/3/18
 */
public class ProjectActionListener implements ProjectManagerListener {
    @Override
    public void projectOpened(@NotNull Project project) {
        Logger.info("【项目打开】项目名称：" + project.getName() + "项目路径：" + project.getProjectFilePath());

        ProjectLevelService service = project.getService(ProjectLevelService.class);
        Logger.info("项目service类：" + service);
    }

    @Override
    public void projectClosed(@NotNull Project project) {
        Logger.info("【项目关闭】项目名称：" + project.getName() + "项目路径：" + project.getProjectFilePath());

    }
}
