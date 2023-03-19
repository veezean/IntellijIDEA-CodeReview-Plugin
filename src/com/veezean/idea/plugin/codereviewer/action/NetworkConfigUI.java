package com.veezean.idea.plugin.codereviewer.action;

import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.TypeReference;
import com.intellij.ide.plugins.PluginManagerCore;
import com.intellij.openapi.extensions.PluginId;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.ui.Messages;
import com.veezean.idea.plugin.codereviewer.common.GlobalConfigManager;
import com.veezean.idea.plugin.codereviewer.common.NetworkOperationHelper;
import com.veezean.idea.plugin.codereviewer.consts.VersionType;
import com.veezean.idea.plugin.codereviewer.model.GlobalConfigInfo;
import com.veezean.idea.plugin.codereviewer.model.Response;
import com.veezean.idea.plugin.codereviewer.model.UserPwdCheckReq;
import com.veezean.idea.plugin.codereviewer.service.ProjectLevelService;
import com.veezean.idea.plugin.codereviewer.util.CommonUtil;
import com.veezean.idea.plugin.codereviewer.util.Logger;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Arrays;
import java.util.Objects;

/**
 * 网络版本配置逻辑
 *
 * @author Veezean, 公众号 @架构悟道
 * @since 2021/4/25
 */
public class NetworkConfigUI extends JDialog {

    private static final int WIDTH = 800;
    private static final int HEIGHT = 600;
    private static final String CHECK_SERVER_URL_PATH = "client/system/checkConnection";
    private static final String CHECK_USER_PWD_PATH = "client/system/checkAuth";

    private JTextField serverUrlField;
    private JRadioButton localVersionRadioButton;
    private JRadioButton netVersionRadioButton;
    private JTextField accountField;
    private JPasswordField passwordField;
    private JButton checkServerConnectionButton;
    private JLabel serverUrlDetectResultShow;
    private JButton loginCheckButton;
    private JLabel loginCheckResultShow;
    private JButton saveButton;
    private JButton cancelButton;
    private JPanel netVersionConfigPanel;
    private JLabel clickServerCheckLabel;
    private JPanel networkConfigJPanel;
    private JLabel pluginCurrentVersionLabel;
    private JLabel checkUpdateButton;
    private JLabel contactMeButton;
    private JLabel helpDocButton;
    private JLabel serverDeployHelpButton;
    private JButton modifyFieldButton;
    private JLabel fieldModifyHint;

