package com.veezean.idea.plugin.codereviewer.action;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
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
import com.veezean.idea.plugin.codereviewer.model.*;

import javax.swing.*;
import javax.swing.event.MouseInputListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.TableModel;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 管理评审内容的主界面
 *
 * @author Wang Weiren
 * @since 2019/9/29
 */
public class ManageReviewCommentUI {
    private static final Object[] COLUMN_NAMES = {"ID", "检视人员", "检视意见", "意见类型",
            "严重级别", "问题归属", "文件路径", "行号范围", "内容摘录", "提交时间", "项目版本", "相关需求", "确认人", "确认结果", "确认备注说明"};
    private JButton clearButton;
    private JButton deleteButton;
    private JButton exportButton;
    private JButton importButton;
    private JTable commentTable;
    public JPanel fullPanel;
    private JButton NetworkConfigButton;
    private JButton updateFromServerButton;
    private JButton commitToServerButton;
    private JComboBox<ProjectEntity> selectProjectComboBox;
    private JLabel verisonTips;
    private JButton reloadProjectButton;
    private JComboBox updateFilterTypecomboBox;
    private final Project project;

    // 记录上一次按住alt点击的时间戳
    private long lastAltClickedTime = -1L;

    public ManageReviewCommentUI(Project project) {
        this.project = project;
    }


    public void initUI() {
        bindButtons();
        reloadTableData();
        bindTableListeners();
        renderActions();
    }

    public void refreshTableDataShow() {
        reloadTableData();
    }

