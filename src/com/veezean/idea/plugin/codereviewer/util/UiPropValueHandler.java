package com.veezean.idea.plugin.codereviewer.util;

import com.veezean.idea.plugin.codereviewer.common.CodeReviewException;
import com.veezean.idea.plugin.codereviewer.model.ReviewComment;

import javax.swing.*;

/**
 * 界面上UI字段设置工具类
 *
 * @author Veezean, 公众号 @架构悟道
 * @since 2022/5/22
 */
public class UiPropValueHandler {

    public static String getUiPropValue(Object field) {
        String value;
        if (field instanceof JTextField) {
            value = ((JTextField) field).getText();
        } else if (field instanceof JTextArea) {
            value = ((JTextArea) field).getText();
        } else if (field instanceof JComboBox) {
            value = (String) ((JComboBox) field).getSelectedItem();
        } else {
            throw new CodeReviewException("不支持的界面字段类型，请检查代码");
        }

        return value;
    }

    public static void setUiPropValue(ReviewComment comment, String propKey, Object field) {
        String propValue = comment.getPropValue(propKey);
        if (field instanceof JTextField) {
            ((JTextField) field).setText(propValue);
        } else if (field instanceof JTextArea) {
            ((JTextArea) field).setText(propValue);
        } else if (field instanceof JComboBox) {
            ((JComboBox) field).setSelectedItem(propValue);
        } else {
            throw new CodeReviewException("不支持的界面字段类型设置，请检查代码");
        }
    }

    public static void setUiPropEditable(Object field, boolean editable) {
        if (field instanceof JTextField) {
            ((JTextField) field).setEditable(editable);
            ((JTextField) field).setOpaque(editable);
        } else if (field instanceof JTextArea) {
            ((JTextArea) field).setEditable(editable);
            ((JTextArea) field).setOpaque(editable);
        } else if (field instanceof JComboBox) {
            // 曲线救国的方式，实现禁用下拉框修改
            Object selectedItem = ((JComboBox) field).getSelectedItem();
            ((JComboBox) field).removeAllItems();
            ((JComboBox) field).addItem(selectedItem);
        } else {
            throw new CodeReviewException("不支持的界面字段类型，请检查代码");
        }
    }

}
