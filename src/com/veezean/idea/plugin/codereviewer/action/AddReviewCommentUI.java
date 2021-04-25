package com.veezean.idea.plugin.codereviewer.action;

import com.intellij.openapi.project.Project;
import com.veezean.idea.plugin.codereviewer.common.CommonUtil;
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

    private static final int WIDTH = 600;
    private static final int HEIGHT = 600;

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
    private JLabel 添加评审意见;

    public static void showDialog(ReviewCommentInfoModel model, Project project) {
        JDialog dialog = new JDialog();
        dialog.setTitle("添加评审意见");
        AddReviewCommentUI reviewCommentUI = new AddReviewCommentUI();
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
    }

    private void createUIComponents() {
        // TODO: place custom component creation code here
    }
}
