package com.veezean.idea.plugin.codereviewer.action;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;

/**
 * 评审操作render逻辑处理类
 *
 * @author Veezean, 公众号 @架构悟道
 * @since 2019/10/1
 */
public class CommentTableCellRender extends DefaultTableCellRenderer {
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        boolean cellEditable = table.isCellEditable(row, column);
        if (cellEditable) {
            this.setBackground(Color.ORANGE);
            this.setForeground(Color.BLUE);
        } else {
            this.setBackground(Color.white);
            this.setForeground(Color.BLACK);
        }
        return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
    }
}
