package com.veezean.idea.plugin.codereviewer.action.element;

import com.intellij.openapi.project.Project;
import com.veezean.idea.plugin.codereviewer.model.Column;

import javax.swing.*;

/**
 * 单行文本界面元素构造器
 *
 * @author Veezean
 * @since 2023/2/4
 */
public class TextFieldCreator implements IElementCreator {

    @Override
    public JComponent create(Project project, Column column, boolean editable) {
        JTextField textField = new JTextField();
        textField.setEditable(editable);
        return textField;
    }
}
