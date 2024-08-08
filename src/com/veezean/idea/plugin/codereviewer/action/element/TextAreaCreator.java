package com.veezean.idea.plugin.codereviewer.action.element;

import com.intellij.openapi.project.Project;
import com.veezean.idea.plugin.codereviewer.model.Column;

import javax.swing.*;
import java.awt.*;

/**
 * 多行文本界面元素构造器
 *
 * @author Veezean
 * @since 2023/2/4
 */
public class TextAreaCreator implements IElementCreator {

    @Override
    public JComponent create(Project project, Column column, boolean editable) {
        JTextArea jTextArea = new JTextArea();
        jTextArea.setLineWrap(true);
        jTextArea.setAutoscrolls(true);
        jTextArea.setRows(10);
        jTextArea.setEditable(editable);
        return jTextArea;
    }
}
