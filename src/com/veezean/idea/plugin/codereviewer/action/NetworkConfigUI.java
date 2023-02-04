package com.veezean.idea.plugin.codereviewer.action;

import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONUtil;
import com.veezean.idea.plugin.codereviewer.common.*;
import com.veezean.idea.plugin.codereviewer.model.GlobalConfigInfo;
import com.veezean.idea.plugin.codereviewer.model.Response;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;

import javax.swing.*;
import java.awt.*;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

/**
 * 网络版本配置逻辑
 *
 * @author Wang Weiren
 * @since 2021/4/25
 */
public class NetworkConfigUI {

    private static final int WIDTH = 800;
    private static final int HEIGHT = 600;
    private static final String CHECK_SERVER_URL_PATH = "check/serverConnection";
    private static final String CHECK_USER_PWD_PATH = "check/checkUserAndPwd";

    private JTextField serverUrlField;
    private JRadioButton localVersionRadioButton;
    private JRadioButton netVersionRadioButton;
    private JTextField accountField;
    private JPasswordField passwordField;
    private JButton checkServerConnectionButton;
    private JLabel serverUrlDetectResultShow;
    private JButton loginCheckButton;
    private JLabel loginCheckResultShow;
    private JButton SaveButton;
    private JButton cancelButton;
    private JPanel NetVersionConfigPanel;
    private JLabel clickServerCheckLabel;
    private JRadioButton netVersionGiteeRadioButton;
    private JPanel networkConfigJPanel;
    private JTextField giteePrivateTokenField;
    private JTextField giteeRepoOwnerField;
    private JTextField giteeRepoPathField;
    private JPanel networkGiteeConfigPanel;
    private JButton connectionTestButton;
    private JLabel giteeConnectionTestResultLabel;


    public NetworkConfigUI() {
        checkServerConnectionButton.addActionListener(e -> {
            serverUrlDetectResultShow.setText("Connecting, please wait...");
            String serverUrl = serverUrlField.getText();
            if (StringUtils.isEmpty(serverUrl)) {
                serverUrlDetectResultShow.setText("Please input server host first!");
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
                        serverUrlDetectResultShow.setText("Server connect failed!");
                        setUserPwdStatus(false);
                    } else {
                        serverUrlDetectResultShow.setText("Server connected!");
                        setUserPwdStatus(true);
                    }
                } catch (Exception ex) {
                    serverUrlDetectResultShow.setText("Server connect failed!");
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
        // 切换到Gitee网络版本
        netVersionGiteeRadioButton.addActionListener(e -> switchVersionType(VersionType.NETWORK_GITEE));

        // 登录校验按钮
        loginCheckButton.addActionListener(e -> {
            String account = accountField.getText();
            char[] passwordChars = passwordField.getPassword();
            if (StringUtils.isEmpty(account) || ArrayUtils.isEmpty(passwordChars)) {
                loginCheckResultShow.setText("Please input account and password!");
                return;
            }
            String pwd = new String(passwordChars);
            String serverUrl = serverUrlField.getText();
            if (StringUtils.isEmpty(serverUrl)) {
                loginCheckResultShow.setText("Please input server host!");
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
                    Map<String, Object> params = new HashMap<>();
                    params.put("user", account);
                    params.put("pwd", pwd);
                    String response = HttpUtil.get(finalServerUrl + "", params,30000);

                    Response responseBean = JSONUtil.toBean(response, Response.class);
                    if (responseBean.getCode() != 0) {
                        loginCheckResultShow.setText("Account or password error!");
                        setUserPwdStatus(false);
                    } else {
                        loginCheckResultShow.setText("Login check successful!");
                        setUserPwdStatus(true);
                    }
                } catch (Exception ex) {
                    loginCheckResultShow.setText("Server connect failed!");
                    setUserPwdStatus(false);
                } finally {
                    loginCheckButton.setEnabled(true);
                }
            }).start();
        });

        // gitee 连接情况检测
        connectionTestButton.addActionListener(e -> {
            String privateToken = giteePrivateTokenField.getText();
            String repoOwner = giteeRepoOwnerField.getText();
            String repoPath = giteeRepoPathField.getText();
            if (StringUtils.isEmpty(privateToken) || StringUtils.isEmpty(repoOwner) || StringUtils.isEmpty(repoPath)) {
                giteeConnectionTestResultLabel.setText("Please input token, owner and path!");
                return;
            }

            String finalServerUrlPattern = "https://gitee.com/api/v5/repos/{0}/{1}/issues?access_token={2}&state=open" +
                    "&sort=created&direction=desc&page=1&per_page=1";
            String finalServerUrl = MessageFormat.format(finalServerUrlPattern, repoOwner, repoPath, privateToken);

            // 子线程中处理，防止界面卡死
            new Thread(() -> {
                try {
                    connectionTestButton.setEnabled(false);
                    HttpResponse httpResponse = HttpHelper.get(finalServerUrl, 30000);
                    int status = httpResponse.getStatus();
                    String body = httpResponse.body();

                    if (status != 200) {
                        Response responseBean = JSONUtil.toBean(body, Response.class);
                        giteeConnectionTestResultLabel.setText(responseBean.getMessage());
                        return;
                    }

                    giteeConnectionTestResultLabel.setText("Connect successful!");


                } catch (Exception ex) {
                    giteeConnectionTestResultLabel.setText("Server connect failed!");
                    setUserPwdStatus(false);
                } finally {
                    connectionTestButton.setEnabled(true);
                }
            }).start();
        });
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
                netVersionGiteeRadioButton.setSelected(false);

                networkConfigJPanel.setVisible(true);
                networkGiteeConfigPanel.setVisible(false);
                // 切换到net版本的时候，默认情况下先置灰用户名密码框
                setUserPwdStatus(false);
                break;
            case NETWORK_GITEE:
                netVersionRadioButton.setSelected(false);
                localVersionRadioButton.setSelected(false);
                netVersionGiteeRadioButton.setSelected(true);

                networkConfigJPanel.setVisible(false);
                networkGiteeConfigPanel.setVisible(true);
                break;
            default:
                localVersionRadioButton.setSelected(true);
                netVersionRadioButton.setSelected(false);
                netVersionGiteeRadioButton.setSelected(false);

                networkConfigJPanel.setVisible(false);
                networkGiteeConfigPanel.setVisible(false);
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

        if (netVersionGiteeRadioButton.isSelected()) {
            return VersionType.NETWORK_GITEE;
        }

        // 其他情况，默认local版本
        return VersionType.LOCAL;
    }

