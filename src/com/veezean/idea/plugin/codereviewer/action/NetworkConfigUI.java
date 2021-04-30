package com.veezean.idea.plugin.codereviewer.action;

import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONUtil;
import com.veezean.idea.plugin.codereviewer.common.GlobalConfigManager;
import com.veezean.idea.plugin.codereviewer.common.InnerProjectCache;
import com.veezean.idea.plugin.codereviewer.common.ProjectInstanceManager;
import com.veezean.idea.plugin.codereviewer.model.GlobalConfigInfo;
import com.veezean.idea.plugin.codereviewer.model.Response;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

/**
 * 网络版本配置逻辑
 *
 * @author Wang Weiren
 * @since 2021/4/25
 */
public class NetworkConfigUI {

    private static final int WIDTH = 600;
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


    public NetworkConfigUI() {
        checkServerConnectionButton.addActionListener(e -> {
            serverUrlDetectResultShow.setText("测试中，请稍后...");
            String serverUrl = serverUrlField.getText();
            if (StringUtils.isEmpty(serverUrl)) {
                serverUrlDetectResultShow.setText("请先填写服务器地址！");
                return;
            }
            if (!serverUrl.endsWith("/")) {
                serverUrl += "/";
            }
            serverUrl += CHECK_SERVER_URL_PATH;
            try {
                String response = HttpUtil.get(serverUrl + "", 30000);
                Response responseBean = JSONUtil.toBean(response, Response.class);
                if (responseBean.getCode() != 0) {
                    serverUrlDetectResultShow.setText("服务器地址连接失败！");
                    setUserPwdStatus(false);
                    return;
                } else {
                    serverUrlDetectResultShow.setText("服务器连接成功！");
                    setUserPwdStatus(true);
                    return;
                }
            } catch (Exception ex) {
                serverUrlDetectResultShow.setText("服务器地址连接失败！");
                setUserPwdStatus(false);
                return;
            }
        });
        // 切换到本地版本
        localVersionRadioButton.addActionListener(e -> {
            localVersionRadioButton.setSelected(true);
            netVersionRadioButton.setSelected(false);
            switchLocalOrNetworkVersion(false);
        });
        // 切换到网络版本
        netVersionRadioButton.addActionListener(e -> {
            netVersionRadioButton.setSelected(true);
            localVersionRadioButton.setSelected(false);
            switchLocalOrNetworkVersion(true);
        });
        // 登录校验按钮
        loginCheckButton.addActionListener(e -> {
            String account = accountField.getText();
            char[] passwordChars = passwordField.getPassword();
            if (StringUtils.isEmpty(account) || ArrayUtils.isEmpty(passwordChars)) {
                loginCheckResultShow.setText("请输入账号和密码!");
                return;
            }
            String pwd = new String(passwordChars);
            String serverUrl = serverUrlField.getText();
            if (StringUtils.isEmpty(serverUrl)) {
                loginCheckResultShow.setText("请先填写服务器地址！");
                return;
            }
            if (!serverUrl.endsWith("/")) {
                serverUrl += "/";
            }
            serverUrl += CHECK_USER_PWD_PATH;
            try {
                Map<String, Object> params = new HashMap<>();
                params.put("user", account);
                params.put("pwd", pwd);
                String response = HttpUtil.get(serverUrl + "", params,30000);

                Response responseBean = JSONUtil.toBean(response, Response.class);
                if (responseBean.getCode() != 0) {
                    loginCheckResultShow.setText("用户名密码校验失败！");
                    setUserPwdStatus(false);
                    return;
                } else {
                    loginCheckResultShow.setText("用户名密码校验成功！");
                    setUserPwdStatus(true);
                    return;
                }
            } catch (Exception ex) {
                loginCheckResultShow.setText("服务器连接失败！");
                setUserPwdStatus(false);
                return;
            }
        });

    }

    private void setUserPwdStatus(boolean enable) {
        accountField.setEnabled(enable);
        passwordField.setEnabled(enable);
        loginCheckButton.setEnabled(enable);
        clickServerCheckLabel.setVisible(!enable);
    }

    private void switchLocalOrNetworkVersion(boolean netVersion) {
        serverUrlField.setEnabled(netVersion);
        checkServerConnectionButton.setEnabled(netVersion);
        accountField.setEnabled(netVersion);
        passwordField.setEnabled(netVersion);
        loginCheckButton.setEnabled(netVersion);
        clickServerCheckLabel.setVisible(netVersion);

        // 切换到net版本的时候，默认情况下先置灰用户名密码框
        if (netVersion) {
            setUserPwdStatus(false);
        }
    }

    public static void showDialog() {
        JDialog dialog = new JDialog();
        dialog.setTitle("添加评审意见");
        NetworkConfigUI networkConfigUI = new NetworkConfigUI();

        networkConfigUI.cancelButton.addActionListener(e -> dialog.dispose());

        // 保存配置
        networkConfigUI.SaveButton.addActionListener(e -> {
            GlobalConfigInfo newConfigInfo = new GlobalConfigInfo();
            newConfigInfo.setNetVersion(networkConfigUI.netVersionRadioButton.isSelected());

            String serverUrl = networkConfigUI.serverUrlField.getText();
            if (!serverUrl.endsWith("/")) {
                serverUrl += "/";
            }
            newConfigInfo.setServerAddress(serverUrl);

            newConfigInfo.setAccount(networkConfigUI.accountField.getText());
            newConfigInfo.setPwd(new String(networkConfigUI.passwordField.getPassword()));
            GlobalConfigManager.getInstance().saveGlobalConfig(newConfigInfo);

            // 保存操作后，重新刷新下管理面板的网络相关按钮动作
            // 如果打开多个idea项目实例，会有多份projectCache对象，配置数据全局共享，全部要变更下
            Map<String, InnerProjectCache> projectCacheMap = ProjectInstanceManager.getInstance().getProjectCacheMap();
            projectCacheMap.forEach((projectHashId, innerProjectCache) -> {
                innerProjectCache.getManageReviewCommentUI().switchNetButtonStatus(newConfigInfo.isNetVersion());
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
            networkConfigUI.localVersionRadioButton.setSelected(!globalConfig.isNetVersion());
            networkConfigUI.netVersionRadioButton.setSelected(globalConfig.isNetVersion());
            networkConfigUI.serverUrlField.setText(globalConfig.getServerAddress());
            networkConfigUI.accountField.setText(globalConfig.getAccount());
            networkConfigUI.passwordField.setText(globalConfig.getPwd());
        } catch (Exception e) {
            e.printStackTrace();
        }

        // 触发版本类型切换动作
        boolean netVersion = networkConfigUI.netVersionRadioButton.isSelected();
        networkConfigUI.switchLocalOrNetworkVersion(netVersion);
    }
}
