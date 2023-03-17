package com.veezean.idea.plugin.codereviewer.action;

import cn.hutool.cron.CronUtil;
import cn.hutool.cron.task.Task;
import com.alibaba.fastjson.TypeReference;
import com.intellij.notification.*;
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
import com.intellij.ui.JBColor;
import com.veezean.idea.plugin.codereviewer.common.GlobalConfigManager;
import com.veezean.idea.plugin.codereviewer.common.InnerProjectCache;
import com.veezean.idea.plugin.codereviewer.common.NetworkOperationHelper;
import com.veezean.idea.plugin.codereviewer.common.ProjectInstanceManager;
import com.veezean.idea.plugin.codereviewer.consts.Constants;
import com.veezean.idea.plugin.codereviewer.consts.InputTypeDefine;
import com.veezean.idea.plugin.codereviewer.model.*;
import com.veezean.idea.plugin.codereviewer.util.CommonUtil;
import com.veezean.idea.plugin.codereviewer.util.ExcelResultProcessor;
import com.veezean.idea.plugin.codereviewer.util.Logger;
import org.apache.commons.lang.StringUtils;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.TableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 管理评审内容的主界面
 *
 * @author Veezean, 公众号 @架构悟道
 * @since 2019/9/29
 */
public class ManageReviewCommentUI {
    private JButton clearButton;
    private JButton deleteButton;
    private JButton exportButton;
    private JButton importButton;
    private JTable commentTable;
    public JPanel fullPanel;
    private JButton networkConfigButton;
    private JButton updateFromServerButton;
    private JButton commitToServerButton;
    private JComboBox<ServerProjectShortInfo> selectProjectComboBox;
    private JButton reloadProjectButton;
    private JComboBox updateFilterTypecomboBox;
    private JPanel networkButtonGroupPanel;
    private JLabel versionNotes;
    private JLabel showHelpDocButton;
//    private JLabel serverNoticeLabel2;
    private JButton syncServerCfgDataButton;
    private JTextArea serverNoticeArea;
    private final Project project;

    private int currentShowMsgIndex = 0;
    private List<NoticeBody> cachedNotices = new ArrayList<>();
    private final Object noticeLock = new Object();

    private JBColor[] NOTICE_COLORS = new JBColor[]{
            JBColor.BLUE,
            JBColor.RED,
            JBColor.GREEN
    };


    // 记录上一次按住alt点击的时间戳
    private long lastAltClickedTime = -1L;

