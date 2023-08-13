package com.veezean.idea.plugin.codereviewer.action;

import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.TypeReference;
import com.intellij.ide.plugins.PluginManagerCore;
import com.intellij.openapi.extensions.PluginId;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.ui.Messages;
import com.veezean.idea.plugin.codereviewer.common.CodeReviewException;
import com.veezean.idea.plugin.codereviewer.common.GlobalConfigManager;
import com.veezean.idea.plugin.codereviewer.common.NetworkOperationHelper;
import com.veezean.idea.plugin.codereviewer.consts.LanguageType;
import com.veezean.idea.plugin.codereviewer.consts.VersionType;
import com.veezean.idea.plugin.codereviewer.model.*;
import com.veezean.idea.plugin.codereviewer.service.ProjectLevelService;
import com.veezean.idea.plugin.codereviewer.util.CommonUtil;
import com.veezean.idea.plugin.codereviewer.util.LanguageUtil;
import com.veezean.idea.plugin.codereviewer.util.Logger;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

/**
 * 网络版本配置逻辑
 *
 * @author Veezean
 * @since 2021/4/25
 */
public class NetworkConfigUI extends JDialog {

    private static final int WIDTH = 1200;
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
    private JPanel configPanel;
    private JLabel clickServerCheckLabel;
    private JPanel networkConfigJPanel;
    private JLabel pluginCurrentVersionLabel;
    private JLabel checkUpdateButton;
    private JLabel contactMeButton;
    private JLabel helpDocButton;
    private JLabel serverDeployHelpButton;
    private JButton modifyFieldButton;
    private JLabel fieldModifyHint;
    private JRadioButton englishRadioButton;
    private JRadioButton chineseRadioButton;
    private JLabel languageSetLabel;
    private JLabel settingsTitleLable;
    private JLabel versionSwitchLabel;
    private JLabel fieldCustomizeLabel;
    private JLabel serverAddressLabel;
    private JLabel loginAccountLabel;
    private JLabel loginPwdLabel;
    private JLabel serverModeHint1Label;
    private JLabel serverModeHint2Label;
    private JPanel versionPanel;
    private JLabel versionCodeLabel;
    private JLabel usageHelpLabel;
    private JLabel feedbackLabel;
    private JButton openLogDirBtn;
    private JLabel problemFeedbackLabel;

    // 网络版，当前账号对应用户信息
    private ValuePair currentUserInfo;

    /**
     * 插件配置界面
     *
     * @author Veezean
     * @date 2023/3/12
     */
    public NetworkConfigUI(JComponent ideMainWindow) {
        // 初始化操作
        preInit(ideMainWindow);

        // 最后初始化操作
        postInit();
    }

