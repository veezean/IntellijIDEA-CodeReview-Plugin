package com.veezean.idea.plugin.codereviewer.action;

import cn.hutool.core.util.StrUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.veezean.idea.plugin.codereviewer.action.element.IElementCreator;
import com.veezean.idea.plugin.codereviewer.common.CommitFlag;
import com.veezean.idea.plugin.codereviewer.common.GlobalConfigManager;
import com.veezean.idea.plugin.codereviewer.common.InnerProjectCache;
import com.veezean.idea.plugin.codereviewer.consts.Constants;
import com.veezean.idea.plugin.codereviewer.consts.InputTypeDefine;
import com.veezean.idea.plugin.codereviewer.model.Column;
import com.veezean.idea.plugin.codereviewer.model.RecordColumns;
import com.veezean.idea.plugin.codereviewer.model.ReviewComment;
import com.veezean.idea.plugin.codereviewer.model.ValuePair;
import com.veezean.idea.plugin.codereviewer.service.ProjectLevelService;
import com.veezean.idea.plugin.codereviewer.util.CommonUtil;
import com.veezean.idea.plugin.codereviewer.util.UiPropValueHandler;
import org.apache.commons.lang.StringUtils;

import javax.swing.*;
import java.awt.*;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * 添加评审操作的主界面
 *
 * @author Veezean
 * @since 2019/9/29
 */
public class AddReviewCommentUI {
    private JButton saveButton;
    private JButton cancelButton;
    private JPanel addReviewCommentPanel;
    private JScrollPane mainScrollPanel;
    private JPanel mainComponentPanel;

    private Map<String, Object> uiFields = new ConcurrentHashMap<>();

    public void addPanelToContainer(JDialog dialog) {
        dialog.setContentPane(addReviewCommentPanel);
    }

    public void initComponent(JDialog dialog, ReviewComment model, Project project, int operateType) {
        createPropFields(operateType, project);
        // 将model已有内容记录，填充到界面上显示
        setValueFromModel2UI(model);
        mainScrollPanel.getVerticalScrollBar().setUnitIncrement(20);
        saveButton.addActionListener(e -> {
            StringBuilder validateErrors = propValueValidateErrors(operateType);
            if (validateErrors.length() > 0) {
                Messages.showErrorDialog(validateErrors.substring(1) + " 内容不可以为空，请补齐！", "错误提示");
                return;
            }

            // 将界面内容塞回存储对象中
            setValueFromUI2Model(model);

            // 自动补齐相关字段值
            long currentTimeMillis = System.currentTimeMillis();
            boolean networkMode = GlobalConfigManager.getInstance().getGlobalConfig().isNetworkMode();
            if (Constants.DETAIL_COMMENT == operateType) {
                String confirmResult = model.getConfirmResult();
                // 如果有具体确认结果，则自动记录对应的确认时间与确认人员
                if (!Constants.UNCONFIRMED.equals(confirmResult)) {
                    model.setConfirmDate(CommonUtil.time2String(currentTimeMillis));
                    if (networkMode) {
                        model.setRealConfirmer(GlobalConfigManager.getInstance().getGlobalConfig().getCurrentUserInfo());
                    }
                } else {
                    // 清除掉确认时间与确认人员信息
                    model.setConfirmDate("");
                    model.setRealConfirmer(null);
                }
            } else if (Constants.ADD_COMMENT == operateType) {
                if (networkMode) {
                    model.setReviewer(GlobalConfigManager.getInstance().getGlobalConfig().getCurrentUserInfo());
                }
                model.setCommitDate(CommonUtil.time2String(currentTimeMillis));
                model.setConfirmResult(ValuePair.buildPair("unconfirmed", "未确认"));
            }

            // 添加标记，标识为本地已经做出修改
            model.setCommitFlag(CommitFlag.UNCOMMITED);

            InnerProjectCache projectCache = ProjectLevelService.getService(project).getProjectCache();
            projectCache.addNewComment(model);
            CommonUtil.reloadCommentListShow(project);
            dialog.dispose();
        });

        cancelButton.addActionListener(e -> {
            dialog.dispose();
        });
    }

