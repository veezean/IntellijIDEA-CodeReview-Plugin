package com.veezean.idea.plugin.codereviewer.action;

import javax.swing.table.DefaultTableModel;

/**
 * 评审信息列表实例对象
 *
 * @author Wang Weiren
 * @since 2019/9/30
 */
public class CommentTableModel extends DefaultTableModel {

    public CommentTableModel(Object[][] data, Object[] columnNames) {
        super(data, columnNames);
    }

    @Override
    public boolean isCellEditable(int row, int column) {
        // 只有部分字段允许修改
        if ((column > 0 && column < 6) || (column > 9 && column < 15)) {
            return true;
        }
        return false;
    }


}
