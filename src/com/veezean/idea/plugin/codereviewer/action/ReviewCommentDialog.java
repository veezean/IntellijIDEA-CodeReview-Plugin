package com.veezean.idea.plugin.codereviewer.action;

import com.intellij.openapi.project.Project;
import com.veezean.idea.plugin.codereviewer.common.Constants;
import com.veezean.idea.plugin.codereviewer.model.ReviewComment;

import javax.swing.*;
import java.awt.*;

/**
 * <类功能简要描述>
 *
 * @author Wang Weiren
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

        // 屏幕中心显示
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int w = (screenSize.width - WIDTH) / 2;
        int h = (screenSize.height * 95 / 100 - HEIGHT) / 2;
        dialog.setLocation(w, h);
        dialog.setModal(true);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.pack();
        dialog.setVisible(true);
    }
}
