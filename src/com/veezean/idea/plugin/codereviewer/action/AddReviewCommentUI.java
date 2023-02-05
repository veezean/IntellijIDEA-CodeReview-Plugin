package com.veezean.idea.plugin.codereviewer.action;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.veezean.idea.plugin.codereviewer.action.element.IElementCreator;
import com.veezean.idea.plugin.codereviewer.common.*;
import com.veezean.idea.plugin.codereviewer.consts.InputTypeDefine;
import com.veezean.idea.plugin.codereviewer.model.Column;
import com.veezean.idea.plugin.codereviewer.model.RecordColumns;
import com.veezean.idea.plugin.codereviewer.model.ReviewComment;
import com.veezean.idea.plugin.codereviewer.util.Logger;
import com.veezean.idea.plugin.codereviewer.util.UiPropValueHandler;
import org.apache.commons.lang.StringUtils;

import javax.swing.*;
import java.awt.*;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 添加评审操作的主界面
 *
 * @author Wang Weiren
 * @since 2019/9/29
 */
public class AddReviewCommentUI {

    // 下面字段虽然代码里面没有使用到，但是不能删，因为UI里面有使用，配置json里面有使用
    private JTextField reviewer;
    private JTextArea comment;
    private JTextField filePath;
    private JTextArea content;
    private JTextField lineRange;

    private JPanel confirmPanel;
    private JButton saveButton;
    private JButton cancelButton;
    private JPanel addReviewCommentPanel;
    private JLabel titleLable;
    private JPanel detailParamsPanel;

    private Map<String, Object> uiFields = new ConcurrentHashMap<>();

    public void addPanelToContainer(JDialog dialog) {
        dialog.setContentPane(addReviewCommentPanel);
    }

    public void initComponent(JDialog dialog, ReviewComment model, Project project, int operateType) {
        addSystemPropertiesToMap();
        createPropFields(operateType);

        titleLable.setText(dialog.getTitle());

        // 将model已有内容记录，填充到界面上显示
        setValueFromModel2UI(model, operateType);

        saveButton.addActionListener(e -> {
            StringBuilder validateErrors = propValueValidateErrors();
            if (validateErrors.length() > 0) {
                Messages.showErrorDialog(validateErrors.substring(1) + " 内容不可以为空，请补齐！", "错误提示");
                return;
            }

            // 将界面内容塞回存储对象中
            setValueFromUI2Model(model);
            InnerProjectCache projectCache =
                    ProjectInstanceManager.getInstance().getProjectCache(project.getLocationHash());
            projectCache.addNewComment(model);
            CommonUtil.reloadCommentListShow(project);
            dialog.dispose();
        });

        cancelButton.addActionListener(e -> {
            dialog.dispose();
        });
    }

    private StringBuilder propValueValidateErrors() {
        // 必填项校验
        List<Column> requiredProps = GlobalConfigManager.getInstance().getSystemDefaultRecordColumns().getColumns()
                .stream()
                .filter(column -> column.isRequired())
                .collect(Collectors.toList());
        StringBuilder builder = new StringBuilder();
        for (Column column : requiredProps) {
            boolean containsKey = uiFields.containsKey(column.getColumnCode());
            if (!containsKey) {
                continue;
            }
            String propValue = UiPropValueHandler.getUiPropValue(uiFields.get(column.getColumnCode()));
            if (StringUtils.isEmpty(propValue)) {
                builder.append(",").append(column.getShowName());
            }
        }
        return builder;
    }
    private void addSystemPropertiesToMap() {
        uiFields.put("reviewer", reviewer);
        uiFields.put("filePath", filePath);
        uiFields.put("content", content);
        uiFields.put("lineRange", lineRange);
        uiFields.put("comment", comment);
    }

    private void setValueFromUI2Model(ReviewComment model) {
        uiFields.forEach((key, field) -> {
            String propValue = UiPropValueHandler.getUiPropValue(field);
            model.setPropValue(key, propValue);
        });
    }

    private void setValueFromModel2UI(ReviewComment model, int operateType) {
        uiFields.forEach((key, field) -> {
            UiPropValueHandler.setUiPropValue(model, key, field);
        });
    }

    private void createPropFields(int operateType) {
        // ---------------------通用属性字段-----------------------
        RecordColumns columns = GlobalConfigManager.getInstance().getSystemDefaultRecordColumns();
        List<Column> extendParams = columns.getColumns().stream()
                .filter(column -> !column.isSystemInitialization())
                .filter(column -> !column.isConfirmProp())
                .filter(column -> {
                    if (Constants.ADD_COMMENT == operateType) {
                        return column.isShowInAddPage();
                    } else if (Constants.CONFIRM_COMMENT == operateType) {
                        return column.isShowInComfirmPage();
                    } else {
                        return false;
                    }
                })
                .sorted(Comparator.comparingInt(Column::getSortIndex))
                .collect(Collectors.toList());
        int size = extendParams.size();
        if (size == 0) {
            return;
        }

        // 每行2个字段，计算出一共需要多少行
        detailParamsPanel.setLayout(new GridLayout((int) Math.ceil(((double) size) / 2d), 4));
        detailParamsPanel.setBorder(BorderFactory.createTitledBorder("详细属性"));

        // 初始化元素
        extendParams.forEach(column -> {
            JLabel jLabel = new JLabel(column.getShowName());
            IElementCreator elementCreator = InputTypeDefine.getElementCreator(column.getInputType());
            JComponent component = elementCreator.create(column);

            // 添加到panel中
            detailParamsPanel.add(jLabel);
            detailParamsPanel.add(component);

            // 添加元素的缓存映射
            uiFields.put(column.getColumnCode(), component);
        });

        // -------------------确认界面额外属性-----------------------
        if (operateType != Constants.CONFIRM_COMMENT) {
            confirmPanel.setVisible(false);
        } else {
            List<Column> confirmParams = columns.getColumns().stream()
                    .filter(column -> !column.isSystemInitialization())
                    .filter(Column::isConfirmProp)
                    .filter(Column::isShowInComfirmPage)
                    .sorted(Comparator.comparingInt(Column::getSortIndex))
                    .collect(Collectors.toList());
            int confirmPropSize = confirmParams.size();
            if (confirmPropSize == 0) {
                return;
            }
            // 每行2个字段，计算出一共需要多少行
            confirmPanel.setLayout(new GridLayout((int) Math.ceil(((double) confirmPropSize) / 2d), 4));
            confirmPanel.setBorder(BorderFactory.createTitledBorder("意见确认"));
            // 初始化元素
            confirmParams.forEach(column -> {
                JLabel jLabel = new JLabel(column.getShowName());
                IElementCreator elementCreator = InputTypeDefine.getElementCreator(column.getInputType());
                JComponent component = elementCreator.create(column);

                // 添加到panel中
                confirmPanel.add(jLabel);
                confirmPanel.add(component);

                // 添加元素的缓存映射
                uiFields.put(column.getColumnCode(), component);
            });

            // 确认界面 字段可编辑 设置
            GlobalConfigManager.getInstance().getSystemDefaultRecordColumns().getColumns().stream()
                    .filter(column -> !column.isEditableInConfirmPage())
                    .filter(column -> uiFields.containsKey(column.getColumnCode()))
                    .forEach(column -> {
                        Logger.info("字段：" + column.getColumnCode() + ", 确认界面是否可编辑：" + column.isEditableInConfirmPage());
                        UiPropValueHandler.setUiPropEditable(uiFields.get(column.getColumnCode()),
                                false);
                    });
        }
    }
}
