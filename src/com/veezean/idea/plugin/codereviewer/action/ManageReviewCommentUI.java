package com.veezean.idea.plugin.codereviewer.action;

import com.intellij.openapi.editor.CaretModel;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.LogicalPosition;
import com.intellij.openapi.editor.SelectionModel;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.search.PsiShortNamesCache;
import com.intellij.util.Icons;
import com.veezean.idea.plugin.codereviewer.common.*;
import com.veezean.idea.plugin.codereviewer.model.ReviewCommentInfoModel;

import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.TableModel;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * 管理评审内容的主界面
 *
 * @author Wang Weiren
 * @since 2019/9/29
 */
public class ManageReviewCommentUI {
    private static final Object[] COLUMN_NAMES = {"ID", "检视人员", "检视意见", "意见类型",
            "严重级别", "问题归属", "文件路径", "行号范围", "内容摘录", "提交时间", "项目版本", "相关需求", "待处理人"};
    private JButton clearButton;
    private JButton deleteButton;
    private JButton exportButton;
    private JButton importButton;
    private JTable commentTable;
    public JPanel fullPanel;
    private final Project project;

    public ManageReviewCommentUI(Project project) {
        this.project = project;
    }


    public void initUI() {
        bindButtons();
        reloadTableData();
        bindTableListeners();
    }

    public void refreshTableDataShow() {
        reloadTableData();
    }

    private void reloadTableData() {
        InnerProjectCache projectCache = ProjectInstanceManager.getInstance().getProjectCache(ManageReviewCommentUI.this.project.getLocationHash());
        List<ReviewCommentInfoModel> cachedComments = projectCache.getCachedComments();
        List<Object[]> rowDataList = new ArrayList<>();
        for (ReviewCommentInfoModel model : cachedComments) {
            Object[] row = {model.getIdentifier(), model.getReviewer(), model.getComments(), model.getType(),
                    model.getSeverity(), model.getFactor(), model.getFilePath(), model.getLineRange(), model.getContent(),
                    model.getDateTime(), model.getProjectVersion(), model.getBelongIssue(), model.getHandler()
            };
            rowDataList.add(row);
        }
        Object[][] rowData = rowDataList.stream().toArray(Object[][]::new);
        TableModel dataModel = new CommentTableModel(rowData, COLUMN_NAMES);
        commentTable.setModel(dataModel);
        commentTable.setEnabled(true);

        // 设置指定列只能通过下拉框选择数据
        JComboBox<String> typeComboBox = new ComboBox<>();
        typeComboBox.addItem("问题");
        typeComboBox.addItem("建议");
        typeComboBox.addItem("疑问");
        commentTable.getColumnModel().getColumn(3).setCellEditor(new DefaultCellEditor(typeComboBox));

        JComboBox<String> severityComboBox = new ComboBox<>();
        severityComboBox.addItem("提示");
        severityComboBox.addItem("一般");
        severityComboBox.addItem("严重");
        commentTable.getColumnModel().getColumn(4).setCellEditor(new DefaultCellEditor(severityComboBox));

        JComboBox<String> factorComboBox = new ComboBox<>();
        factorComboBox.addItem("编码基础类");
        factorComboBox.addItem("业务功能类");
        factorComboBox.addItem("安全可靠类");
        factorComboBox.addItem("其它");
        commentTable.getColumnModel().getColumn(5).setCellEditor(new DefaultCellEditor(factorComboBox));


        commentTable.getModel().addTableModelListener(new TableModelListener() {
            @Override
            public void tableChanged(TableModelEvent e) {
                System.out.println("table changed...");
                int row = e.getFirstRow();
                Long identifier = (Long) commentTable.getValueAt(row, 0);
                String reviewer = (String) commentTable.getValueAt(row, 1);
                String comments = (String) commentTable.getValueAt(row, 2);
                String type = (String) commentTable.getValueAt(row, 3);
                String severity = (String) commentTable.getValueAt(row, 4);
                String factor = (String) commentTable.getValueAt(row, 5);
                ReviewCommentInfoModel model = new ReviewCommentInfoModel();
                model.setIdentifier(identifier);
                model.setReviewer(reviewer);
                model.setComments(comments);
                model.setType(type);
                model.setSeverity(severity);
                model.setFactor(factor);

                // 新增的三个字段
                String handler = (String) commentTable.getValueAt(row, 12);
                String projectVersion = (String) commentTable.getValueAt(row, 10);
                String belongIssue = (String) commentTable.getValueAt(row, 11);
                model.setHandler(handler);
                model.setProjectVersion(projectVersion);
                model.setBelongIssue(belongIssue);

                InnerProjectCache projectCache = ProjectInstanceManager.getInstance().getProjectCache(ManageReviewCommentUI.this.project.getLocationHash());
                projectCache.updateCommonColumnContent(model);
            }
        });
    }