    private void reloadTableData() {
        InnerProjectCache projectCache =
                ProjectInstanceManager.getInstance().getProjectCache(ManageReviewCommentUI.this.project.getLocationHash());
        List<ReviewCommentInfoModel> cachedComments = projectCache.getCachedComments();
        List<Object[]> rowDataList = new ArrayList<>();
        for (ReviewCommentInfoModel model : cachedComments) {
            Object[] row = {model.getIdentifier(), model.getReviewer(), model.getComments(), model.getType(),
                    model.getSeverity(), model.getFactor(), model.getFilePath(), model.getLineRange(),
                    model.getContent(),
                    model.getDateTime(), model.getProjectVersion(), model.getBelongIssue(), model.getHandler(),
                    model.getConfirmResult(), model.getConfirmNotes()
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

        // 确认结果
        JComboBox<String> confirmResultComboBox = new ComboBox<>();
        confirmResultComboBox.addItem("未确认");
        confirmResultComboBox.addItem("已修改");
        confirmResultComboBox.addItem("待修改");
        confirmResultComboBox.addItem("拒绝");
        commentTable.getColumnModel().getColumn(13).setCellEditor(new DefaultCellEditor(confirmResultComboBox));

        // 按住alt单击，弹出详情确认框
        commentTable.addMouseListener(new MouseAdapter() {


            @Override
            public void mouseReleased(MouseEvent e) {
                // 默认处理点击事件，不能丢
                super.mouseReleased(e);

                // 判断是否摁下alt、且单击场景才响应此事件
                boolean altDown = e.isAltDown();
                int clickCount = e.getClickCount();
                if (!altDown || clickCount > 1) {
                    return;
                }

                long currentTimeMillis = System.currentTimeMillis();
                if (currentTimeMillis - lastAltClickedTime < 500L) {
                    System.out.println("点击过快，忽略");
                    return;
                } else {
                    lastAltClickedTime = currentTimeMillis;
                }

                int rowAtPoint = commentTable.rowAtPoint(e.getPoint());
                System.out.println("按住alt并点击了表格第" + rowAtPoint + "行");

                // 弹出显示框
                InnerProjectCache projectCache =
                        ProjectInstanceManager.getInstance().getProjectCache(ManageReviewCommentUI.this.project.getLocationHash());
                ReviewCommentInfoModel commentInfoModel = projectCache.getCachedComments().get(rowAtPoint);
                AddReviewCommentUI.showDialog(commentInfoModel, project, Constants.CONFIRM_COMMENT);
            }
        });

        commentTable.getModel().addTableModelListener(e -> {
            System.out.println("table changed...");
            // 此处只处理界面上允许被修改的字段，进行赋值操作， 然后在updateCommonColumnContent方法中也仅更新这几个指定字段
            // 如果全部更新的话，有一些不允许被修改的隐藏字段（比如entityUniqueId等）可能信息会因修改其它字段而丢失
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
            String projectVersion = (String) commentTable.getValueAt(row, 10);
            String belongIssue = (String) commentTable.getValueAt(row, 11);
            String handler = (String) commentTable.getValueAt(row, 12);
            model.setProjectVersion(projectVersion);
            model.setBelongIssue(belongIssue);
            model.setHandler(handler);

            // 新增结果确认的2个字段
            String confirmResult = (String)commentTable.getValueAt(row, 13);
            String confirmNotes = (String)commentTable.getValueAt(row, 14);
            model.setConfirmResult(confirmResult);
            model.setConfirmNotes(confirmNotes);

            InnerProjectCache projectCache1 =
                    ProjectInstanceManager.getInstance().getProjectCache(ManageReviewCommentUI.this.project.getLocationHash());
            projectCache1.updateCommonColumnContent(model);
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
                Messages.showErrorDialog("open failed! Cause:" + System.lineSeparator() + "editor is null", "Open " +
                        "Failed");
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
            InnerProjectCache projectCache =
                    ProjectInstanceManager.getInstance().getProjectCache(ManageReviewCommentUI.this.project.getLocationHash());
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
                    InnerProjectCache projectCache =
                            ProjectInstanceManager.getInstance().getProjectCache(ManageReviewCommentUI.this.project.getLocationHash());
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
                    InnerProjectCache projectCache =
                            ProjectInstanceManager.getInstance().getProjectCache(ManageReviewCommentUI.this.project.getLocationHash());
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
                InnerProjectCache projectCache =
                        ProjectInstanceManager.getInstance().getProjectCache(ManageReviewCommentUI.this.project.getLocationHash());
                projectCache.deleteComments(deleteIndentifierList);
            }

            reloadTableData();
        });

        // 网络版本相关逻辑
        NetworkConfigButton.addActionListener(e -> NetworkConfigUI.showDialog());

        reloadProjectButton.addActionListener(e -> reloadProjectList());

        // 提交本地内容到服务端
        commitToServerButton.addActionListener(e -> {
            ProjectEntity selectedProject = (ProjectEntity) selectProjectComboBox.getSelectedItem();
            if (selectedProject == null) {
                System.out.println("未选中项目");
                Messages.showErrorDialog("请先从下拉框中选择一个项目!", "操作失败");
                return;
            }
            String projectKey = selectedProject.getProjectKey();

            try {
                CommitComment commitComment = buildCommitCommentData(projectKey);
                String commitCommentPostBody = JSON.toJSONString(commitComment);

                int resp = JOptionPane.showConfirmDialog(null, "共计" + commitComment.getComments().size()
                                + "条数据将被提交至【" + selectedProject.getProjectName() + "】项目中，是否确认提交？", "提交确认",
                        JOptionPane.YES_NO_OPTION);
                if (resp != 0) {
                    System.out.println("取消提交操作");
                    return;
                }

                System.out.println("本次提交的评审内容：");
                System.out.println(commitCommentPostBody);

                GlobalConfigInfo globalConfig = GlobalConfigManager.getInstance().getGlobalConfig();
                // 上传本地的评审信息内容
                String response = HttpUtil.post(globalConfig.getServerAddress() + "user_operate" +
                        "/commitComments", commitCommentPostBody, 30000);
                System.out.println("上传本地的评审信息内容：" + response);
                Response<List<Comment>> responseBean = JSON.parseObject(response,
                        new TypeReference<Response<List<Comment>>>() {
                        });
                if (responseBean.getCode() != 0) {
                    throw new CodeReviewException("上传本地的评审信息内容失败");
                }

                Messages.showMessageDialog("上传本地的评审信息内容成功", "操作完成", Icons.EXPORT_ICON);

            } catch (Exception ex) {
                ex.printStackTrace();
                Messages.showErrorDialog("处理异常，原因：" + ex.getMessage(), "操作失败");
            }

        });

        // 从服务端拉取内容到本地
        updateFromServerButton.addActionListener(e -> {
            ProjectEntity selectedProject = (ProjectEntity) selectProjectComboBox.getSelectedItem();
            if (selectedProject == null) {
                System.out.println("未选中项目");
                Messages.showErrorDialog("请先从下拉框中选择一个项目!", "操作失败");
                return;
            }

            String selectedType = (String) updateFilterTypecomboBox.getSelectedItem();

            int resp = JOptionPane.showConfirmDialog(null, "您选择了从服务端获取【" + selectedType +
                            "】评审意见，此操作将会覆盖本地已有的记录，如果本地数据未提交将会丢失，是否继续？",
                    "更新操作确认",
                    JOptionPane.YES_NO_OPTION);
            if (resp != 0) {
                System.out.println("取消更新操作");
                return;
            }

            int filterType = 0;
            switch (selectedType) {
                case "全部":
                    filterType = 0;
                    break;
                case "我提交或待我确认的":
                    filterType = 1;
                    break;
                case "我提交的":
                    filterType = 2;
                    break;
                case "待我确认的":
                    filterType = 3;
                    break;
                default:
                    throw new CodeReviewException("过滤类型不识别");
            }

            String projectKey = selectedProject.getProjectKey();
            try {
                GlobalConfigInfo globalConfig = GlobalConfigManager.getInstance().getGlobalConfig();
                // 获取评审内容列表信息
                Map<String, Object> params = new HashMap<>();
                params.put("projectKey", projectKey);
                params.put("filterType", filterType);
                params.put("currentUser", globalConfig.getAccount());
                String response = HttpUtil.get(globalConfig.getServerAddress() + "user_operate/queryProjectComments",
                        params, 30000);
                System.out.println("获取评审内容列表信息：" + response);
                Response<List<Comment>> responseBean = JSON.parseObject(response,
                        new TypeReference<Response<List<Comment>>>() {
                        });
                if (responseBean.getCode() != 0) {
                    throw new CodeReviewException("拉取评审内容列表失败");
                }
                List<Comment> commentEntities = responseBean.getData();
                if (commentEntities != null) {
                    List<ReviewCommentInfoModel> commentInfoModelList = commentEntities.stream()
                            .map(comment -> {
                                ReviewCommentInfoModel commentInfoModel = comment;
                                return commentInfoModel;
                            }).collect(Collectors.toList());

                    // 写入本地，并刷新表格显示
                    InnerProjectCache projectCache =
                            ProjectInstanceManager.getInstance().getProjectCache(ManageReviewCommentUI.this.project.getLocationHash());
                    projectCache.importComments(commentInfoModelList);
                    CommonUtil.reloadCommentListShow(ManageReviewCommentUI.this.project);
                }

                Messages.showMessageDialog("评审意见更新成功", "操作完成", Icons.IMPORT_ICON);

            } catch (Exception ex) {
                ex.printStackTrace();
                Messages.showErrorDialog("处理异常，原因：" + ex.getMessage(), "操作失败");
            }

        });
    }

    private CommitComment buildCommitCommentData(String projectKey) {
        // 本地内容构造成服务端需要的格式，提交服务端
        InnerProjectCache projectCache =
                ProjectInstanceManager.getInstance().getProjectCache(ManageReviewCommentUI.this.project.getLocationHash());
        List<ReviewCommentInfoModel> cachedComments = projectCache.getCachedComments();
        List<Comment> comments = cachedComments.stream()
                .map(reviewCommentInfoModel -> {
                    Comment comment = new Comment();
                    BeanUtil.copyProperties(reviewCommentInfoModel, comment);
                    return comment;
                }).collect(Collectors.toList());

        CommitComment commitComment = new CommitComment();
        commitComment.setProjectKey(projectKey);
        commitComment.setComments(comments);
        String account = GlobalConfigManager.getInstance().getGlobalConfig().getAccount();
        commitComment.setCommitUser(account);
        return commitComment;
    }

    private void renderActions() {
        boolean netVersion = GlobalConfigManager.getInstance().isNetVersion();
        switchNetButtonStatus(netVersion);
    }

    /**
     * 重新拉取服务端绑定的项目列表
     */
    private void reloadProjectList() {
        try {
            boolean netVersion = GlobalConfigManager.getInstance().isNetVersion();
            GlobalConfigInfo globalConfig = GlobalConfigManager.getInstance().getGlobalConfig();
            // 拉取项目列表
            if (netVersion) {
                Map<String, Object> params = new HashMap<>();
                params.put("userId", globalConfig.getAccount());
                String response = HttpUtil.get(globalConfig.getServerAddress() + "user_operate" +
                        "/queryUserBindedProjects", params, 30000);
                System.out.println("绑定项目列表信息：" + response);
                Response<List<ProjectEntity>> responseBean = JSON.parseObject(response,
                        new TypeReference<Response<List<ProjectEntity>>>() {
                        });
                if (responseBean.getCode() != 0) {
                    throw new CodeReviewException("拉取项目列表失败");
                }
                List<ProjectEntity> projectEntities = responseBean.getData();
                selectProjectComboBox.removeAllItems();
                projectEntities.forEach(projectEntity -> selectProjectComboBox.addItem(projectEntity));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 根据配置是否网络版本，切换相关按钮是否可用
     *
     * @param netVersion 是否网络版本，true是，false本地版
     */
    public void switchNetButtonStatus(boolean netVersion) {
        selectProjectComboBox.setEnabled(netVersion);
        updateFromServerButton.setEnabled(netVersion);
        commitToServerButton.setEnabled(netVersion);
        reloadProjectButton.setEnabled(netVersion);
        updateFilterTypecomboBox.setEnabled(netVersion);

        if (!netVersion) {
            verisonTips.setText("当前为本地版本，可通过设置切换为网络版本，方便团队协作");
        } else {
            verisonTips.setText("当前为网络版本，可在设置中配置服务器地址、用户密码等信息");
        }
    }
}
