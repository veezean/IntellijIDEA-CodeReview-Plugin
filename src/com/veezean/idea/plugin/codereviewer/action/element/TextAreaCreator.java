package com.veezean.idea.plugin.codereviewer.action.element;

import com.veezean.idea.plugin.codereviewer.model.Column;

import javax.swing.*;
import java.awt.*;

/**
 * 多行文本界面元素构造器
 *
 * @author Veezean, 公众号 @架构悟道
 * @since 2023/2/4
 */
public class TextAreaCreator implements IElementCreator {

    @Override
    public JComponent create(Column column) {
        JTextArea jTextArea = new JTextArea();
        jTextArea.setLineWrap(true);
        jTextArea.setAutoscrolls(true);
        return jTextArea;
    }
}
