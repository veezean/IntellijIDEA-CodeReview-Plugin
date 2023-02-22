package com.veezean.idea.plugin.codereviewer.action;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.http.HttpUtil;
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
import com.intellij.psi.PsiJavaFile;
import com.intellij.psi.search.PsiShortNamesCache;
import com.veezean.idea.plugin.codereviewer.common.*;
import com.veezean.idea.plugin.codereviewer.consts.InputTypeDefine;
import com.veezean.idea.plugin.codereviewer.model.*;
import com.veezean.idea.plugin.codereviewer.util.Logger;
import org.apache.commons.lang.StringUtils;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.TableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 管理评审内容的主界面
 *
 * @author Wang Weiren
 * @since 2019/9/29
 */
public class ManageReviewCommentUI {
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
    private JButton reloadProjectButton;
    private JComboBox updateFilterTypecomboBox;
    private JPanel networkButtonGroupPanel;
    private JLabel versionNotes;
    private JLabel showHelpDocButton;
    private final Project project;

    // 记录上一次按住alt点击的时间戳
    private long lastAltClickedTime = -1L;

    public ManageReviewCommentUI(Project project) {
        this.project = project;
        showHelpDocButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                try {
                    Desktop.getDesktop().browse(URI.create("http://blog.codingcoder.cn/post/codereviewhelperdoc.html"));
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        });
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
        List<ReviewComment> cachedComments = projectCache.getCachedComments();

        RecordColumns recordColumns = GlobalConfigManager.getInstance().getCustomConfigColumns();
        List<Column> availableColumns = recordColumns.getTableAvailableColumns();
        List<Object[]> rowDataList = new ArrayList<>();

        projectCache.getCachedComments().stream()
                .sorted((o1, o2) -> (int) (Long.parseLong(o2.getId()) - Long.parseLong(o1.getId())))
                .forEach(reviewComment -> {
                    Object[] row = new Object[availableColumns.size()];
                    for (int i = 0; i < availableColumns.size(); i++) {
                        row[i] = reviewComment.getPropValue(availableColumns.get(i).getColumnCode());
                    }
                    rowDataList.add(row);
                });

        Object[][] rowData = rowDataList.toArray(new Object[0][]);

