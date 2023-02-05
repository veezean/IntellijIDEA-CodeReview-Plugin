package com.veezean.idea.plugin.codereviewer.action.element;

import com.veezean.idea.plugin.codereviewer.model.Column;

import javax.swing.*;
import java.awt.*;

/**
 * <类功能简要描述>
 *
 * @author Wang Weiren
 * @since 2023/2/4
 */
public class TextAreaCreator implements IElementCreator {

    @Override
    public JComponent create(Column column) {
        return new JTextArea();
    }
}