    private StringBuilder propValueValidateErrors(int operateType) {
        // 必填项校验（显示、可编辑、必填 三者同时满足条件才行）
        List<Column> requiredProps = GlobalConfigManager.getInstance().getCustomConfigColumns().getColumns()
                .stream()
                .filter(Column::isRequired)
                .filter(column -> column.isEditableInAddPage() || column.isEditableInEditPage() || column.isEditableInConfirmPage())
                .filter(column -> {
                    if (Constants.ADD_COMMENT == operateType) {
                        return column.isShowInAddPage();
                    } else if (Constants.DETAIL_COMMENT == operateType) {
                        // 如果是详情窗口，不管是什么场景允许显示的字段，都要统一显示出来
                        return column.isShowInConfirmPage() || column.isShowInAddPage() || column.isShowInEditPage() || column.isShowInIdeaTable();
                    } else {
                        return false;
                    }
                })
                .collect(Collectors.toList());
        StringBuilder builder = new StringBuilder();
        for (Column column : requiredProps) {
            boolean containsKey = uiFields.containsKey(column.getColumnCode());
            if (!containsKey) {
                continue;
            }
            Object propValue = UiPropValueHandler.getUiPropValue(uiFields.get(column.getColumnCode()));
            if (StrUtil.isEmptyIfStr(propValue)) {
                builder.append(",").append(column.getShowName());
            }
        }
        return builder;
    }

    private void setValueFromUI2Model(ReviewComment model) {
        uiFields.forEach((key, field) -> {
            Object propValue = UiPropValueHandler.getUiPropValue(field);
            if (propValue instanceof ValuePair) {
                model.setPairPropValue(key, (ValuePair) propValue);
            } else {
                model.setStringPropValue(key, (String) propValue);
            }
        });
    }

    private void setValueFromModel2UI(ReviewComment model) {
        uiFields.forEach((key, field) -> {
            UiPropValueHandler.setUiPropValue(model, key, field);
        });
    }

    private void createPropFields(int operateType, Project project) {
        RecordColumns columns = GlobalConfigManager.getInstance().getCustomConfigColumns();
        List<Column> extendParams = columns.getColumns().stream()
                .filter(column -> {
                    if (Constants.ADD_COMMENT == operateType) {
                        return column.isShowInAddPage();
                    } else if (Constants.DETAIL_COMMENT == operateType) {
                        return column.isShowInConfirmPage() || column.isShowInAddPage() || column.isShowInEditPage() || column.isShowInIdeaTable();
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

        GridBagLayout gridBagLayout = new GridBagLayout();
        gridBagLayout.columnWidths = new int[]{0, 0};  //设置了总共有2列
        gridBagLayout.rowHeights = new int[]{extendParams.size()};  //设置总行
        gridBagLayout.columnWeights = new double[]{0.3, 0.7};  //设置了列的宽度为容器宽度
        gridBagLayout.rowWeights = new double[]{1.0}; //设置了行的高度为容器高度

        mainComponentPanel.setLayout(gridBagLayout);

        AtomicInteger row = new AtomicInteger(0);
        // 初始化元素
        extendParams.forEach(column -> {
            boolean editable = false;
            if (Constants.ADD_COMMENT == operateType) {
                editable = column.isEditableInAddPage();
            } else if (Constants.DETAIL_COMMENT == operateType) {
                editable = column.isEditableInConfirmPage();
            } else {
                editable = column.isEditableInEditPage();
            }

            JLabel jLabel = new JLabel(column.getShowName());
            IElementCreator elementCreator = InputTypeDefine.getElementCreator(column);
            JComponent component = elementCreator.create(project, column, editable);

            GridBagConstraints gbc_label = new GridBagConstraints();
            gbc_label.fill = GridBagConstraints.HORIZONTAL;
            gbc_label.anchor = GridBagConstraints.WEST;
            gbc_label.insets = new Insets(2, 10, 2, 10);
            gbc_label.gridx = 0;
            gbc_label.gridy = row.get();
            gbc_label.weighty = 0;

            GridBagConstraints gbc_component = new GridBagConstraints();
            if (InputTypeDefine.isComboBox(column.getInputType())) {
                gbc_component.fill = GridBagConstraints.NONE;
            } else {
                gbc_component.fill = GridBagConstraints.HORIZONTAL;
            }
            gbc_component.anchor = GridBagConstraints.WEST;
            gbc_component.insets = new Insets(2, 10, 2, 10);
            gbc_component.gridx = 1;
            gbc_component.gridy = row.get();
            gbc_component.weighty = 0;

            mainComponentPanel.add(jLabel, gbc_label);
            mainComponentPanel.add(component, gbc_component);

            row.incrementAndGet();

            // 添加元素的缓存映射
            uiFields.put(column.getColumnCode(), component);
        });
    }
}
