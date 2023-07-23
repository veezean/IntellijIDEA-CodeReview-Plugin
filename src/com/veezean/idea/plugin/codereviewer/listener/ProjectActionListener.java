package com.veezean.idea.plugin.codereviewer.listener;

import cn.hutool.cron.CronUtil;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.project.ProjectManagerListener;
import com.veezean.idea.plugin.codereviewer.service.ProjectLevelService;
import com.veezean.idea.plugin.codereviewer.util.Logger;
import org.jetbrains.annotations.NotNull;

/**
 * 应用级别监听，监听项目打开关闭事件并处理
 *
 * @author Veezean
 * @since 2023/3/18
 */
public class ProjectActionListener implements ProjectManagerListener {

    @Override
    public void projectOpened(@NotNull Project project) {
        Logger.info("【项目打开】项目名称：" + project.getName() + "项目路径：" + project.getProjectFilePath());

        // 启动定时器执行
        startScheduler();

        project.getService(ProjectLevelService.class).onProjectOpend();
    }

    private void startScheduler() {
        // app级别全局，支持秒级别定时任务
        CronUtil.setMatchSecond(true);
        if (CronUtil.getScheduler().isStarted()) {
            Logger.info("定时服务已经启动...");
        } else {
            Logger.info("启动定时服务...");
            CronUtil.start();
        }
    }

    @Override
    public void projectClosed(@NotNull Project project) {
        Logger.info("【项目关闭】项目名称：" + project.getName() + "项目路径：" + project.getProjectFilePath());
        project.getService(ProjectLevelService.class).onProjectClosed();

    }
}
