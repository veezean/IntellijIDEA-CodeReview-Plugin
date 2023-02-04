package com.veezean.idea.plugin.codereviewer.action;

import com.intellij.openapi.project.Project;
import com.veezean.idea.plugin.codereviewer.common.*;
import com.veezean.idea.plugin.codereviewer.consts.InputTypeDefine;
import com.veezean.idea.plugin.codereviewer.model.Column;
import com.veezean.idea.plugin.codereviewer.model.ColumnValueEnums;
import com.veezean.idea.plugin.codereviewer.model.ReviewComment;
import com.veezean.idea.plugin.codereviewer.util.RecordColumnBuildFactory;
import com.veezean.idea.plugin.codereviewer.util.UiPropValueHandler;
import org.apache.commons.lang.StringUtils;

import javax.swing.*;
import java.awt.*;
import java.lang.reflect.Field;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 添加评审操作的主界面
 *
 * @author Wang Weiren
 * @since 2019/9/29
 */
public class AddReviewCommentUI {

    private static final int WIDTH = 700;
    private static final int HEIGHT = 800;

    // 下面字段虽然代码里面没有使用到，但是不能删，因为UI里面有使用，配置json里面有使用
    private JTextField reviewerTextField;
    private JTextArea commentsTextArea;
    private JComboBox questionTypeComboBox;
    private JComboBox severityComboBox;
    private JComboBox triggerFactorComboBox;
    private JTextField filePathTextField;
    private JTextArea codeContentsTextArea;
    private JTextField lineTextField;
    private JTextField handlerTextField;
    private JTextField projectVersionTextField;
    private JTextField belongIssueTextField;
    private JComboBox confirmResultComboBox;
    private JTextArea confirmNotesTextArea;

    private JPanel confirmPanel;
    private JButton saveButton;
    private JButton cancelButton;
    private JPanel addReviewCommentPanel;
    private JLabel titleLable;

    public static void showDialog(ReviewComment model, Project project) {
        showDialog(model, project, Constants.ADD_COMMENT);
    }

    public static void showDialog(ReviewComment model, Project project, int operateType) {
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

        // 初始化界面UI中的下拉框控件候选值
        initCombobox(reviewCommentUI);

        // 将model已有内容记录，填充到界面上显示
        setValueFromModel2UI(model, reviewCommentUI);

        reviewCommentUI.saveButton.addActionListener(e -> {

            // 将界面内容塞回存储对象中
            setValueFromUI2Model(model, reviewCommentUI);
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

    private static void initCombobox(AddReviewCommentUI uiInstance) {
        GlobalConfigManager.getInstance().getSystemDefaultRecordColumns().getColumns()
                .stream()
                .filter(column -> InputTypeDefine.COMBO_BOX.getValue().equalsIgnoreCase(column.getInputType()))
                .filter(column -> StringUtils.isNotEmpty(column.getEditUiName()))
                .forEach(column -> UiPropValueHandler.setUiComboBoxLists(column, uiInstance));
    }

    private static void handleDiffOperateTypeShow(AddReviewCommentUI reviewCommentUI, int operateType) {
        if (operateType == Constants.UPDATE_COMMENT) {

        } else if (operateType == Constants.CONFIRM_COMMENT) {
            // 确认场景，部分字段设置不可编辑
            GlobalConfigManager.getInstance().getSystemDefaultRecordColumns().getColumns().stream()
                    .filter(column -> !column.isEditableInConfirmPage())
                    .forEach(column -> UiPropValueHandler.setUiPropEditable(column, reviewCommentUI, false));
        } else if (operateType == Constants.ADD_COMMENT) {
            // 新增场景，不可见确认结果与确认备注
            GlobalConfigManager.getInstance().getSystemDefaultRecordColumns().getColumns().stream()
                    .filter(column -> !column.isShowInAddPage())
                    .forEach(column -> UiPropValueHandler.setUiPropVisable(column, reviewCommentUI, false));

            reviewCommentUI.confirmPanel.setVisible(false);
        }
    }

    private static void setValueFromUI2Model(ReviewComment model, Object UiInstance) {
        List<Column> columns = RecordColumnBuildFactory.loadColumnDefines().getColumns();
        for (Column col : columns) {
            if (StringUtils.isEmpty(col.getEditUiName())) {
                continue;
            }
            String propValue = UiPropValueHandler.getUiPropValue(col, UiInstance);
            model.setPropValue(col.getColumnCode(), propValue);
        }
    }

    private static void setValueFromModel2UI(ReviewComment model, Object UiInstance) {
        List<Column> columns = RecordColumnBuildFactory.loadColumnDefines().getColumns();
        for (Column col : columns) {
            if (StringUtils.isEmpty(col.getEditUiName())) {
                continue;
            }
            UiPropValueHandler.setUiPropValue(model, col, UiInstance);
        }
    }
}