        // 根据配置渲染界面表格
        TableModel dataModel = new CommentTableModel(rowData, recordColumns);
        commentTable.setModel(dataModel);
        commentTable.setEnabled(true);
        for (int i = 0; i < availableColumns.size(); i++) {
            Column column = availableColumns.get(i);
            if (InputTypeDefine.COMBO_BOX.getValue().equalsIgnoreCase(column.getInputType())) {
                JComboBox<String> comboBox = new ComboBox<>();
                column.getEnumValues().forEach(comboBox::addItem);
                commentTable.getColumnModel().getColumn(i).setCellEditor(new DefaultCellEditor(comboBox));
            }
        }

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
                    Logger.info("点击过快，忽略");
                    return;
                } else {
                    lastAltClickedTime = currentTimeMillis;
                }

                int rowAtPoint = commentTable.rowAtPoint(e.getPoint());
                Logger.info("按住alt并点击了表格第" + rowAtPoint + "行");

                // 弹出显示框
                InnerProjectCache projectCache =
                        ProjectInstanceManager.getInstance().getProjectCache(ManageReviewCommentUI.this.project.getLocationHash());
                ReviewComment commentInfoModel = projectCache.getCachedComments().get(rowAtPoint);
                ReviewCommentDialog.show(commentInfoModel, project, Constants.CONFIRM_COMMENT);
            }
        });

        commentTable.getModel().addTableModelListener(e -> {
            Logger.info("监听到了表格内容变更事件...");
            int row = e.getFirstRow();

            ReviewComment comment = new ReviewComment();
            for (int i = 0; i < availableColumns.size(); i++) {
                comment.setPropValue(availableColumns.get(i).getColumnCode(), (String) commentTable.getValueAt(row, i));
            }

            InnerProjectCache projectCache1 =
                    ProjectInstanceManager.getInstance().getProjectCache(ManageReviewCommentUI.this.project.getLocationHash());
            projectCache1.updateCommonColumnContent(comment);
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
                        doubleClickDumpToOriginal(ManageReviewCommentUI.this.project, row);
                        return;
                    }
                }
                // 其它场景，默认的处理方法
                super.mouseClicked(e);
            }
        });
    }

    private void doubleClickDumpToOriginal(Project project, int row) {
        InnerProjectCache projectCache =
                ProjectInstanceManager.getInstance().getProjectCache(ManageReviewCommentUI.this.project.getLocationHash());
        String id = (String) commentTable.getValueAt(row, 0);
        ReviewComment commentInfoModel = projectCache.getCachedCommentById(id);

        String filePath = commentInfoModel.getFilePath();
        String packageName = "";
        try {
            String[] splitFilePath = filePath.split("\\|");
            if (splitFilePath.length > 1) {
                packageName = splitFilePath[0];
                filePath = splitFilePath[1];
            }
        } catch (Exception e) {
            e.printStackTrace();
            Messages.showErrorDialog("打开失败，原因:" + System.lineSeparator() + e.getMessage(), "打开失败");
            return;
        }

        PsiFile[] filesByName = PsiShortNamesCache.getInstance(project).getFilesByName(filePath);
        if (filesByName.length > 0) {
            String targetFilePkgName = packageName;
            PsiFile psiFile = Stream.of(filesByName).filter(psi -> {
                if (psi instanceof PsiJavaFile && StringUtils.isNotEmpty(targetFilePkgName)) {
                    PsiJavaFile javaFile = (PsiJavaFile) psi;
                    String pkgName = javaFile.getPackageName();
                    return StringUtils.equals(pkgName, targetFilePkgName);
                } else {
                    return true;
                }
            }).findFirst().orElse(null);

            if (psiFile == null) {
                Messages.showErrorDialog("文件不存在：" + packageName + "." + filePath, "打开失败");
                return;
            }

            VirtualFile virtualFile = psiFile.getVirtualFile();
            // 打开对应的文件
            OpenFileDescriptor openFileDescriptor = new OpenFileDescriptor(project, virtualFile);
            Editor editor = FileEditorManager.getInstance(project).openTextEditor(openFileDescriptor, true);
            if (editor == null) {
                Messages.showErrorDialog("打开失败！原因：" + System.lineSeparator() + "编辑器不存在", "打开失败");
                return;
            }

            // 跳转到指定的位置
            CaretModel caretModel = editor.getCaretModel();
            LogicalPosition logicalPosition = caretModel.getLogicalPosition();
            logicalPosition.leanForward(true);
            LogicalPosition logical = new LogicalPosition(commentInfoModel.getStartLine(), logicalPosition.column);
            caretModel.moveToLogicalPosition(logical);
            SelectionModel selectionModel = editor.getSelectionModel();
            selectionModel.selectLineAtCaret();
        } else {
            Messages.showErrorDialog("打开失败！原因：" + System.lineSeparator() + "当前工程中未找到此文件", "打开失败");
        }

    }

    private void bindButtons() {
        clearButton.addActionListener(e -> {
            int resp = JOptionPane.showConfirmDialog(null, "确定要清空本地所有记录吗？此操作不可恢复！",
                    "清空操作确认",
                    JOptionPane.YES_NO_OPTION);
            if (resp != 0) {
                Logger.info("取消清空操作...");
                return;
            }
            InnerProjectCache projectCache =
                    ProjectInstanceManager.getInstance().getProjectCache(ManageReviewCommentUI.this.project.getLocationHash());
            int clearComments = projectCache.clearComments();
            Logger.info("执行清空操作，清空条数： " + clearComments);
            reloadTableData();
        });

        importButton.addActionListener(e -> {

            List<ReviewComment> reviewCommentInfoModels = null;
            try {
                String recentSelectedFileDir = GlobalConfigManager.getInstance().getRecentSelectedFileDir();
                JFileChooser fileChooser = new JFileChooser(recentSelectedFileDir);
                int saveDialog = fileChooser.showOpenDialog(fullPanel);
                if (saveDialog == JFileChooser.APPROVE_OPTION) {
                    String importPath = fileChooser.getSelectedFile().getPath();

                    GlobalConfigManager.getInstance().saveRecentSelectedFileDir(new File(importPath).getParentFile().getAbsolutePath());

                    reviewCommentInfoModels = ExcelResultProcessor.importExcel(importPath);
                    InnerProjectCache projectCache =
                            ProjectInstanceManager.getInstance().getProjectCache(ManageReviewCommentUI.this.project.getLocationHash());
                    projectCache.importComments(reviewCommentInfoModels);
                    CommonUtil.reloadCommentListShow(ManageReviewCommentUI.this.project);
                    Messages.showMessageDialog("导出成功", "操作提示", ImageIconHelper.getDefaultIcon());
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                Messages.showErrorDialog("导出失败！原因：" + System.lineSeparator() + ex.getMessage(), "操作失败");
            }
        });

        exportButton.addActionListener(e -> {
            String recentSelectedFileDir = GlobalConfigManager.getInstance().getRecentSelectedFileDir();
            JFileChooser fileChooser = new JFileChooser(recentSelectedFileDir);
            fileChooser.setSelectedFile(new File("代码检视意见_" + DateTimeUtil.getFormattedTimeForFileName()));
            fileChooser.setFileFilter(new FileNameExtensionFilter("Excel表格(*.xlsx)", ".xlsx"));
            int saveDialog = fileChooser.showSaveDialog(fullPanel);
            if (saveDialog == JFileChooser.APPROVE_OPTION) {
                String path = fileChooser.getSelectedFile().getPath();
                if (!path.toLowerCase().endsWith(".xlsx")) {
                    path += ".xlsx";
                }

                GlobalConfigManager.getInstance().saveRecentSelectedFileDir(new File(path).getParentFile().getAbsolutePath());

                try {
                    InnerProjectCache projectCache =
                            ProjectInstanceManager.getInstance().getProjectCache(ManageReviewCommentUI.this.project.getLocationHash());
                    ExcelResultProcessor.export(path, projectCache.getCachedComments());
                    Messages.showMessageDialog("导出成功", "操作完成", ImageIconHelper.getDefaultIcon());
                } catch (Exception ex) {
                    Messages.showErrorDialog("导出失败！原因：" + System.lineSeparator() + ex.getMessage(),
                            "操作失败");
                }
            }
        });

        deleteButton.addActionListener(e -> {
            int resp = JOptionPane.showConfirmDialog(null, "确定要删除所选记录吗？此操作不可恢复！", "删除操作确认",
                    JOptionPane.YES_NO_OPTION);
            if (resp != 0) {
                Logger.info("取消删除评审记录操作");
                return;
            }

            List<String> deleteIndentifierList = new ArrayList<>();
            int[] selectedRows = commentTable.getSelectedRows();
            if (selectedRows != null && selectedRows.length > 0) {
                for (int rowId : selectedRows) {
                    String valueAt = (String) commentTable.getValueAt(rowId, 0);
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

        reloadProjectButton.addActionListener(e -> {
            new Thread(() -> {
                try {
                    reloadProjectButton.setEnabled(false);
                    VersionType versionType = GlobalConfigManager.getInstance().getVersionType();
                    GlobalConfigInfo globalConfig = GlobalConfigManager.getInstance().getGlobalConfig();
                    // 拉取项目列表
                    if (VersionType.NETWORK.equals(versionType)) {
                        Map<String, Object> params = new HashMap<>();
                        params.put("userId", globalConfig.getAccount());
                        String response = HttpUtil.get(globalConfig.getServerAddress() + "user_operate" +
                                "/queryUserBindedProjects", params, 30000);
                        Logger.info("绑定项目列表信息：" + response);
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
                } catch (Exception ex) {
                    ex.printStackTrace();
                } finally {
                    reloadProjectButton.setEnabled(true);
                }
            }).start();
        });

        // 提交本地内容到服务端
        commitToServerButton.addActionListener(e -> {
            ProjectEntity selectedProject = (ProjectEntity) selectProjectComboBox.getSelectedItem();
            if (selectedProject == null) {
                Logger.error("提交服务端失败，未选中项目");
                Messages.showErrorDialog("请先选择一个项目！", "操作错误提示");
                return;
            }
            String projectKey = selectedProject.getProjectKey();
            CommitComment commitComment = buildCommitCommentData(projectKey);
            String commitCommentPostBody = JSON.toJSONString(commitComment);

            int resp = JOptionPane.showConfirmDialog(null, "共有 " + commitComment.getComments().size()
                            + "条记录将被提交到服务端【" + selectedProject.getProjectName() + "】项目中，是否确认提交？",
                    "提交前确认",
                    JOptionPane.YES_NO_OPTION);
            if (resp != 0) {
                Logger.info("取消提交操作");
                return;
            }

            Logger.info("本次提交的评审内容：" + commitCommentPostBody);

            // 子线程操作防止界面卡死
            AtomicBoolean isSuccess = new AtomicBoolean(true);
            Thread workThread = new Thread(() -> {
                try {
                    commitToServerButton.setEnabled(false);
                    GlobalConfigInfo globalConfig = GlobalConfigManager.getInstance().getGlobalConfig();
                    // 上传本地的评审信息内容
                    String response = HttpUtil.post(globalConfig.getServerAddress() + "user_operate" +
                            "/commitComments", commitCommentPostBody, 30000);
                    Logger.info("上传本地的评审信息内容：" + response);
                    Response<List<Comment>> responseBean = JSON.parseObject(response,
                            new TypeReference<Response<List<Comment>>>() {
                            });
                    if (responseBean.getCode() != 0) {
                        throw new CodeReviewException("上传本地的评审信息内容失败");
                    }


                } catch (Exception ex) {
                    ex.printStackTrace();
                    isSuccess.set(false);

                } finally {
                    commitToServerButton.setEnabled(true);
                }
            });
            workThread.start();

            try {
                workThread.join();
            } catch (Exception ex) {
                ex.printStackTrace();
            }

            if (isSuccess.get()) {
                Messages.showMessageDialog("提交成功", "操作完成", ImageIconHelper.getDefaultIcon());
            } else {
                Messages.showErrorDialog("操作失败", "操作失败");
            }

        });

        // 从服务端拉取内容到本地
        updateFromServerButton.addActionListener(e -> {
            ProjectEntity selectedProject = (ProjectEntity) selectProjectComboBox.getSelectedItem();
            if (selectedProject == null) {
                Logger.info("未选中项目");
                Messages.showErrorDialog("请先选择一个项目！", "错误提示");
                return;
            }

            String selectedType = (String) updateFilterTypecomboBox.getSelectedItem();

            int resp = JOptionPane.showConfirmDialog(null,
                    "你即将从服务端拉取【" + selectedType +
                            "】类型的评审意见信息，这将覆盖你本地已有记录，操作前请先确保本地数据已经提交或者导出保存。确认继续执行拉取操作吗？",
                    "操作确认",
                    JOptionPane.YES_NO_OPTION);
            if (resp != 0) {
                Logger.info("取消更新操作");
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
            int finalFilterType = filterType;
            // 子线程操作，防止界面卡死
            AtomicBoolean isSuccess = new AtomicBoolean(true);
            Thread workThread = new Thread(() -> {
                try {
                    updateFromServerButton.setEnabled(false);
                    GlobalConfigInfo globalConfig = GlobalConfigManager.getInstance().getGlobalConfig();
                    // 获取评审内容列表信息
                    Map<String, Object> params = new HashMap<>();
                    params.put("projectKey", projectKey);
                    params.put("filterType", finalFilterType);
                    params.put("currentUser", globalConfig.getAccount());
                    String response = HttpUtil.get(globalConfig.getServerAddress() + "user_operate" +
                                    "/queryProjectComments",
                            params, 30000);
                    Logger.info("获取评审内容列表信息：" + response);
                    Response<List<Comment>> responseBean = JSON.parseObject(response,
                            new TypeReference<Response<List<Comment>>>() {
                            });
                    if (responseBean.getCode() != 0) {
                        throw new CodeReviewException("拉取评审内容列表失败");
                    }
                    List<Comment> commentEntities = responseBean.getData();
                    updateLocalData(commentEntities);
                } catch (Exception ex) {
                    ex.printStackTrace();
                    isSuccess.set(false);
                } finally {
                    updateFromServerButton.setEnabled(true);
                }
            });

            workThread.start();

            try {
                workThread.join();
            } catch (Exception ex) {
                ex.printStackTrace();
            }

            if (isSuccess.get()) {
                Messages.showMessageDialog("拉取成功", "操作完成", ImageIconHelper.getDefaultIcon());
            } else {
                Messages.showErrorDialog("操作失败", "操作失败");
            }
        });
    }

    private void updateLocalData(List<Comment> comments) {
        try {
            if (comments != null) {
                List<ReviewComment> commentInfoModelList = comments.stream()
                        .map(comment -> (ReviewComment) comment).collect(Collectors.toList());

                // 写入本地，并刷新表格显示
                InnerProjectCache projectCache =
                        ProjectInstanceManager.getInstance().getProjectCache(ManageReviewCommentUI.this.project.getLocationHash());
                projectCache.importComments(commentInfoModelList);
                CommonUtil.reloadCommentListShow(ManageReviewCommentUI.this.project);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private CommitComment buildCommitCommentData(String projectKey) {
        List<Comment> comments = generateCommitList();
        CommitComment commitComment = new CommitComment();
        commitComment.setProjectKey(projectKey);
        commitComment.setComments(comments);
        String account = GlobalConfigManager.getInstance().getGlobalConfig().getAccount();
        commitComment.setCommitUser(account);
        return commitComment;
    }

    private List<Comment> generateCommitList() {
        // 本地内容构造成服务端需要的格式，提交服务端
        InnerProjectCache projectCache =
                ProjectInstanceManager.getInstance().getProjectCache(ManageReviewCommentUI.this.project.getLocationHash());
        List<ReviewComment> cachedComments = projectCache.getCachedComments();
        return cachedComments.stream()
                .map(reviewCommentInfoModel -> {
                    Comment comment = new Comment();
                    BeanUtil.copyProperties(reviewCommentInfoModel, comment);
                    return comment;
                }).collect(Collectors.toList());
    }

    private void renderActions() {
        VersionType versionType = GlobalConfigManager.getInstance().getVersionType();
        switchNetButtonStatus(versionType);
    }

    /**
     * 根据配置是否网络版本，切换相关按钮是否可用
     *
     * @param versionType 版本类型，参见 VersionType 定义
     */
    void switchNetButtonStatus(VersionType versionType) {
        switch (versionType) {
            case NETWORK:
                networkButtonGroupPanel.setVisible(true);
                versionNotes.setText("网络模式");
                break;
            default:
                networkButtonGroupPanel.setVisible(false);
                versionNotes.setText("单机模式");
                break;
        }
    }
}
