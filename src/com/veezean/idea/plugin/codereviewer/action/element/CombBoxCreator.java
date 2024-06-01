package com.veezean.idea.plugin.codereviewer.action.element;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import com.veezean.idea.plugin.codereviewer.model.Column;
import com.veezean.idea.plugin.codereviewer.model.ValuePair;

import javax.swing.*;

/**
 * 下拉框界面元素构造器
 *
 * @author Veezean
 * @since 2023/2/4
 */
public class CombBoxCreator implements IElementCreator {

    @Override
    public JComponent create(Project project, Column column, boolean editable) {
        ComboBox<ValuePair> comboBox =
                new ComboBox<>(column.getEnumValues().toArray(new ValuePair[0]));
        comboBox.setEditable(editable);
        return comboBox;
    }
}