    private void bindTableListeners() {
        // 指定可编辑列颜色变更
        commentTable.setDefaultRenderer(Object.class, new CommentTableCellRender());

        // 双击跳转到源码位置
        commentTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int row = ((JTable) e.getSource()).rowAtPoint(e.getPoint());
                    int column = ((JTable) e.getSource()).columnAtPoint(e.getPoint());
                    if (!commentTable.isCellEditable(row, column)) {
                        doubleClickDumpToOriginal(ManageReviewCommentUI.this.project, row, column);
                        return;
                    }
                }
                // 其它场景，默认的处理方法
                super.mouseClicked(e);
            }
        });
    }

    private void doubleClickDumpToOriginal(Project project, int row, int column) {
        String filePath = (String) commentTable.getValueAt(row, 6);
        String line = (String) commentTable.getValueAt(row, 7);
        int startLine = 0;
        try {
            if (filePath == null || line == null) {
                throw new Exception("filePath or line is null");
            }

            String[] lines = line.split("~");
            if (lines.length != 2) {
                throw new Exception("line format illegal");
            }

            startLine = Integer.parseInt(lines[0].trim()) - 1;
            if (startLine < 0) {
                startLine = 0;
            }
        } catch (Exception e) {
            e.printStackTrace();
            Messages.showErrorDialog("open failed! Cause:" + System.lineSeparator() + e.getMessage(), "Open Failed");
            return;
        }

        PsiFile[] filesByName = PsiShortNamesCache.getInstance(project).getFilesByName(filePath);
        if (filesByName.length > 0) {
            PsiFile psiFile = filesByName[0];
            VirtualFile virtualFile = psiFile.getVirtualFile();
            // 打开对应的文件
            OpenFileDescriptor openFileDescriptor = new OpenFileDescriptor(project, virtualFile);
            Editor editor = FileEditorManager.getInstance(project).openTextEditor(openFileDescriptor, true);
            if (editor == null) {
                Messages.showErrorDialog("open failed! Cause:" + System.lineSeparator() + "editor is null", "Open Failed");
                return;
            }

            // 跳转到指定的位置
            CaretModel caretModel = editor.getCaretModel();
            LogicalPosition logicalPosition = caretModel.getLogicalPosition();
            logicalPosition.leanForward(true);
            LogicalPosition logical = new LogicalPosition(startLine, logicalPosition.column);
            caretModel.moveToLogicalPosition(logical);
            SelectionModel selectionModel = editor.getSelectionModel();
            selectionModel.selectLineAtCaret();
        } else {
            Messages.showErrorDialog("open failed! Cause:" + System.lineSeparator() + "当前工程中未找到此文件", "Open Failed");
        }

    }

    private void bindButtons() {
        clearButton.addActionListener(e -> {
            int resp = JOptionPane.showConfirmDialog(null, "确定清空所有评审内容吗？清空后将无法恢复！", "清空确认", JOptionPane.YES_NO_OPTION);
            if (resp != 0) {
                System.out.println("clear cancel");
                return;
            }
            InnerProjectCache projectCache = ProjectInstanceManager.getInstance().getProjectCache(ManageReviewCommentUI.this.project.getLocationHash());
            int clearComments = projectCache.clearComments();
            System.out.println("clear count: " + clearComments);
            reloadTableData();
        });

        importButton.addActionListener(e -> {

            List<ReviewCommentInfoModel> reviewCommentInfoModels = null;
            try {
                JFileChooser fileChooser = new JFileChooser();
                int saveDialog = fileChooser.showOpenDialog(fullPanel);
                if (saveDialog == JFileChooser.APPROVE_OPTION) {
                    String importPath = fileChooser.getSelectedFile().getPath();

                    reviewCommentInfoModels = ExcelOperateUtil.importExcel(importPath);
                    InnerProjectCache projectCache = ProjectInstanceManager.getInstance().getProjectCache(ManageReviewCommentUI.this.project.getLocationHash());
                    projectCache.importComments(reviewCommentInfoModels);
                    CommonUtil.reloadCommentListShow(ManageReviewCommentUI.this.project);
                    Messages.showMessageDialog("导入成功！", "导入完成", Icons.IMPORT_ICON);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                Messages.showErrorDialog("导入失败！原因：" + System.lineSeparator() + ex.getMessage(), "导入失败");
            }
        });

        exportButton.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setSelectedFile(new File("CodeReview_report_" + DateTimeUtil.getFormattedTimeForFileName()));
            fileChooser.setFileFilter(new FileNameExtensionFilter("Excel表格(*.xlsx)", ".xlsx"));
            int saveDialog = fileChooser.showSaveDialog(fullPanel);
            if (saveDialog == JFileChooser.APPROVE_OPTION) {
                String path = fileChooser.getSelectedFile().getPath();
                if (!path.toLowerCase().endsWith(".xlsx")) {
                    path += ".xlsx";
                }

                try {
                    InnerProjectCache projectCache = ProjectInstanceManager.getInstance().getProjectCache(ManageReviewCommentUI.this.project.getLocationHash());
                    ExcelOperateUtil.exportExcel(path, projectCache.getCachedComments());
                    Messages.showMessageDialog("导出成功", "导出完成", Icons.EXPORT_ICON);
                } catch (Exception ex) {
                    Messages.showErrorDialog("导出失败！原因:" + System.lineSeparator() + ex.getMessage(), "导出失败");
                }

            }

        });

        deleteButton.addActionListener(e -> {
            int resp = JOptionPane.showConfirmDialog(null, "确定删除所选评审内容吗？删除后无法恢复！", "删除确认", JOptionPane.YES_NO_OPTION);
            if (resp != 0) {
                System.out.println("delete cancel");
                return;
            }

            List<Long> deleteIndentifierList = new ArrayList<>();
            int[] selectedRows = commentTable.getSelectedRows();
            if (selectedRows != null && selectedRows.length > 0) {
                for (int rowId : selectedRows) {
                    Long valueAt = (Long) commentTable.getValueAt(rowId, 0);
                    deleteIndentifierList.add(valueAt);
                }
                InnerProjectCache projectCache = ProjectInstanceManager.getInstance().getProjectCache(ManageReviewCommentUI.this.project.getLocationHash());
                projectCache.deleteComments(deleteIndentifierList);
            }

            reloadTableData();
        });
    }

}
