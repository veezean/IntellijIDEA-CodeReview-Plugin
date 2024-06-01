package com.veezean.idea.plugin.codereviewer.util;

import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.fileTypes.LanguageFileType;
import com.intellij.ui.EditorTextField;
import com.veezean.idea.plugin.codereviewer.common.CodeReviewException;
import com.veezean.idea.plugin.codereviewer.common.LanguageFileTypeFactory;
import com.veezean.idea.plugin.codereviewer.model.ReviewComment;

import javax.swing.*;

/**
 * 界面上UI字段设置工具类
 *
 * @author Veezean
 * @since 2022/5/22
 */
public class UiPropValueHandler {

    public static Object getUiPropValue(Object field) {
        if (field instanceof JTextField) {
            return ((JTextField) field).getText();
        } else if (field instanceof JTextArea) {
            return ((JTextArea) field).getText();
        } else if (field instanceof EditorTextField) {
            return ((EditorTextField) field).getText();
        } else if (field instanceof JComboBox) {
            return ((JComboBox) field).getSelectedItem();
        } else {
            throw new CodeReviewException("不支持的界面字段类型，请检查代码");
        }
    }

    public static void setUiPropValue(ReviewComment comment, String propKey, Object field) {
        if (field instanceof JTextField) {
            ((JTextField) field).setText(comment.getStringPropValue(propKey));
        } else if (field instanceof JTextArea) {
            ((JTextArea) field).setText(comment.getStringPropValue(propKey));
        } else if (field instanceof EditorTextField) {
            Document document = EditorFactory.getInstance().createDocument(comment.getStringPropValue(propKey));
            ((EditorTextField) field).setDocument(document);
            LanguageFileType languageFileType = LanguageFileTypeFactory.getLanguageFileType(comment.fileSuffix());
            ((EditorTextField) field).setFileType(languageFileType);
        } else if (field instanceof JComboBox) {
            ((JComboBox) field).setSelectedItem(comment.getPairPropValue(propKey));
        } else {
            throw new CodeReviewException("不支持的界面字段类型设置，请检查代码");
        }
    }

}