    public static void showDialog() {
        JDialog dialog = new JDialog();
        dialog.setTitle("Add Comment");
        NetworkConfigUI networkConfigUI = new NetworkConfigUI();

        networkConfigUI.cancelButton.addActionListener(e -> dialog.dispose());

        // 保存配置
        networkConfigUI.SaveButton.addActionListener(e -> {
            GlobalConfigInfo newConfigInfo = new GlobalConfigInfo();
            newConfigInfo.setVersionType(networkConfigUI.getVersionType().getValue());

            // 网络版本的相关配置
            String serverUrl = networkConfigUI.serverUrlField.getText();
            if (!serverUrl.endsWith("/")) {
                serverUrl += "/";
            }
            newConfigInfo.setServerAddress(serverUrl);
            newConfigInfo.setAccount(networkConfigUI.accountField.getText());
            newConfigInfo.setPwd(new String(networkConfigUI.passwordField.getPassword()));

            // 网络版（Gitee）相关配置
            newConfigInfo.setGiteePrivateToken(networkConfigUI.giteePrivateTokenField.getText());
            newConfigInfo.setGiteeRepoOwner(networkConfigUI.giteeRepoOwnerField.getText());
            newConfigInfo.setGiteeRepoPath(networkConfigUI.giteeRepoPathField.getText());

            GlobalConfigManager.getInstance().saveGlobalConfig(newConfigInfo);

            // 保存操作后，重新刷新下管理面板的网络相关按钮动作
            // 如果打开多个idea项目实例，会有多份projectCache对象，配置数据全局共享，全部要变更下
            Map<String, InnerProjectCache> projectCacheMap = ProjectInstanceManager.getInstance().getProjectCacheMap();
            projectCacheMap.forEach((projectHashId, innerProjectCache) -> {
                innerProjectCache.getManageReviewCommentUI().switchNetButtonStatus(VersionType.getVersionType(newConfigInfo.getVersionType()));
            });

            // 保存后自动关闭窗口
            dialog.dispose();
        });

        // 屏幕中心显示
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int w = (screenSize.width - WIDTH) / 2;
        int h = (screenSize.height * 95 / 100 - HEIGHT) / 2;
        dialog.setLocation(w, h);

        dialog.setContentPane(networkConfigUI.NetVersionConfigPanel);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.pack();
        dialog.setVisible(true);

        // 加载本地已有配置
        try {
            GlobalConfigInfo globalConfig = GlobalConfigManager.getInstance().getGlobalConfig();
            // 版本切换radio
            networkConfigUI.localVersionRadioButton.setSelected(globalConfig.getVersionType() == VersionType.LOCAL.getValue());
            networkConfigUI.netVersionRadioButton.setSelected(globalConfig.getVersionType() == VersionType.NETWORK.getValue());
            networkConfigUI.netVersionGiteeRadioButton.setSelected(globalConfig.getVersionType() == VersionType.NETWORK_GITEE.getValue());

            // 网络版本 对应配置
            networkConfigUI.serverUrlField.setText(globalConfig.getServerAddress());
            networkConfigUI.accountField.setText(globalConfig.getAccount());
            networkConfigUI.passwordField.setText(globalConfig.getPwd());

            // 网络版本（Gitee）对应配置
            networkConfigUI.giteePrivateTokenField.setText(globalConfig.getGiteePrivateToken());
            networkConfigUI.giteeRepoOwnerField.setText(globalConfig.getGiteeRepoOwner());
            networkConfigUI.giteeRepoPathField.setText(globalConfig.getGiteeRepoPath());
        } catch (Exception e) {
            e.printStackTrace();
        }

        // 触发版本类型切换动作
        VersionType versionType = networkConfigUI.getVersionType();
        networkConfigUI.switchVersionType(versionType);
    }
}
