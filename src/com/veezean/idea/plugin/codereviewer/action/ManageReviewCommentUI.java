package com.veezean.idea.plugin.codereviewer.action;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.http.HttpResponse;
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
import com.veezean.idea.plugin.codereviewer.model.*;
import org.apache.commons.lang.StringUtils;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.TableModel;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 管理评审内容的主界面
 *
 * @author Wang Weiren
 * @since 2019/9/29
 */
public class ManageReviewCommentUI {
    private static final Object[] COLUMN_NAMES = {"ID", "Reviewer", "Comment", "Type",
            "Level", "BelongTo", "FilePath", "LineRange", "Content", "CommitTime", "ProjectVersion",
            "BelongingRequirement",
            "Confirmer",
            "Confirm Result",
            "Confirm Note",
            "Status"};
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
    private JPanel networkGiteeButtonGroupPanel;
    private JButton createIssuesButton;
    private JButton updateLocalIssueStatusButton;
    private JLabel versionNotes;
    private final Project project;

    // 记录上一次按住alt点击的时间戳
    private long lastAltClickedTime = -1L;

    public ManageReviewCommentUI(Project project) {
        this.project = project;
    }


    public void initUI() {
        bindButtons();
        bindGiteeButtons();
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
                    model.getConfirmResult(), model.getConfirmNotes(),
                    generateStatus(model)
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
            String confirmResult = (String) commentTable.getValueAt(row, 13);
            String confirmNotes = (String) commentTable.getValueAt(row, 14);
            model.setConfirmResult(confirmResult);
            model.setConfirmNotes(confirmNotes);

            InnerProjectCache projectCache1 =
                    ProjectInstanceManager.getInstance().getProjectCache(ManageReviewCommentUI.this.project.getLocationHash());
            projectCache1.updateCommonColumnContent(model);
        });
    }

    private String generateStatus(ReviewCommentInfoModel model) {
        if (model.getGiteeIssueInfo() == null) {
            return "LOCAL";
        }

        GlobalConfigInfo globalConfig = GlobalConfigManager.getInstance().getGlobalConfig();
        if (globalConfig.getVersionType() == VersionType.NETWORK_GITEE.getValue()) {
            return "Issue " + model.getGiteeIssueInfo().getState();
        }

        return "LOCAL";
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
        String packageName = "";
        String line = (String) commentTable.getValueAt(row, 7);
        int startLine = 0;
        try {
            if (filePath == null || line == null) {
                throw new Exception("filePath or line is null");
            }

            String[] splitFilePath = filePath.split("\\|");
            if (splitFilePath.length > 1) {
                packageName = splitFilePath[0];
                filePath = splitFilePath[1];
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
                Messages.showErrorDialog("file not found! file: " + packageName + "." + filePath, "Open Failed");
                return;
            }

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
            int resp = JOptionPane.showConfirmDialog(null, "Sure you want to clear all local " +
                            "comments? This cannot be undo!",
                    "Clear Confirm",
                    JOptionPane.YES_NO_OPTION);
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
                    Messages.showMessageDialog("Import Successful", "Finished", ImageIconHelper.getDefaultIcon());
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                Messages.showErrorDialog("Import failed! Cause:" + System.lineSeparator() + ex.getMessage(), "Failed");
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
                    Messages.showMessageDialog("Export Successful", "Finished", ImageIconHelper.getDefaultIcon());
                } catch (Exception ex) {
                    Messages.showErrorDialog("Export failed! Cause: " + System.lineSeparator() + ex.getMessage(),
                            "Failed");
                }

            }

        });

        deleteButton.addActionListener(e -> {
            int resp = JOptionPane.showConfirmDialog(null, "Sure you want to delete selected comments? This cannot be" +
                            " undo!", "Delete Confirm",
                    JOptionPane.YES_NO_OPTION);
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
                System.out.println("未选中项目");
                Messages.showErrorDialog("Please select a project first!", "ERROR");
                return;
            }
            String projectKey = selectedProject.getProjectKey();
            CommitComment commitComment = buildCommitCommentData(projectKey);
            String commitCommentPostBody = JSON.toJSONString(commitComment);

            int resp = JOptionPane.showConfirmDialog(null, "Total " + commitComment.getComments().size()
                            + " comments will be uploaded into [" + selectedProject.getProjectName() + "] project in " +
                            "server, confirm to upload?",
                    "Upload Confirm",
                    JOptionPane.YES_NO_OPTION);
            if (resp != 0) {
                System.out.println("取消提交操作");
                return;
            }

            System.out.println("本次提交的评审内容：");
            System.out.println(commitCommentPostBody);

            // 子线程操作防止界面卡死
            AtomicBoolean isSuccess = new AtomicBoolean(true);
            Thread workThread = new Thread(() -> {
                try {
                    commitToServerButton.setEnabled(false);
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
                Messages.showMessageDialog("Upload Success", "Finished", ImageIconHelper.getDefaultIcon());
            } else {
                Messages.showErrorDialog("Operation Failed", "ERROR");
            }

        });

        // 从服务端拉取内容到本地
        updateFromServerButton.addActionListener(e -> {
            ProjectEntity selectedProject = (ProjectEntity) selectProjectComboBox.getSelectedItem();
            if (selectedProject == null) {
                System.out.println("未选中项目");
                Messages.showErrorDialog("Please select a project first!", "ERROR");
                return;
            }

            String selectedType = (String) updateFilterTypecomboBox.getSelectedItem();

            int resp = JOptionPane.showConfirmDialog(null,
                    "You are going to download the comments with the type of [" + selectedType +
                            "], this will overwrite all local exist data. Sure you will do this? ",
                    "Confirm",
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
                    System.out.println("获取评审内容列表信息：" + response);
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
                Messages.showMessageDialog("Download Successful", "Finished", ImageIconHelper.getDefaultIcon());
            } else {
                Messages.showErrorDialog("Operation Failed", "ERROR");
            }

        });
    }

    private void bindGiteeButtons() {
        createIssuesButton.addActionListener(e -> {

            GlobalConfigInfo globalConfig = GlobalConfigManager.getInstance().getGlobalConfig();
            String postUrlPattern = Constants.GITEE_SERVER_URL + "/repos/{0}/issues";
            String postUrl = MessageFormat.format(postUrlPattern, globalConfig.getGiteeRepoOwner());
            // 过滤出所有未曾提交过的评审意见
            List<Comment> comments =
                    generateCommitList().stream().filter(comment -> comment.getGiteeIssueInfo() == null).collect(Collectors.toList());

            if (comments.isEmpty()) {
                Messages.showMessageDialog("No new comment need to create issue into gitee", "Notification",
                        ImageIconHelper.getDefaultIcon());
                return;
            }

            int resp = JOptionPane.showConfirmDialog(null, "Total " + comments.size()
                            + " comments will be created as issues into gitee [" + globalConfig.getGiteeRepoOwner()
                            + "/" + globalConfig.getGiteeRepoPath()
                            + "], confirm it?",
                    "Create Confirm",
                    JOptionPane.YES_NO_OPTION);
            if (resp != 0) {
                System.out.println("取消提交操作");
                return;
            }

            // 子线程操作，防止界面卡死
            AtomicInteger failedCount = new AtomicInteger();
            Thread workThread = new Thread(() -> {
                try {
                    createIssuesButton.setEnabled(false);
                    for (Comment comment : comments) {
                        GiteeIssueModel issueModel = convertToGiteeIssueModel(comment,
                                globalConfig.getGiteePrivateToken(),
                                globalConfig.getGiteeRepoPath());
                        try {
                            HttpResponse httpResponse = HttpHelper.post(postUrl, issueModel, 30000);
                            int status = httpResponse.getStatus();
                            if (status >= 200 && status < 300) {
                                String body = httpResponse.body();
                                // 从body里面解析出对应的issueID
                                CreateIssueResponseBody giteeIssue = JSON.parseObject(body,
                                        CreateIssueResponseBody.class);
                                comment.setGiteeIssueInfo(giteeIssue);
                            } else {
                                // 失败
                                failedCount.incrementAndGet();
                            }
                        } catch (Exception exception) {
                            failedCount.incrementAndGet();
                            exception.printStackTrace();
                        }
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                    failedCount.incrementAndGet();
                } finally {
                    createIssuesButton.setEnabled(true);

                    // 将已提交的issueID信息更新到本地
                    updateLocalData(comments);
                }
            });

            workThread.start();

            try {
                workThread.join();
            } catch (Exception ex) {
                ex.printStackTrace();
            }

            if (failedCount.get() > 0) {
                Messages.showErrorDialog("Create issues failed count: " + failedCount.get(), "ERROR");
            } else {
                Messages.showMessageDialog("Operation Finished", "FINISHED", ImageIconHelper.getDefaultIcon());
            }
        });

        // 更新已转换为issue的记录的issue状态
        updateLocalIssueStatusButton.addActionListener(e -> {
            GlobalConfigInfo globalConfig = GlobalConfigManager.getInstance().getGlobalConfig();
            String updateUrlPattern = Constants.GITEE_SERVER_URL + "/repos/{0}/{1}/issues/{2}?access_token={3}";

            // 过滤出所有已提交的评审意见
            List<Comment> comments =
                    generateCommitList().stream().filter(comment -> comment.getGiteeIssueInfo() != null).collect(Collectors.toList());
            if (comments.isEmpty()) {
                Messages.showMessageDialog("No comment need to be update", "FINISHED", ImageIconHelper.getDefaultIcon());
                return;
            }

            // 子线程操作，防止界面卡死
            AtomicInteger failedCount = new AtomicInteger();
            Thread workThread = new Thread(() -> {
                try {
                    createIssuesButton.setEnabled(false);
                    for (Comment comment : comments) {
                        CreateIssueResponseBody existInfo = comment.getGiteeIssueInfo();
                        String updateUrl = MessageFormat.format(updateUrlPattern, globalConfig.getGiteeRepoOwner(),
                                globalConfig.getGiteeRepoPath(), existInfo.getNumber(), globalConfig.getGiteePrivateToken());
                        try {
                            HttpResponse httpResponse = HttpHelper.get(updateUrl, 30000);
                            int status = httpResponse.getStatus();
                            if (status >= 200 && status < 300) {
                                String body = httpResponse.body();
                                // 从body里面解析出对应的issueID
                                CreateIssueResponseBody giteeIssue = JSON.parseObject(body,
                                        CreateIssueResponseBody.class);
                                comment.setGiteeIssueInfo(giteeIssue);
                            } else {
                                // 失败
                                failedCount.incrementAndGet();
                            }
                        } catch (Exception exception) {
                            failedCount.incrementAndGet();
                            exception.printStackTrace();
                        }
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                    failedCount.incrementAndGet();
                } finally {
                    createIssuesButton.setEnabled(true);

                    // 将已提交的issueID信息更新到本地
                    updateLocalData(comments);
                }
            });

            workThread.start();

            try {
                workThread.join();
            } catch (Exception ex) {
                ex.printStackTrace();
            }

            if (failedCount.get() > 0) {
                Messages.showErrorDialog("Update issues failed count: " + failedCount.get(), "ERROR");
            } else {
                Messages.showMessageDialog("Operation Finished", "FINISHED", ImageIconHelper.getDefaultIcon());
            }
        });
    }

    private void updateLocalData(List<Comment> comments) {
        try {
            if (comments != null) {
                List<ReviewCommentInfoModel> commentInfoModelList = comments.stream()
                        .map(comment -> (ReviewCommentInfoModel) comment).collect(Collectors.toList());

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

    private GiteeIssueModel convertToGiteeIssueModel(Comment comment, String token, String repo) {
        GiteeIssueModel model = new GiteeIssueModel();
        model.setAccessToken(token);
        model.setRepo(repo);
        model.setLabels("CodeReview");
        model.setSecurityHole(false);

        String title = "【" + comment.getType() + "】【" + comment.getFactor() + "】【" + comment.getSeverity() + "】代码检视意见";
        model.setTitle(title);

        StringBuilder bodyBuilder = new StringBuilder();
        bodyBuilder.append(" **代码信息**").append(System.lineSeparator()).append(System.lineSeparator())
                .append("> ").append(comment.getFilePath()).append(" (").append(comment.getLineRange()).append("行)").append(System.lineSeparator()).append(System.lineSeparator())
                .append("```").append(System.lineSeparator())
                .append(comment.getContent()).append(System.lineSeparator())
                .append("```").append(System.lineSeparator()).append(System.lineSeparator())
                .append("---").append(System.lineSeparator()).append(System.lineSeparator())
                .append("**检视意见**").append(System.lineSeparator()).append(System.lineSeparator())
//                .append("> 意见类型：").append(comment.getType()).append(System.lineSeparator())
//                .append("> 问题归属：").append(comment.getFactor()).append(System.lineSeparator())
//                .append("> 严重级别：").append(comment.getSeverity()).append(System.lineSeparator())
                .append(comment.getComments()).append(System.lineSeparator()).append(System.lineSeparator());
        model.setBody(bodyBuilder.toString());

        return model;
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
        List<ReviewCommentInfoModel> cachedComments = projectCache.getCachedComments();
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
            case NETWORK_GITEE:
                networkButtonGroupPanel.setVisible(false);
                networkGiteeButtonGroupPanel.setVisible(true);
                GlobalConfigInfo globalConfig = GlobalConfigManager.getInstance().getGlobalConfig();
                versionNotes.setText("Gitee-Mode [ " + globalConfig.getGiteeRepoOwner() + "/" + globalConfig.getGiteeRepoPath() + " ]");
                break;
            case NETWORK:
                networkButtonGroupPanel.setVisible(true);
                networkGiteeButtonGroupPanel.setVisible(false);
                versionNotes.setText("Network-Mode");
                break;
            default:
                networkButtonGroupPanel.setVisible(false);
                networkGiteeButtonGroupPanel.setVisible(false);
                versionNotes.setText("Local-Mode");
                break;
        }
    }
}