    /**
     * 插件配置界面
     *
     * @author Veezean, 公众号 @架构悟道
     * @date 2023/3/12
     */
    public NetworkConfigUI(JComponent ideMainWindow) {

        setLocation(CommonUtil.getWindowRelativePoint(ideMainWindow, WIDTH, HEIGHT));
        setModal(true);

        setContentPane(netVersionConfigPanel);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        cancelButton.addActionListener(e -> dispose());

        // 保存配置
        saveButton.addActionListener(e -> {
            GlobalConfigInfo newConfigInfo = GlobalConfigManager.getInstance().getGlobalConfig();
            newConfigInfo.setVersionType(getVersionType().getValue());

            try {
                // 网络版本的相关配置
                String serverUrl = serverUrlField.getText();
                if (StringUtils.isNotEmpty(serverUrl) && !serverUrl.endsWith("/")) {
                    serverUrl += "/";
                }
                newConfigInfo.setServerAddress(serverUrl);
                newConfigInfo.setAccount(accountField.getText());
                newConfigInfo.setPwd(new String(passwordField.getPassword()));

                GlobalConfigManager.getInstance().saveGlobalConfig();

                // 根据切换的情况重置下字段定义
                resetColumnCaches();

                // 保存操作后，重新刷新下管理面板的网络相关按钮动作
                // 如果打开多个idea项目实例，会有多份projectCache对象，配置数据全局共享，全部要变更下
                Arrays.stream(ProjectManager.getInstance().getOpenProjects()).forEach(project -> {
                    ProjectLevelService.getService(project).getProjectCache().getManageReviewCommentUI()
                            .ifPresent(manageUI -> {
                                manageUI.pullColumnConfigsFromServer();
                                manageUI.switchNetButtonStatus();
                            });
                });
            } catch (Exception ex) {
                Logger.error("设置失败", ex);
                Messages.showErrorDialog("设置失败！原因：" + System.lineSeparator() + ex.getMessage(),
                        "操作失败");
                return;
            }

            // 保存后自动关闭窗口
            dispose();
        });

        checkServerConnectionButton.addActionListener(e -> {
            serverUrlDetectResultShow.setText("连接中，请稍等...");
            String serverUrl = serverUrlField.getText();
            if (StringUtils.isEmpty(serverUrl)) {
                serverUrlDetectResultShow.setText("请先输入服务端地址");
                return;
            }
            if (!serverUrl.endsWith("/")) {
                serverUrl += "/";
            }
            String finalServerUrl = serverUrl + CHECK_SERVER_URL_PATH;
            // 子线程中处理，防止界面卡死
            new Thread(() -> {
                try {
                    checkServerConnectionButton.setEnabled(false);
                    String response = HttpUtil.get(finalServerUrl + "", 30000);
                    Response responseBean = JSONUtil.toBean(response, Response.class);
                    if (responseBean.getCode() != 0) {
                        serverUrlDetectResultShow.setText("服务连接失败！");
                        setUserPwdStatus(false);
                    } else {
                        serverUrlDetectResultShow.setText("服务连接成功！");
                        setUserPwdStatus(true);
                    }
                } catch (Exception ex) {
                    serverUrlDetectResultShow.setText("服务连接失败！");
                    setUserPwdStatus(false);
                } finally {
                    checkServerConnectionButton.setEnabled(true);
                }
            }).start();
        });
        // 切换到本地版本
        localVersionRadioButton.addActionListener(e -> switchVersionType(VersionType.LOCAL));
        // 切换到网络版本
        netVersionRadioButton.addActionListener(e -> switchVersionType(VersionType.NETWORK));

        // 登录校验按钮
        loginCheckButton.addActionListener(e -> {
            String account = accountField.getText();
            char[] passwordChars = passwordField.getPassword();
            if (StringUtils.isEmpty(account) || ArrayUtils.isEmpty(passwordChars)) {
                loginCheckResultShow.setText("请输入用户名和密码！");
                return;
            }
            String pwd = new String(passwordChars);
            String serverUrl = serverUrlField.getText();
            if (StringUtils.isEmpty(serverUrl)) {
                loginCheckResultShow.setText("请先输入服务地址！");
                return;
            }
            if (!serverUrl.endsWith("/")) {
                serverUrl += "/";
            }
            String finalServerUrl = serverUrl + CHECK_USER_PWD_PATH;
            // 子线程中处理，防止界面卡死
            new Thread(() -> {
                try {
                    loginCheckButton.setEnabled(false);
                    UserPwdCheckReq pwdCheckReq = new UserPwdCheckReq();
                    pwdCheckReq.setAccount(account);
                    pwdCheckReq.setPassword(CommonUtil.md5(pwd));
                    NetworkOperationHelper.doPost(finalServerUrl,
                            pwdCheckReq,
                            new TypeReference<Response<String>>() {
                            },
                            responseBean -> {
                                if (responseBean.getCode() != 0) {
                                    loginCheckResultShow.setText("用户名或密码错误！");
                                    setUserPwdStatus(false);
                                } else {
                                    loginCheckResultShow.setText("登录检测成功！");
                                    setUserPwdStatus(true);
                                }
                            }
                    );
                } catch (Exception ex) {
                    loginCheckResultShow.setText("服务连接失败！");
                    setUserPwdStatus(false);
                } finally {
                    loginCheckButton.setEnabled(true);
                }
            }).start();
        });

        checkUpdateButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                NetworkOperationHelper.openBrowser("http://blog.codingcoder.cn/post/codereviewversions.html");
            }
        });
        checkUpdateButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        contactMeButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                NetworkOperationHelper.openBrowser("http://blog.codingcoder.cn/about/");
            }
        });
        contactMeButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        helpDocButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                NetworkOperationHelper.openBrowser("http://blog.codingcoder.cn/post/codereviewhelperdoc.html");
            }
        });
        helpDocButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        serverDeployHelpButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                NetworkOperationHelper.openBrowser("http://blog.codingcoder.cn/post/codereviewserverdeploydoc.html");
            }
        });
        serverDeployHelpButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        String pluginVersion =
                Objects.requireNonNull(PluginManagerCore.getPlugin(PluginId.getId("com.veezean.idea.plugin" +
                        ".codereviewer"))).getVersion();
        pluginCurrentVersionLabel.setText(pluginVersion == null ? "" : pluginVersion);

        // 点击字段定制修改按钮
        modifyFieldButton.addActionListener(e -> FieldConfigUI.showConfigUI(NetworkConfigUI.this.getRootPane()));

        // 加载本地已有配置
        try {
            GlobalConfigInfo globalConfig = GlobalConfigManager.getInstance().getGlobalConfig();
            // 版本切换radio
            localVersionRadioButton.setSelected(!globalConfig.isNetworkMode());
            netVersionRadioButton.setSelected(globalConfig.isNetworkMode());

            // 网络版本 对应配置
            serverUrlField.setText(globalConfig.getServerAddress());
            accountField.setText(globalConfig.getAccount());
            passwordField.setText(globalConfig.getPwd());
        } catch (Exception e) {
            e.printStackTrace();
        }

        // 触发版本类型切换动作
        VersionType versionType = getVersionType();
        switchVersionType(versionType);
    }

    private void resetColumnCaches() {
        GlobalConfigManager.getInstance().resetColumnCaches();
    }

    private void setUserPwdStatus(boolean enable) {
        accountField.setEnabled(enable);
        passwordField.setEnabled(enable);
        loginCheckButton.setEnabled(enable);
        clickServerCheckLabel.setVisible(!enable);
    }

    private void switchVersionType(VersionType versionType) {
        switch (versionType) {
            case NETWORK:
                netVersionRadioButton.setSelected(true);
                localVersionRadioButton.setSelected(false);

                networkConfigJPanel.setVisible(true);
                // 切换到net版本的时候，默认情况下先置灰用户名密码框
                setUserPwdStatus(false);

                // 网络版本不允许本地修改字段配置
                modifyFieldButton.setEnabled(false);
                fieldModifyHint.setVisible(true);
                break;
            default:
                localVersionRadioButton.setSelected(true);
                netVersionRadioButton.setSelected(false);

                networkConfigJPanel.setVisible(false);

                // 本地版本允许本地修改字段配置
                modifyFieldButton.setEnabled(true);
                fieldModifyHint.setVisible(false);
                break;
        }
    }

    private VersionType getVersionType() {
        if (localVersionRadioButton.isSelected()) {
            return VersionType.LOCAL;
        }

        if (netVersionRadioButton.isSelected()) {
            return VersionType.NETWORK;
        }

        // 其他情况，默认local版本
        return VersionType.LOCAL;
    }

    public static void showDialog(JComponent mainWindow) {
        NetworkConfigUI networkConfigUI = new NetworkConfigUI(mainWindow);
        networkConfigUI.pack();
        networkConfigUI.setVisible(true);
    }
}
