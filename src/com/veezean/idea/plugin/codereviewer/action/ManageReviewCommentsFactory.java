package com.veezean.idea.plugin.codereviewer.action;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import com.veezean.idea.plugin.codereviewer.service.ProjectLevelService;
import com.veezean.idea.plugin.codereviewer.util.CommonUtil;
import org.jetbrains.annotations.NotNull;

/**
 * 评审信息管理类（窗口工具类）
 *
 * @author Veezean
 * @since 2019/9/30
 */
public class ManageReviewCommentsFactory implements ToolWindowFactory {

    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {

        ManageReviewCommentUI managerUI = new ManageReviewCommentUI(project);
                ProjectLevelService.getService(project).getProjectCache().setManageReviewCommentUI(managerUI);
        managerUI.initUI();

        ContentFactory contentFactory = ContentFactory.SERVICE.getInstance();
        Content content = contentFactory.createContent(managerUI.fullPanel,"", false);
        toolWindow.getContentManager().addContent(content);
        toolWindow.setIcon(CommonUtil.getToolWindowIcon());

    }
}
