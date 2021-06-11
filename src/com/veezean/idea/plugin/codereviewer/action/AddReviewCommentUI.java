package com.veezean.idea.plugin.codereviewer.action;

import com.intellij.openapi.project.Project;
import com.veezean.idea.plugin.codereviewer.common.CommonUtil;
import com.veezean.idea.plugin.codereviewer.common.Constants;
import com.veezean.idea.plugin.codereviewer.common.InnerProjectCache;
import com.veezean.idea.plugin.codereviewer.common.ProjectInstanceManager;
import com.veezean.idea.plugin.codereviewer.model.ReviewCommentInfoModel;

import javax.swing.*;
import java.awt.*;

/**
 * 添加评审操作的主界面
 *
 * @author Wang Weiren
 * @since 2019/9/29
 */
public class AddReviewCommentUI {

    private static final int WIDTH = 700;
    private static final int HEIGHT = 800;

    private JTextField reviewerTextField;
    private JTextArea commentsTextArea;
    private JComboBox questionTypeComboBox;
    private JComboBox severityComboBox;
    private JComboBox triggerFactorComboBox;
    private JButton saveButton;
    private JButton cancelButton;
    private JPanel addReviewCommentPanel;
    private JTextField filePathTextField;
    private JTextArea codeContentsTextArea;
    private JTextField lineTextField;
    private JTextField handlerTextField;
    private JTextField projectVersionTextField;
    private JTextField belongIssueTextField;
    private JLabel titleLable;
    private JComboBox confirmResultComboBox;
    private JTextArea confirmNotesTextArea;
    private JPanel confirmPanel;

    public static void showDialog(ReviewCommentInfoModel model, Project project) {
        showDialog(model, project, Constants.ADD_COMMENT);
    }

    public static void showDialog(ReviewCommentInfoModel model, Project project, int operateType) {
        JDialog dialog = new JDialog();
        String title = "Add Comment";
        if (operateType == Constants.UPDATE_COMMENT) {
            title = "Update Comment";
        } else if (operateType == Constants.CONFIRM_COMMENT) {
            title = "Confirm Comment";
        }
        dialog.setTitle(title);

        AddReviewCommentUI reviewCommentUI = new AddReviewCommentUI();
        reviewCommentUI.titleLable.setText(title);
        reviewCommentUI.reviewerTextField.setText(model.getReviewer());
        reviewCommentUI.handlerTextField.setText(model.getHandler());
        reviewCommentUI.commentsTextArea.setText(model.getComments());
        reviewCommentUI.codeContentsTextArea.setText(model.getContent());
        reviewCommentUI.filePathTextField.setText(model.getFilePath());
        reviewCommentUI.lineTextField.setText(model.getLineRange());
        reviewCommentUI.questionTypeComboBox.setSelectedItem(model.getType());
        reviewCommentUI.severityComboBox.setSelectedItem(model.getSeverity());
        reviewCommentUI.triggerFactorComboBox.setSelectedItem(model.getFactor());
        reviewCommentUI.projectVersionTextField.setText(model.getProjectVersion());
        reviewCommentUI.belongIssueTextField.setText(model.getBelongIssue());

        reviewCommentUI.confirmResultComboBox.setSelectedItem(model.getConfirmResult());
        reviewCommentUI.confirmNotesTextArea.setText(model.getConfirmNotes());

        reviewCommentUI.saveButton.addActionListener(e -> {
            model.setContent(reviewCommentUI.codeContentsTextArea.getText());
            model.setComments(reviewCommentUI.commentsTextArea.getText());
            model.setReviewer(reviewCommentUI.reviewerTextField.getText());
            model.setHandler(reviewCommentUI.handlerTextField.getText());
            model.setType(reviewCommentUI.questionTypeComboBox.getSelectedItem().toString());
            model.setSeverity(reviewCommentUI.severityComboBox.getSelectedItem().toString());
            model.setFactor(reviewCommentUI.triggerFactorComboBox.getSelectedItem().toString());
            model.setProjectVersion(reviewCommentUI.projectVersionTextField.getText());
            model.setBelongIssue(reviewCommentUI.belongIssueTextField.getText());

            model.setConfirmResult((String) reviewCommentUI.confirmResultComboBox.getSelectedItem());
            model.setConfirmNotes(reviewCommentUI.confirmNotesTextArea.getText());

            System.out.println(model);

            InnerProjectCache projectCache = ProjectInstanceManager.getInstance().getProjectCache(project.getLocationHash());
            projectCache.addNewComment(model);

            CommonUtil.reloadCommentListShow(project);
            dialog.dispose();
        });

        reviewCommentUI.cancelButton.addActionListener(e -> {
            dialog.dispose();
        });



        // 屏幕中心显示
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int w = (screenSize.width - WIDTH) / 2;
        int h = (screenSize.height * 95 / 100 - HEIGHT) / 2;
        dialog.setLocation(w, h);

        dialog.setContentPane(reviewCommentUI.addReviewCommentPanel);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.pack();
        dialog.setVisible(true);

        handleDiffOperateTypeShow(reviewCommentUI, operateType);

    }

    private static void handleDiffOperateTypeShow(AddReviewCommentUI reviewCommentUI, int operateType) {
        if (operateType == Constants.UPDATE_COMMENT) {

        } else if (operateType == Constants.CONFIRM_COMMENT) {
            // 确认场景，不可修改评审意见\检视人员
            reviewCommentUI.reviewerTextField.setEditable(false);
            reviewCommentUI.reviewerTextField.setOpaque(false);
            reviewCommentUI.commentsTextArea.setEditable(false);
            reviewCommentUI.commentsTextArea.setOpaque(false);
        } else if (operateType == Constants.ADD_COMMENT) {
            // 新增场景，不可见确认结果与确认备注
            reviewCommentUI.confirmPanel.setVisible(false);
        }
    }

    private void createUIComponents() {
        // TODO: place custom component creation code here
    }
}
