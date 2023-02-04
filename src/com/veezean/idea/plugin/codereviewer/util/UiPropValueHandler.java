package com.veezean.idea.plugin.codereviewer.util;

import cn.hutool.core.util.ReflectUtil;
import com.veezean.idea.plugin.codereviewer.common.CodeReviewException;
import com.veezean.idea.plugin.codereviewer.common.GlobalConfigManager;
import com.veezean.idea.plugin.codereviewer.model.Column;
import com.veezean.idea.plugin.codereviewer.model.ReviewComment;
import org.apache.commons.lang.StringUtils;

import javax.swing.*;

/**
 * 界面上UI字段设置工具类
 *
 * @author Wang Weiren
 * @since 2022/5/22
 */
public class UiPropValueHandler {

    public static String getUiPropValue(Column column, Object instance) {
        String editUiName = column.getEditUiName();
        if (StringUtils.isEmpty(editUiName)) {
            return "";
        }
        Object fieldValue = ReflectUtil.getFieldValue(instance, editUiName);
        String value;
        if (fieldValue instanceof JTextField) {
            value = ((JTextField) fieldValue).getText();
        } else if (fieldValue instanceof JTextArea) {
            value = ((JTextArea) fieldValue).getText();
        } else if (fieldValue instanceof JComboBox) {
            value = (String) ((JComboBox) fieldValue).getSelectedItem();
        } else {
            throw new CodeReviewException("不支持的界面字段类型，请检查代码");
        }

        return value;
    }

    public static void setUiPropValue(ReviewComment comment, Column column, Object instance) {
        String editUiName = column.getEditUiName();
        if (StringUtils.isEmpty(editUiName)) {
            return;
        }

        String propValue = comment.getPropValue(column.getColumnCode());

        Object fieldValue = ReflectUtil.getFieldValue(instance, editUiName);
        if (fieldValue instanceof JTextField) {
            ((JTextField) fieldValue).setText(propValue);
        } else if (fieldValue instanceof JTextArea) {
            ((JTextArea) fieldValue).setText(propValue);
        } else if (fieldValue instanceof JComboBox) {
            ((JComboBox) fieldValue).setSelectedItem(propValue);
        } else {
            throw new CodeReviewException("不支持的界面字段类型设置，请检查代码");
        }
    }

    public static void setUiPropEditable(Column column, Object instance, boolean editable) {
        String editUiName = column.getEditUiName();
        if (StringUtils.isEmpty(editUiName)) {
            return;
        }

        Object fieldValue = ReflectUtil.getFieldValue(instance, editUiName);
        if (fieldValue instanceof JTextField) {
            ((JTextField) fieldValue).setEditable(editable);
            ((JTextField) fieldValue).setOpaque(editable);
        } else if (fieldValue instanceof JTextArea) {
            ((JTextArea) fieldValue).setEditable(editable);
            ((JTextArea) fieldValue).setOpaque(editable);
        } else if (fieldValue instanceof JComboBox) {
            // 曲线救国的方式，实现禁用下拉框修改
            Object selectedItem = ((JComboBox) fieldValue).getSelectedItem();
            ((JComboBox) fieldValue).removeAllItems();
            ((JComboBox) fieldValue).addItem(selectedItem);
        } else {
            throw new CodeReviewException("不支持的界面字段类型，请检查代码");
        }
    }


    public static void setUiPropVisable(Column column, Object instance, boolean visable) {
        String editUiName = column.getEditUiName();
        if (StringUtils.isEmpty(editUiName)) {
            return;
        }

        Object fieldValue = ReflectUtil.getFieldValue(instance, editUiName);
        if (fieldValue instanceof JTextField) {
            ((JTextField) fieldValue).setVisible(visable);
        } else if (fieldValue instanceof JTextArea) {
            ((JTextArea) fieldValue).setVisible(visable);
        } else if (fieldValue instanceof JComboBox) {
            ((JComboBox) fieldValue).setVisible(visable);
        } else {
            throw new CodeReviewException("不支持的界面字段类型，请检查代码");
        }
    }

    public static void setUiComboBoxLists(Column column, Object instance) {
        String editUiName = column.getEditUiName();
        if (StringUtils.isEmpty(editUiName)) {
            return;
        }

        Object fieldValue = ReflectUtil.getFieldValue(instance, editUiName);
       if (fieldValue instanceof JComboBox) {
           JComboBox comboBox = ((JComboBox) fieldValue);
           GlobalConfigManager.getInstance().getColumnValueEnums()
                   .getValuesByCode(column.getColumnCode())
                   .forEach(comboBox::addItem);
        } else {
            throw new CodeReviewException("对象不是JComboBox类型");
        }
    }
}
