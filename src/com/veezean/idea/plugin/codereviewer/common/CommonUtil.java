package com.veezean.idea.plugin.codereviewer.common;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import com.veezean.idea.plugin.codereviewer.action.ManageReviewCommentUI;

import java.io.Closeable;
import java.io.IOException;

/**
 * <类功能简要描述>
 *
 * @author admin
 * @since 2019/10/2
 */
public class CommonUtil {

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

    public synchronized static void reloadCommentListShow(Project project) {
        try {
            ManageReviewCommentUI manageReviewCommentUI = GlobalCacheManager.getInstance().getManageReviewCommentUI();
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
