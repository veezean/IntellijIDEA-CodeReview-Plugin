package com.veezean.idea.plugin.codereviewer.action;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import com.intellij.util.Icons;
import com.veezean.idea.plugin.codereviewer.common.DataPersistentUtil;
import com.veezean.idea.plugin.codereviewer.common.GlobalCacheManager;
import org.jetbrains.annotations.NotNull;

/**
 * <类功能简要描述>
 *
 * @author Wang Weiren
 * @since 2019/9/30
 */
public class ManageReviewCommentsFactory implements ToolWindowFactory {

    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {

        //TODO 多处重复，提取下
        int projectIdentifier = DataPersistentUtil.getProjectIdentifier();
        if (projectIdentifier < 0) {
            int projectUniqueId = project.getProjectFile().toString().hashCode();
            DataPersistentUtil.setProjectIdentifier(projectUniqueId);
        }

        ManageReviewCommentUI manageReviewCommentUI = GlobalCacheManager.getInstance().getManageReviewCommentUI();
        if (manageReviewCommentUI == null) {
            manageReviewCommentUI = new ManageReviewCommentUI();
            GlobalCacheManager.getInstance().setManageReviewCommentUI(manageReviewCommentUI);
        }
        manageReviewCommentUI.initUI(project);

        ContentFactory contentFactory = ContentFactory.SERVICE.getInstance();
        Content content = contentFactory.createContent(manageReviewCommentUI.fullPanel,"", false);
        toolWindow.getContentManager().addContent(content);
        toolWindow.setIcon(Icons.UI_FORM_ICON);

    }
}
