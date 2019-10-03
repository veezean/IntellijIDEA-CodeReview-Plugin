package com.veezean.idea.plugin.codereviewer.action;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import com.veezean.idea.plugin.codereviewer.common.GlobalCacheManager;
import com.veezean.idea.plugin.codereviewer.model.ReviewCommentInfoModel;

import javax.swing.*;

/**
 * <类功能简要描述>
 *
 * @author Wang Weiren
 * @since 2019/9/29
 */
public class AddReviewCommentUI {

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

    public static void showDialog(ReviewCommentInfoModel model, Project project) {
        JDialog dialog = new JDialog();
        dialog.setTitle("添加评审意见");
        AddReviewCommentUI reviewCommentUI = new AddReviewCommentUI();
        reviewCommentUI.reviewerTextField.setText(model.getReviewer());
        reviewCommentUI.commentsTextArea.setText(model.getComments());
        reviewCommentUI.codeContentsTextArea.setText(model.getContent());
        reviewCommentUI.filePathTextField.setText(model.getFilePath());
        reviewCommentUI.lineTextField.setText(model.getLineRange());
        reviewCommentUI.questionTypeComboBox.setSelectedItem(model.getType());
        reviewCommentUI.severityComboBox.setSelectedItem(model.getSeverity());
        reviewCommentUI.triggerFactorComboBox.setSelectedItem(model.getFactor());
        reviewCommentUI.saveButton.addActionListener(e -> {
            //TODO 记录内容
            model.setContent(reviewCommentUI.codeContentsTextArea.getText());
            model.setComments(reviewCommentUI.commentsTextArea.getText());
            model.setReviewer(reviewCommentUI.reviewerTextField.getText());
            model.setType(reviewCommentUI.questionTypeComboBox.getSelectedItem().toString());
            model.setSeverity(reviewCommentUI.severityComboBox.getSelectedItem().toString());
            model.setFactor(reviewCommentUI.triggerFactorComboBox.getSelectedItem().toString());

            System.out.println(model);

            GlobalCacheManager.getInstance().addNewComment(model);

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

            dialog.dispose();
        });

        reviewCommentUI.cancelButton.addActionListener(e -> {
            dialog.dispose();
        });

        dialog.setContentPane(reviewCommentUI.addReviewCommentPanel);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.pack();
        dialog.setVisible(true);
    }
}
