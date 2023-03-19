package com.veezean.idea.plugin.codereviewer.action;

import cn.hutool.json.JSONUtil;
import com.veezean.idea.plugin.codereviewer.common.GlobalConfigManager;
import com.veezean.idea.plugin.codereviewer.common.NetworkOperationHelper;
import com.veezean.idea.plugin.codereviewer.model.RecordColumns;
import com.veezean.idea.plugin.codereviewer.util.CommonUtil;
import com.veezean.idea.plugin.codereviewer.util.LanguageUtil;
import com.veezean.idea.plugin.codereviewer.util.Logger;
import org.jetbrains.debugger.ObjectProperty;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.*;
import java.util.Optional;

/**
 * 字段配置界面
 *
 * @author Veezean, 公众号 @架构悟道
 * @date 2023/3/12
 */
public class FieldConfigUI extends JDialog {
    private static final int WIDTH = 800;
    private static final int HEIGHT = 800;

    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JTextArea fieldConfigTextArea;
    private JLabel restoreToDefault;
    private JLabel showHelpBtn;
    private JLabel titleLabel;
    private JPanel operatePanel;
    private JLabel detailDocLabel;
    private JTextArea shortHelpArea;
    private JPanel configContentPanel;

    public FieldConfigUI(JComponent ideMainWindow) {

        setLocation(CommonUtil.getWindowRelativePoint(ideMainWindow, WIDTH, HEIGHT));
        setSize(WIDTH, HEIGHT);

        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);

        buttonOK.addActionListener(e -> onOK());

        buttonCancel.addActionListener(e -> onCancel());

        // call onCancel() when cross is clicked
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });

        // call onCancel() on ESCAPE
        contentPane.registerKeyboardAction(e -> onCancel(), KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

        // 加载当前配置信息
        RecordColumns recordColumns = GlobalConfigManager.getInstance().getCustomConfigColumns();
        fieldConfigTextArea.setText(JSONUtil.toJsonPrettyStr(recordColumns));

        restoreToDefault.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int resp = JOptionPane.showConfirmDialog(contentPane, LanguageUtil.getString("ALERT_CONFIRM_CONTENT"),
                        LanguageUtil.getString("ALERT_TITLE_CONFIRM"),
                        JOptionPane.OK_CANCEL_OPTION);
                if (resp != 0) {
                    Logger.info("取消重置配置操作...");
                    return;
                }

                RecordColumns systemDefaultColumns = GlobalConfigManager.getInstance().getSystemDefaultColumns();
                GlobalConfigManager.getInstance().saveCustomConfigColumn(systemDefaultColumns);
                fieldConfigTextArea.setText(JSONUtil.toJsonPrettyStr(systemDefaultColumns));
            }
        });
        restoreToDefault.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        showHelpBtn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                NetworkOperationHelper.openBrowser("http://blog.codingcoder.cn/post/codereviewfieldmodifyhelper.html");
            }
        });
        showHelpBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));


        refreshShowLanguages();
    }

    private void onOK() {
        // 更新自定义字段
        RecordColumns recordColumns = JSONUtil.toBean(fieldConfigTextArea.getText(), RecordColumns.class);
        GlobalConfigManager.getInstance().saveCustomConfigColumn(recordColumns);

        dispose();
    }

    private void onCancel() {
        dispose();
    }

    public static void showConfigUI(JComponent rootPanel) {
        FieldConfigUI dialog = new FieldConfigUI(rootPanel);
        dialog.pack();
        dialog.setVisible(true);
    }

    private void refreshShowLanguages() {
        titleLabel.setText(LanguageUtil.getString("FIELD_UI_TITLE"));
        detailDocLabel.setText(LanguageUtil.getString("FIELD_UI_HELPDOC_LABEL"));
        showHelpBtn.setText(LanguageUtil.getString("CONFIG_UI_CLICK_HERE_TO_SHOW"));
        shortHelpArea.setText(LanguageUtil.getString("FIELD_UI_SHORT_INFO"));
        restoreToDefault.setText(LanguageUtil.getString("FIELD_RESET_TO_DEFAULT"));

        Optional.ofNullable(operatePanel.getBorder())
                .filter(border -> border instanceof TitledBorder)
                .map(border -> (TitledBorder)border)
                .ifPresent(titledBorder -> {
                    titledBorder.setTitle(LanguageUtil.getString("FIELD_UI_OPERATE_TITLE"));
                });
        Optional.ofNullable(configContentPanel.getBorder())
                .filter(border -> border instanceof TitledBorder)
                .map(border -> (TitledBorder)border)
                .ifPresent(titledBorder -> {
                    titledBorder.setTitle(LanguageUtil.getString("FIELD_UI_CONFIG_TITLE"));
                });
    }

}
