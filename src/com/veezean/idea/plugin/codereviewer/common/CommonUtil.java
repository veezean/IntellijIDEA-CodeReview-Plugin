package com.veezean.idea.plugin.codereviewer.common;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import com.veezean.idea.plugin.codereviewer.action.ManageReviewCommentUI;

import java.io.Closeable;
import java.io.IOException;

/**
 * 通用工具类
 *
 * @author Wang Weiren
 * @since 2019/10/2
 */
public class CommonUtil {

    /**
     * 静默关流处理方法
     *
     * @param closeable 可关闭流
     */
    public static void closeQuitely(Closeable closeable) {
        if (closeable == null) {
            return;
        }

        try {
            closeable.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 重新加载指定项目的评审信息
     *
     * @param project 待处理的项目
     */
    public synchronized static void reloadCommentListShow(Project project) {
        try {
            InnerProjectCache projectCache = ProjectInstanceManager.getInstance().getProjectCache(project.getLocationHash());

            ManageReviewCommentUI manageReviewCommentUI = projectCache.getManageReviewCommentUI();
            ToolWindow toolWindow = ToolWindowManager.getInstance(project).getToolWindow("CodeReview");

            if (manageReviewCommentUI != null && toolWindow != null) {
                manageReviewCommentUI.refreshTableDataShow();
            } else {
                System.out.println("manageReviewCommentUI = " + manageReviewCommentUI);
                System.out.println("toolWindow = " + toolWindow);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
