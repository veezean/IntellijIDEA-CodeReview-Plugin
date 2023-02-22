package com.veezean.idea.plugin.codereviewer.action;

import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONUtil;
import com.intellij.ide.plugins.PluginManager;
import com.intellij.openapi.extensions.PluginId;
import com.veezean.idea.plugin.codereviewer.common.GlobalConfigManager;
import com.veezean.idea.plugin.codereviewer.common.InnerProjectCache;
import com.veezean.idea.plugin.codereviewer.common.ProjectInstanceManager;
import com.veezean.idea.plugin.codereviewer.common.VersionType;
import com.veezean.idea.plugin.codereviewer.model.GlobalConfigInfo;
import com.veezean.idea.plugin.codereviewer.model.Response;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

/**
 * 网络版本配置逻辑
 *
 * @author Wang Weiren
 * @since 2021/4/25
 */
public class NetworkConfigUI extends JDialog{

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
    private JButton saveButton;
    private JButton cancelButton;
    private JPanel netVersionConfigPanel;
    private JLabel clickServerCheckLabel;
    private JPanel networkConfigJPanel;
    private JLabel pluginCurrentVersionLabel;
    private JLabel checkUpdateButton;
    private JLabel contactMeButton;
    private JLabel helpDocButton;
    private JLabel helpDocClickButton;
    private JButton modifyFieldButton;
    private JLabel fieldModifyHint;


    public NetworkConfigUI() {
        // 屏幕中心显示
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int w = (screenSize.width - WIDTH) / 2;
        int h = (screenSize.height * 95 / 100 - HEIGHT) / 2;
        setLocation(w, h);
        setModal(true);

        setContentPane(netVersionConfigPanel);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        cancelButton.addActionListener(e -> dispose());

        // 保存配置
        saveButton.addActionListener(e -> {
            GlobalConfigInfo newConfigInfo = new GlobalConfigInfo();
            newConfigInfo.setVersionType(getVersionType().getValue());

            // 网络版本的相关配置
            String serverUrl = serverUrlField.getText();
            if (StringUtils.isNotEmpty(serverUrl) && !serverUrl.endsWith("/")) {
                serverUrl += "/";
            }
            newConfigInfo.setServerAddress(serverUrl);
            newConfigInfo.setAccount(accountField.getText());
            newConfigInfo.setPwd(new String(passwordField.getPassword()));

            GlobalConfigManager.getInstance().saveGlobalConfig(newConfigInfo);

            // 保存操作后，重新刷新下管理面板的网络相关按钮动作
            // 如果打开多个idea项目实例，会有多份projectCache对象，配置数据全局共享，全部要变更下
            Map<String, InnerProjectCache> projectCacheMap = ProjectInstanceManager.getInstance().getProjectCacheMap();
            projectCacheMap.forEach((projectHashId, innerProjectCache) -> {
                innerProjectCache.getManageReviewCommentUI().switchNetButtonStatus(VersionType.getVersionType(newConfigInfo.getVersionType()));
            });

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
                    Map<String, Object> params = new HashMap<>();
                    params.put("user", account);
                    params.put("pwd", pwd);
                    String response = HttpUtil.get(finalServerUrl + "", params, 30000);

                    Response responseBean = JSONUtil.toBean(response, Response.class);
                    if (responseBean.getCode() != 0) {
                        loginCheckResultShow.setText("用户名或密码错误！");
                        setUserPwdStatus(false);
                    } else {
                        loginCheckResultShow.setText("登录检测成功！");
                        setUserPwdStatus(true);
                    }
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
                try {
                    Desktop.getDesktop().browse(URI.create("http://blog.codingcoder.cn/post/codereviewversions.html"));
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        });
        contactMeButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                try {
                    Desktop.getDesktop().browse(URI.create("http://blog.codingcoder.cn/about/"));
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        });
        helpDocButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                try {
                    Desktop.getDesktop().browse(URI.create("http://blog.codingcoder.cn/post/codereviewhelperdoc.html"));
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        });
        helpDocClickButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                try {
                    Desktop.getDesktop().browse(URI.create("http://blog.codingcoder.cn/post/codereviewhelperdoc.html"));
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        });

        String pluginVersion = PluginManager.getPlugin(PluginId.getId("com.veezean.idea.plugin.codereviewer")).getVersion();
        pluginCurrentVersionLabel.setText(pluginVersion == null ? "" : pluginVersion);

        // 点击字段定制修改按钮
        modifyFieldButton.addActionListener(e -> FieldConfigUI.showConfigUI());


        // 加载本地已有配置
        try {
            GlobalConfigInfo globalConfig = GlobalConfigManager.getInstance().getGlobalConfig();
            // 版本切换radio
            localVersionRadioButton.setSelected(globalConfig.getVersionType() == VersionType.LOCAL.getValue());
            netVersionRadioButton.setSelected(globalConfig.getVersionType() == VersionType.NETWORK.getValue());

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

    public static void showDialog() {
        NetworkConfigUI networkConfigUI = new NetworkConfigUI();
        networkConfigUI.pack();
        networkConfigUI.setVisible(true);
    }
}
