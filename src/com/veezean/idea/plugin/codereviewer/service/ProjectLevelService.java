package com.veezean.idea.plugin.codereviewer.service;

import cn.hutool.cron.CronUtil;
import cn.hutool.cron.task.Task;
import com.intellij.openapi.project.Project;
import com.veezean.idea.plugin.codereviewer.action.ManageReviewCommentUI;
import com.veezean.idea.plugin.codereviewer.common.InnerProjectCache;
import com.veezean.idea.plugin.codereviewer.util.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 项目级别service，每个project一个实例
 *
 * @author Veezean
 * @since 2023/3/18
 */
public class ProjectLevelService{

    private InnerProjectCache projectCache;

    private List<String> schedulerIds = Collections.synchronizedList(new ArrayList<>());

    private Project project;

    public ProjectLevelService(final Project project) {
        this.project = project;
        this.projectCache = new InnerProjectCache(this.project);
    }

    /**
     * 工具封装，获取给定项目自身的service（项目级别唯一实例）
     *
     * @param project
     * @return
     */
    public static ProjectLevelService getService(Project project) {
        return project.getService(ProjectLevelService.class);
    }

    public InnerProjectCache getProjectCache() {
        return this.projectCache;
    }

    /**
     * 添加项目级别的定时任务
     *
     * @param cron
     * @param task
     */
    public void createScheduler(String cron, Task task) {
        String scheduleId = CronUtil.schedule(cron, task);
        schedulerIds.add(scheduleId);
        Logger.info("添加定时任务：" + scheduleId);
    }


    public void onProjectOpend() {
        Logger.info("项目打开，执行onProjectOpend方法[IN]" + this);

        Logger.info("项目打开，执行onProjectOpend方法[OUT]" + this);
    }

    public void onProjectClosed() {
        Logger.info("项目关闭，执行onProjectClosed方法[IN]" + this);

        for (String schedulerId : schedulerIds) {
            CronUtil.remove(schedulerId);
            Logger.info("移除定时任务：" + schedulerId);
        }

        Logger.info("项目关闭，执行onProjectClosed方法[OUT]" + this);
    }
}
