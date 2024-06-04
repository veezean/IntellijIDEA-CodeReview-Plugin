package com.veezean.idea.plugin.codereviewer.action;

import com.veezean.idea.plugin.codereviewer.util.CommonUtil;
import com.veezean.idea.plugin.codereviewer.util.LanguageUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class ShowSnapshotUI extends JDialog {
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;

    private static final int WIDTH = 1400;
    private static final int HEIGHT = 960;

    public ShowSnapshotUI(JComponent ideMainWindow) {
        setContentPane(contentPane);
        getRootPane().setDefaultButton(buttonOK);

        int ideaWidth = ideMainWindow.getWidth();
        int dialogWidth = ideaWidth - 100;
        if (dialogWidth < WIDTH) {
            dialogWidth = WIDTH;
        }
        int ideaHeight = ideMainWindow.getHeight();
        int dialogHeight = ideaHeight - 100;
        if (dialogHeight < HEIGHT) {
            dialogHeight = HEIGHT;
        }
        setLocation(CommonUtil.getWindowRelativePoint(ideMainWindow, dialogWidth, dialogHeight));
        setPreferredSize(new Dimension(dialogWidth, dialogHeight));
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

        refreshShowLanguages();
    }

    private void onOK() {
        // add your code here
        dispose();
    }

    private void onCancel() {
        // add your code here if necessary
        dispose();
    }


    private void refreshShowLanguages() {
        // TODO
    }

    public static void showSnapshotUI(JComponent mainWindow) {
        ShowSnapshotUI dialog = new ShowSnapshotUI(mainWindow);
        dialog.pack();
        dialog.setVisible(true);
    }
}
