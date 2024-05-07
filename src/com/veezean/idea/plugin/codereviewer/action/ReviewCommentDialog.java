package com.veezean.idea.plugin.codereviewer.action;

import com.intellij.openapi.project.Project;
import com.veezean.idea.plugin.codereviewer.common.InnerProjectCache;
import com.veezean.idea.plugin.codereviewer.consts.Constants;
import com.veezean.idea.plugin.codereviewer.model.ReviewComment;
import com.veezean.idea.plugin.codereviewer.service.ProjectLevelService;
import com.veezean.idea.plugin.codereviewer.util.CommonUtil;

import javax.swing.*;
import java.awt.*;
import java.util.Optional;

/**
 * 评审意见管理界面
 *
 * @author Veezean
 * @since 2023/2/4
 */
public class ReviewCommentDialog {

    private static final int WIDTH = 900;
    private static final int HEIGHT = 800;

    public static void show(ReviewComment model, Project project, int operateType) {
        JDialog dialog = new JDialog();
        String title = "添加评审意见";
        if (operateType == Constants.CONFIRM_COMMENT) {
            title = "评审意见确认";
        }
        dialog.setTitle(title);

        AddReviewCommentUI reviewCommentUI = new AddReviewCommentUI();
        // 面板UI初始化
        reviewCommentUI.initComponent(dialog, model, project, operateType);
        // 将内容面板加到弹窗中
        reviewCommentUI.addPanelToContainer(dialog);

        JRootPane rootPane = Optional.ofNullable(ProjectLevelService.getService(project))
                .map(ProjectLevelService::getProjectCache)
                .map(InnerProjectCache::getManageReviewCommentUI)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(manageReviewCommentUI -> manageReviewCommentUI.fullPanel)
                .map(JComponent::getRootPane)
                .orElse(null);

        dialog.setLocation(CommonUtil.getWindowRelativePoint(rootPane, WIDTH, HEIGHT));
        dialog.setModal(true);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.pack();
        // 解决java.lang.Throwable: Thread context was already set问题
        SwingUtilities.invokeLater(() -> dialog.setVisible(true));
    }
}