    private void preInit(JComponent ideMainWindow) {

        setLocation(CommonUtil.getWindowRelativePoint(ideMainWindow, WIDTH, HEIGHT));
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setModal(true);

        setContentPane(configPanel);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        cancelButton.addActionListener(e -> dispose());

        // 保存配置
        saveButton.addActionListener(e -> {
            GlobalConfigInfo newConfigInfo = GlobalConfigManager.getInstance().getGlobalConfig();
            newConfigInfo.setLanguage(getLanguageType().getValue());
            newConfigInfo.setVersionType(getVersionType().getValue());

            try {
                if (VersionType.NETWORK.getValue() == getVersionType().getValue()) {
                    if (this.currentUserInfo == null) {
                        throw new CodeReviewException("网络版本请先检测账号密码是否正确");
                    }
                    newConfigInfo.setCurrentUserInfo(this.currentUserInfo);
                }
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
                                manageUI.changeLanguageEvent();
                            });
                });
            } catch (Exception ex) {
                Logger.error("设置失败", ex);
                Messages.showErrorDialog(LanguageUtil.getString("ALERT_CONTENT_FAILED") + System.lineSeparator() + ex.getMessage(),
                        LanguageUtil.getString("ALERT_TITLE_FAILED"));
                return;
            }

            // 保存后自动关闭窗口
            dispose();
        });

        checkServerConnectionButton.addActionListener(e -> {
            serverUrlDetectResultShow.setText(LanguageUtil.getString("CONFIG_UI_DETECHTING_HINT"));
            String serverUrl = serverUrlField.getText();
            if (StringUtils.isEmpty(serverUrl)) {
                serverUrlDetectResultShow.setText(LanguageUtil.getString("CONFIG_UI_INPUT_SERVER_HINT"));
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
                        serverUrlDetectResultShow.setText(LanguageUtil.getString("CONFIG_UI_SERVER_CONNNECT_FAILED_HINT"));
                        setUserPwdStatus(false);
                    } else {
                        serverUrlDetectResultShow.setText(LanguageUtil.getString("CONFIG_UI_SERVER_CONNNECT_SUCC_HINT"));
                        setUserPwdStatus(true);
                    }
                } catch (Exception ex) {
                    serverUrlDetectResultShow.setText(LanguageUtil.getString("CONFIG_UI_SERVER_CONNNECT_FAILED_HINT"));
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
                loginCheckResultShow.setText(LanguageUtil.getString("CONFIG_UI_INPUT_ACCOUT_PWD"));
                return;
            }
            String pwd = new String(passwordChars);
            String serverUrl = serverUrlField.getText();
            if (StringUtils.isEmpty(serverUrl)) {
                loginCheckResultShow.setText(LanguageUtil.getString("CONFIG_UI_INPUT_SERVER_ADDRESS"));
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
                            new TypeReference<Response<UserPwdCheckRespBody>>() {
                            },
                            responseBean -> {
                                setUserPwdStatus(true);
                                if (!responseBean.getData().isPass()) {
                                    loginCheckResultShow.setText(LanguageUtil.getString("CONFIG_UI_PWD_ERROR"));
                                } else {
                                    this.currentUserInfo = responseBean.getData().getUserInfo();
                                    loginCheckResultShow.setText(LanguageUtil.getString(
                                            "CONFIG_UI_LOGIN_SUCC") + "(" + this.currentUserInfo + ")");
                                }
                            }
                    );
                } catch (Exception ex) {
                    loginCheckResultShow.setText(LanguageUtil.getString("CONFIG_UI_SERVER_CONNNECT_FAILED_HINT"));
                    setUserPwdStatus(true);
                } finally {
                    loginCheckButton.setEnabled(true);
                }
            }).start();
        });

        checkUpdateButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                NetworkOperationHelper.openBrowser("https://blog.codingcoder.cn/post/codereviewversions.html");
            }
        });
        checkUpdateButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        contactMeButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                NetworkOperationHelper.openBrowser("https://blog.codingcoder.cn/about/");
            }
        });
        contactMeButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        helpDocButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                NetworkOperationHelper.openBrowser("https://blog.codingcoder.cn/post/codereviewhelperdoc.html");
            }
        });
        helpDocButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        serverDeployHelpButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                NetworkOperationHelper.openBrowser("https://blog.codingcoder.cn/post/codereviewserverdeploydoc.html");
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

        englishRadioButton.addActionListener(e -> {
            changeLanguageEvent(LanguageType.ENGLISH);
        });
        chineseRadioButton.addActionListener(e -> {
            changeLanguageEvent(LanguageType.CHINESE);
        });

        openLogDirBtn.addActionListener(e -> {
            File logDir = Logger.getLogDir();
            try {
                Desktop.getDesktop().open(logDir);
            } catch (IOException ex) {
                Logger.error("打开日志目录失败", ex);
            }
        });
    }

    private void postInit() {
        changeLanguageEvent(LanguageType.languageType(GlobalConfigManager.getInstance().getGlobalConfig().getLanguage()));
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

    private LanguageType getLanguageType() {
        if (englishRadioButton.isSelected()) {
            return LanguageType.ENGLISH;
        }
        if (chineseRadioButton.isSelected()) {
            return LanguageType.CHINESE;
        }
        // 其余情况，默认English兜底
        return LanguageType.ENGLISH;
    }

    private void changeLanguageEvent(LanguageType languageType) {
        if (LanguageType.CHINESE.equals(languageType)) {
            chineseRadioButton.setSelected(true);
            englishRadioButton.setSelected(false);
        } else {
            chineseRadioButton.setSelected(false);
            englishRadioButton.setSelected(true);
        }
        // 语言设置的值，设置后立即生效
        GlobalConfigManager.getInstance().getGlobalConfig().setLanguage(languageType.getValue());
        // 当前界面显示语言刷新下
        refreshShowLanguages(languageType);
    }

    private void refreshShowLanguages(LanguageType languageType) {
        languageSetLabel.setText(LanguageUtil.getString(languageType, "CONFIG_UI_LANGUAGE_SET"));
        settingsTitleLable.setText(LanguageUtil.getString(languageType, "CONFIG_UI_TITLE_SETTING"));
        versionSwitchLabel.setText(LanguageUtil.getString(languageType, "CONFIG_UI_VERSION_SET"));
        localVersionRadioButton.setText(LanguageUtil.getString(languageType, "CONFIG_UI_LOCAL_VERSION_RADIO"));
        netVersionRadioButton.setText(LanguageUtil.getString(languageType, "CONFIG_UI_NETWORK_VERSION_RADIO"));
        fieldCustomizeLabel.setText(LanguageUtil.getString(languageType, "CONFIG_UI_FIELD_CONFIG_LABEL"));
        modifyFieldButton.setText(LanguageUtil.getString(languageType, "CONFIG_UI_FIELD_CHANGE_BUTTON"));
        fieldModifyHint.setText(LanguageUtil.getString(languageType, "CONFIG_UI_FIELD_CONFIG_HINT_LABEL"));
        serverAddressLabel.setText(LanguageUtil.getString(languageType, "CONFIG_UI_SERVER_ADDRESS_LABEL"));
        checkServerConnectionButton.setText(LanguageUtil.getString(languageType, "CONFIG_UI_CONN_TEST_BUTTON"));
        loginAccountLabel.setText(LanguageUtil.getString(languageType, "CONFIG_UI_LOGIN_ACCOUNT_LABEL"));
        loginPwdLabel.setText(LanguageUtil.getString(languageType, "CONFIG_UI_LOGIN_PWD_LABEL"));
        clickServerCheckLabel.setText(LanguageUtil.getString(languageType, "CONFIG_UI_TEST_CONN_HINT_LABEL"));
        serverModeHint1Label.setText(LanguageUtil.getString(languageType, "CONFIG_UI_SERVER_MODEL_HINT1"));
        serverModeHint2Label.setText(LanguageUtil.getString(languageType, "CONFIG_UI_SERVER_MODEL_HINT2"));
        serverDeployHelpButton.setText(LanguageUtil.getString(languageType, "CONFIG_UI_SERVER_MODEL_CLICK_LABEL"));
        loginCheckButton.setText(LanguageUtil.getString(languageType, "CONFIG_UI_TEST_LOGIN_BUTTON"));


        checkUpdateButton.setText(LanguageUtil.getString(languageType, "CONFIG_UI_CHECK_UPDATE_LABEL"));
        contactMeButton.setText(LanguageUtil.getString(languageType, "CONFIG_UI_CLICK_FEEDBACK_LABEL"));
        helpDocButton.setText(LanguageUtil.getString(languageType, "CONFIG_UI_CLICK_HERE_TO_SHOW"));
        feedbackLabel.setText(LanguageUtil.getString(languageType, "CONFIG_UI_ADVICE_LABEL"));
        usageHelpLabel.setText(LanguageUtil.getString(languageType, "CONFIG_UI_HELP_DOC_LABEL"));
        versionCodeLabel.setText(LanguageUtil.getString(languageType, "CONFIG_UI_VERSION_CODE_LABEL"));

        openLogDirBtn.setText(LanguageUtil.getString(languageType, "OPEN_LOCAL_LOG_DIR"));
        problemFeedbackLabel.setText(LanguageUtil.getString(languageType, "PROBLEM_FEEDBACK_HINT"));

        Optional.ofNullable(versionPanel.getBorder())
                .filter(border -> border instanceof TitledBorder)
                .map(border -> (TitledBorder)border)
                .ifPresent(titledBorder -> {
                    titledBorder.setTitle(LanguageUtil.getString(languageType, "CONFIG_UI_VERSION_PANEL_TITLE"));
                });

        saveButton.setText(LanguageUtil.getString("BUTTON_SAVE"));
        cancelButton.setText(LanguageUtil.getString("BUTTON_CANCEL"));
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
