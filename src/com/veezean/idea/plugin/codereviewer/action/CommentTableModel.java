package com.veezean.idea.plugin.codereviewer.action;

import com.veezean.idea.plugin.codereviewer.model.Column;
import com.veezean.idea.plugin.codereviewer.model.RecordColumns;

import javax.swing.table.DefaultTableModel;

/**
 * 评审信息列表实例对象
 *
 * @author Wang Weiren
 * @since 2019/9/30
 */
public class CommentTableModel extends DefaultTableModel {

    private RecordColumns recordColumns;

    public CommentTableModel(Object[][] data, RecordColumns recordColumns) {
        super(data, recordColumns.getTableHeaderColumns());
        this.recordColumns = recordColumns;
    }

    @Override
    public boolean isCellEditable(int row, int col) {
        Column column = recordColumns.getColumnByIndex(col);
        return column != null && column.isEditable();
    }


}
