package com.veezean.idea.plugin.codereviewer.action.element;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import com.veezean.idea.plugin.codereviewer.model.Column;
import com.veezean.idea.plugin.codereviewer.model.ValuePair;

import javax.swing.*;

/**
 * 日期时间选择组件构造器
 *
 * @author Veezean
 * @since 2023/2/4
 */
public class DateSelectCreator implements IElementCreator {

    @Override
    public JComponent create(Project project, Column column, boolean editable) {
        JTextField jTextField = new JTextField("单击选择日期");
        new DatePicker().register(jTextField);
        return jTextField;
    }
}
