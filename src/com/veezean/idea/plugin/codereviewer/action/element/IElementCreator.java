package com.veezean.idea.plugin.codereviewer.action.element;

import com.veezean.idea.plugin.codereviewer.model.Column;

import javax.swing.*;

/**
 * 界面元素构造器
 *
 * @author Veezean
 * @since 2023/2/4
 */
public interface IElementCreator {
    JComponent create(Column column, boolean editable);
}
