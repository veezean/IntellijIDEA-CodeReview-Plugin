package com.veezean.idea.plugin.codereviewer.action;

import com.veezean.idea.plugin.codereviewer.common.NetworkOperationHelper;
import com.veezean.idea.plugin.codereviewer.util.CommonUtil;
import com.veezean.idea.plugin.codereviewer.util.LanguageUtil;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.*;
import java.util.Optional;

public class UsageShowDialogUI extends JDialog {
    private JPanel contentPane;
    private JButton buttonOK;
    private JLabel usageHelpTitleLabel;
    private JTextPane usageHelpTextPane;
    private JLabel forMoreHelpLabel;
    private JLabel moreHelpClickBtn;

    private static final int WIDTH = 520;
    private static final int HEIGHT = 320;

    public UsageShowDialogUI(JComponent ideMainWindow) {
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);

        setLocation(CommonUtil.getWindowRelativePoint(ideMainWindow, WIDTH, HEIGHT));
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setModal(true);

        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        buttonOK.addActionListener(e -> dispose());

        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                dispose();
            }
        });

        // call onCancel() on ESCAPE
        contentPane.registerKeyboardAction(e -> dispose(), KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

        moreHelpClickBtn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                NetworkOperationHelper.openBrowser("https://blog.codingcoder.cn/post/codereviewhelperdoc.html");
            }
        });
        moreHelpClickBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        refreshShowLanguages();
    }


    private void refreshShowLanguages() {
        usageHelpTitleLabel.setText(LanguageUtil.getString("FIELD_UI_OPERATE_TITLE"));
        usageHelpTextPane.setText(LanguageUtil.getString("MAIN_USAGE_HINT"));

        moreHelpClickBtn.setText(LanguageUtil.getString("CONFIG_UI_CLICK_HERE_TO_SHOW"));
        forMoreHelpLabel.setText(LanguageUtil.getString("USAGE_DIALOG_FOR_MORE"));
    }

    public static void showUsageDialog(JComponent mainWindow) {
        UsageShowDialogUI dialog = new UsageShowDialogUI(mainWindow);
        dialog.pack();
        dialog.setVisible(true);
    }
}
