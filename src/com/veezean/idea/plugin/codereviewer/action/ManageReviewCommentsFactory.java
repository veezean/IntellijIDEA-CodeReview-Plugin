package com.veezean.idea.plugin.codereviewer.action;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import com.veezean.idea.plugin.codereviewer.util.CommonUtil;
import com.veezean.idea.plugin.codereviewer.common.InnerProjectCache;
import com.veezean.idea.plugin.codereviewer.common.ProjectInstanceManager;
import org.jetbrains.annotations.NotNull;

/**
 * 评审信息管理类（窗口工具类）
 *
 * @author Veezean, 公众号 @架构悟道
 * @since 2019/9/30
 */
public class ManageReviewCommentsFactory implements ToolWindowFactory {

    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        // 打开不同的window窗口的时候，会进来一次
        // 由于不同窗口，插件是同一个进程，因此UI示例必须要分开
        String locationHash = project.getLocationHash();

        // 清除缓存的项目信息，因为项目关闭的时候，project已经销毁，idea进程没有退出的情况下再次打开此项目的时候会报错
        // 所以每次启动的时候先清理掉上次残留的缓存
        // TODO 这里尝试注册service在project销毁时自动清理，暂未成功，待研究
        ProjectInstanceManager.getInstance().removeProjectCache(locationHash);
        InnerProjectCache projectCache = new InnerProjectCache(project);
        ProjectInstanceManager.getInstance().addProjectCache(project.getLocationHash(), projectCache);

        ManageReviewCommentUI manageReviewCommentUI = projectCache.getManageReviewCommentUI();
        if (manageReviewCommentUI == null) {
            manageReviewCommentUI = new ManageReviewCommentUI(project);
            projectCache.setManageReviewCommentUI(manageReviewCommentUI);
        }
        manageReviewCommentUI.initUI();

        ContentFactory contentFactory = ContentFactory.SERVICE.getInstance();
        Content content = contentFactory.createContent(manageReviewCommentUI.fullPanel,"", false);
        toolWindow.getContentManager().addContent(content);
        toolWindow.setIcon(CommonUtil.getDefaultIcon());

    }
}