    public ManageReviewCommentUI(Project project) {
        this.project = project;
        showHelpDocButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                NetworkOperationHelper.openBrowser("http://blog.codingcoder.cn/post/codereviewhelperdoc.html");
            }
        });
        showHelpDocButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    public void initUI() {
        bindButtons();
        reloadTableData();
        bindTableListeners();
        renderActions();

        timelyPullFromServer();
    }

    private void timelyPullFromServer() {

        // 支持秒级别定时任务
        CronUtil.setMatchSecond(true);
        if (CronUtil.getScheduler().isStarted()) {
            Logger.info("定时服务已经启动...");
        } else {
            Logger.info("启动定时服务...");
            CronUtil.start();
        }

        // 每5分钟执行一次定时任务
        CronUtil.schedule("0 0/5 * * * ?", (Task) () -> {
            if (GlobalConfigManager.getInstance().getGlobalConfig().isNetworkMode()) {
                NetworkOperationHelper.doGet("client/system/getSystemNotice",
                        new TypeReference<Response<List<NoticeBody>>>() {
                        },
                        notices -> {
                            synchronized (noticeLock) {
                                currentShowMsgIndex = 0;
                                cachedNotices.clear();
                                cachedNotices.addAll(Optional.ofNullable(notices).map(Response::getData).orElse(new ArrayList<>()));
                                Logger.info("通知信息拉取更新完成，当前通知数：" + cachedNotices.size());
                            }
                        }
                );
            }
        });

        // 每5s切换一次通知内容（如果有的话）
        CronUtil.schedule("0/5 * * * * ?", (Task) () -> {
            if (GlobalConfigManager.getInstance().getGlobalConfig().isNetworkMode()) {
                synchronized (noticeLock) {
                    if (!cachedNotices.isEmpty()) {
                        int size = cachedNotices.size();
                        // 如果本地显示通知索引值大于新的总通知数，置零
                        if (currentShowMsgIndex >= size) {
                            currentShowMsgIndex = 0;
                        }
                        Optional.ofNullable(cachedNotices.get(currentShowMsgIndex))
                                .map(NoticeBody::getMsg)
                                .ifPresent(s -> {
                                    if (s.length() > 120) {
                                        s = s.substring(0, 120);
                                    }
                                    serverNoticeArea.setText(s);
                                    JBColor noticeColor = NOTICE_COLORS[currentShowMsgIndex % NOTICE_COLORS.length];
                                    serverNoticeArea.setForeground(noticeColor);
                                    serverNoticeArea.setBorder(BorderFactory.createLineBorder(noticeColor));
                                });
                        currentShowMsgIndex++;
                    }
                }
            }
        });

        // 每1小时气泡提示一次
        CronUtil.schedule("0 0 0/1 * * ?", (Task) () -> {
            if (GlobalConfigManager.getInstance().getGlobalConfig().isNetworkMode()) {
                String noticeContent = "";
                synchronized (noticeLock) {
                    noticeContent = cachedNotices.stream()
                            .map(NoticeBody::getMsg)
                            .filter(StringUtils::isNotEmpty)
                            .map(s -> "· " + s)
                            .collect(Collectors.joining("<br>"));
                }
                if (StringUtils.isNotEmpty(noticeContent)) {
                    NotificationGroup notificationGroup = new NotificationGroup("CodeReviewNotification",
                            NotificationDisplayType.TOOL_WINDOW, true);
                    Notification notification = notificationGroup.createNotification("CodeReview通知提醒", ""
                            , noticeContent,
                            NotificationType.WARNING);
                    Notifications.Bus.notify(notification, ManageReviewCommentUI.this.project);
                    Logger.info("插件通知新消息弹出，通知内容：" + noticeContent);
                }
            }
        });
    }

    public void refreshTableDataShow() {
        reloadTableData();
    }

    private void reloadTableData() {
        InnerProjectCache projectCache =
                ProjectInstanceManager.getInstance().getProjectCache(ManageReviewCommentUI.this.project.getLocationHash());

        RecordColumns recordColumns = GlobalConfigManager.getInstance().getCustomConfigColumns();
        List<Column> availableColumns = recordColumns.getTableAvailableColumns();
        List<Object[]> rowDataList = new ArrayList<>();

        projectCache.getCachedComments()
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

        // 按住alt单击，弹出详情确认框
        commentTable.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent e) {
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

                Logger.info("详情确认窗口已经弹出");
                ReviewCommentDialog.show(commentInfoModel, project, Constants.CONFIRM_COMMENT);
                Logger.info("详情确认窗口已经关闭");
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
        GlobalConfigInfo globalConfig = GlobalConfigManager.getInstance().getGlobalConfig();

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
                    Messages.showMessageDialog("导入成功", "操作提示", CommonUtil.getDefaultIcon());
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                Messages.showErrorDialog("导入成功！原因：" + System.lineSeparator() + ex.getMessage(), "操作失败");
            }
        });

        exportButton.addActionListener(e -> {
            String recentSelectedFileDir = GlobalConfigManager.getInstance().getRecentSelectedFileDir();
            JFileChooser fileChooser = new JFileChooser(recentSelectedFileDir);
            fileChooser.setSelectedFile(new File("代码检视意见_" + CommonUtil.getFormattedTimeForFileName()));
            fileChooser.setFileFilter(new FileNameExtensionFilter("Excel表格(*.xlsx)", ".xlsx"));
            int saveDialog = fileChooser.showSaveDialog(fullPanel);
            if (saveDialog == JFileChooser.APPROVE_OPTION) {
                String path = fileChooser.getSelectedFile().getPath();
                if (!path.toLowerCase().endsWith(".xlsx")) {
                    path += ".xlsx";
                }

                String absoluteParentPath = new File(path).getParentFile().getAbsolutePath();
                GlobalConfigManager.getInstance().saveRecentSelectedFileDir(absoluteParentPath);

                try {
                    InnerProjectCache projectCache =
                            ProjectInstanceManager.getInstance().getProjectCache(ManageReviewCommentUI.this.project.getLocationHash());
                    ExcelResultProcessor.export(path, projectCache.getCachedComments());
                    Messages.showMessageDialog("导出成功", "操作完成", CommonUtil.getDefaultIcon());
                    Desktop.getDesktop().open(new File(absoluteParentPath));
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
        networkConfigButton.addActionListener(e -> NetworkConfigUI.showDialog());

        syncServerCfgDataButton.addActionListener(e -> {
            pullColumnConfigsFromServer();
            switchNetButtonStatus();
            Messages.showMessageDialog("操作完成", "操作完成", CommonUtil.getDefaultIcon());
        });

        reloadProjectButton.addActionListener(e -> {
            new Thread(() -> {
                try {
                    reloadProjectButton.setEnabled(false);
                    // 拉取项目列表
                    if (GlobalConfigManager.getInstance().getGlobalConfig().isNetworkMode()) {
                        NetworkOperationHelper.doGet("client/project/getMyProjects",
                                new TypeReference<Response<List<ServerProjectShortInfo>>>() {
                                },
                                serverProjectShortInfos -> {
                                    // 列表数据缓存到本地
                                    globalConfig.setCachedProjectList(serverProjectShortInfos.getData());
                                    GlobalConfigManager.getInstance().saveGlobalConfig();
                                    resetProjectSelectBox(serverProjectShortInfos.getData());
                                });
                    }
                } catch (Exception ex) {
                    Logger.error("拉取项目列表数据失败", ex);
                } finally {
                    reloadProjectButton.setEnabled(true);
                }
            }).start();
        });

        // 本地缓存的项目信息先初始化出来
        Optional.ofNullable(globalConfig.getCachedProjectList()).ifPresent(this::resetProjectSelectBox);

        // 切换项目的时候的监听方法
        selectProjectComboBox.addItemListener(e -> {
            ServerProjectShortInfo selectedItem = (ServerProjectShortInfo) selectProjectComboBox.getSelectedItem();
            Optional.ofNullable(selectedItem).ifPresent(serverProjectShortInfo -> {
                // 缓存当前选中的记录
                globalConfig.setSelectedServerProjectId(selectedItem.getProjectId());
                GlobalConfigManager.getInstance().saveGlobalConfig();
            });
        });

        // 提交本地内容到服务端
        commitToServerButton.addActionListener(e -> {
            ServerProjectShortInfo selectedProject = (ServerProjectShortInfo) selectProjectComboBox.getSelectedItem();
            if (selectedProject == null) {
                Logger.error("提交服务端失败，未选中项目");
                Messages.showErrorDialog("请先选择一个项目！", "操作错误提示");
                return;
            }
            Long projectKey = selectedProject.getProjectId();
            CommitComment commitComment = buildCommitCommentData(projectKey);

            int resp = JOptionPane.showConfirmDialog(null, "共有 " + commitComment.getComments().size()
                            + "条记录将被提交到服务端【" + selectedProject.getProjectName() + "】项目中，是否确认提交？",
                    "提交前确认",
                    JOptionPane.YES_NO_OPTION);
            if (resp != 0) {
                Logger.info("取消提交操作");
                return;
            }

            // 子线程操作防止界面卡死
            AtomicBoolean isSuccess = new AtomicBoolean(true);
            Thread workThread = new Thread(() -> {
                try {
                    commitToServerButton.setEnabled(false);
                    NetworkOperationHelper.doPost("client/comment/commitComments",
                            commitComment,
                            new TypeReference<Response<String>>() {
                            },
                            respBody -> {
                            }
                    );
                } catch (Exception ex) {
                    Logger.error("上传评审数据失败", ex);
                    isSuccess.set(false);
                } finally {
                    commitToServerButton.setEnabled(true);
                }
            });
            workThread.start();

            try {
                workThread.join();
            } catch (Exception ex) {
                Logger.error("上传评审数据失败", ex);
            }

            if (isSuccess.get()) {
                Messages.showMessageDialog("提交成功", "操作完成", CommonUtil.getDefaultIcon());
            } else {
                Messages.showErrorDialog("操作失败", "操作失败");
            }
        });

        // 从服务端拉取内容到本地
        updateFromServerButton.addActionListener(e -> {
            ServerProjectShortInfo selectedProject = (ServerProjectShortInfo) selectProjectComboBox.getSelectedItem();
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

            Long projectKey = selectedProject.getProjectId();

            // 子线程操作，防止界面卡死
            AtomicBoolean isSuccess = new AtomicBoolean(true);
            Thread workThread = new Thread(() -> {
                try {
                    updateFromServerButton.setEnabled(false);
                    ReviewQueryParams queryParams = new ReviewQueryParams();
                    queryParams.setProjectId(projectKey);
                    queryParams.setType(selectedType);
                    NetworkOperationHelper.doPost("client/comment/queryList",
                            queryParams,
                            new TypeReference<Response<List<CommentReqBody>>>() {
                            },
                            listResponse -> updateLocalData(listResponse.getData())
                    );
                } catch (Exception ex) {
                    Logger.error("查询评审信息失败", ex);
                    isSuccess.set(false);
                } finally {
                    updateFromServerButton.setEnabled(true);
                }
            });

            workThread.start();

            try {
                workThread.join();
            } catch (Exception ex) {
                Logger.error("查询评审信息失败", ex);
            }

            if (isSuccess.get()) {
                Messages.showMessageDialog("拉取成功", "操作完成", CommonUtil.getDefaultIcon());
            } else {
                Messages.showErrorDialog("操作失败", "操作失败");
            }
        });
    }

    void pullColumnConfigsFromServer() {
        if (GlobalConfigManager.getInstance().getGlobalConfig().isNetworkMode()) {
            NetworkOperationHelper.doGet("client/system/pullColumnDefines",
                    new TypeReference<Response<RecordColumns>>() {
                    },
                    recordColumnsResponse -> {
                        Optional.ofNullable(recordColumnsResponse)
                                .map(Response::getData)
                                .ifPresent(recordColumns -> {
                                    // 拉取到服务端配置信息，缓存到本地
                                    GlobalConfigManager.getInstance().saveCustomConfigColumn(recordColumns);
                                });
                    }
            );
        }
    }

    private void resetProjectSelectBox(List<ServerProjectShortInfo> serverProjectShortInfos) {
        selectProjectComboBox.removeAllItems();
        serverProjectShortInfos.forEach(serverProjectShortInfo -> selectProjectComboBox.addItem(serverProjectShortInfo));
        // 如果此前的项目仍存在，则保持选中项目不变
        Optional.ofNullable(GlobalConfigManager.getInstance().getGlobalConfig()
                .getSelectedServerProjectId()).flatMap(projId -> serverProjectShortInfos.stream()
                .filter(serverProjectShortInfo -> serverProjectShortInfo.getProjectId().equals(projId))
                .findFirst()).ifPresent(serverProjectShortInfo -> selectProjectComboBox.setSelectedItem(serverProjectShortInfo));
    }

    private void updateLocalData(List<CommentReqBody> comments) {
        try {
            if (comments != null) {
                List<ReviewComment> commentInfoModelList = comments.stream()
                        .map(comment -> {
                            ReviewComment reviewComment = new ReviewComment();
                            reviewComment.setEntityUniqueId(comment.getEntityUniqueId());
                            reviewComment.setPropValues(comment.getPropValues());
                            reviewComment.setLineRangeInfo();
                            return reviewComment;
                        }).collect(Collectors.toList());

                // 写入本地，并刷新表格显示
                InnerProjectCache projectCache =
                        ProjectInstanceManager.getInstance().getProjectCache(ManageReviewCommentUI.this.project.getLocationHash());
                projectCache.importComments(commentInfoModelList);
                CommonUtil.reloadCommentListShow(ManageReviewCommentUI.this.project);
            }
        } catch (Exception e) {
            Logger.error("更新本地表格数据失败", e);
        }
    }

    private CommitComment buildCommitCommentData(Long projectKey) {
        List<CommentReqBody> comments = generateCommitList();
        CommitComment commitComment = new CommitComment();
        commitComment.setProjectId(projectKey);
        commitComment.setComments(comments);
        return commitComment;
    }

    private List<CommentReqBody> generateCommitList() {
        // 本地内容构造成服务端需要的格式，提交服务端
        InnerProjectCache projectCache =
                ProjectInstanceManager.getInstance().getProjectCache(ManageReviewCommentUI.this.project.getLocationHash());
        List<ReviewComment> cachedComments = projectCache.getCachedComments();
        return cachedComments.stream()
                .map(reviewCommentInfoModel -> {
                    CommentReqBody comment = new CommentReqBody();
                    comment.setPropValues(reviewCommentInfoModel.getPropValues());
                    comment.setEntityUniqueId(reviewCommentInfoModel.getEntityUniqueId());
                    return comment;
                }).collect(Collectors.toList());
    }

    private void renderActions() {
        switchNetButtonStatus();
    }

    /**
     * 根据配置是否网络版本，切换相关按钮是否可用
     */
    void switchNetButtonStatus() {
        if (GlobalConfigManager.getInstance().getGlobalConfig().isNetworkMode()) {
            networkButtonGroupPanel.setVisible(true);
            versionNotes.setText("网络模式");
            // 本地缓存的项目信息先初始化出来
            Optional.ofNullable(GlobalConfigManager.getInstance().getGlobalConfig().getCachedProjectList()).ifPresent(this::resetProjectSelectBox);
            // 显示通知信息区域
            serverNoticeArea.setVisible(true);
        } else {
            networkButtonGroupPanel.setVisible(false);
            versionNotes.setText("单机模式");
            // 去掉通知信息区域
            serverNoticeArea.setVisible(false);
        }

        // 重新根据配置情况刷新下表格内容
        CommonUtil.reloadCommentListShow(ManageReviewCommentUI.this.project);
    }
}
