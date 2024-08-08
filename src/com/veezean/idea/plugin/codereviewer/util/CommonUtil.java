package com.veezean.idea.plugin.codereviewer.util;

import cn.hutool.crypto.digest.MD5;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiJavaFile;
import com.veezean.idea.plugin.codereviewer.action.ManageReviewCommentUI;
import com.veezean.idea.plugin.codereviewer.common.InnerProjectCache;
import com.veezean.idea.plugin.codereviewer.mark.CodeCommentMarker;
import com.veezean.idea.plugin.codereviewer.service.ProjectLevelService;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.io.ByteArrayInputStream;
import java.io.Closeable;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;

/**
 * 通用工具类
 *
 * @author Veezean
 * @since 2019/10/2
 */
public class CommonUtil {

    private static final ThreadLocal<SimpleDateFormat> SDF = ThreadLocal.withInitial(() -> new SimpleDateFormat("yyyy" +
            "-MM-dd HH:mm:ss"));
    public static String md5(String original) {
        return MD5.create().digestHex(original, StandardCharsets.UTF_8);
    }

    public static String time2String(long millis) {
        return SDF.get().format(new Date(millis));
    }

    public static Date stringToDate(String dateTime) {
        try {
            return SDF.get().parse(dateTime);
        } catch (ParseException e) {
            Logger.error("date parse failed:" + dateTime);
            return new Date();
        }
    }

    /**
     * 格式化时间信息，作为文件名称中使用
     *
     * @return 格式化后的时间字符串
     */
    public static String getFormattedTimeForFileName() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        return simpleDateFormat.format(new Date());
    }


    /**
     * 静默关流处理方法
     *
     * @param closeable 可关闭流
     */
    public static void closeQuitely(Closeable closeable) {
        if (closeable == null) {
            return;
        }

        try {
            closeable.close();
        } catch (IOException e) {
            // do nothing here
        }
    }

    /**
     * 重新加载指定项目的评审信息
     *
     * @param project 待处理的项目
     */
    public synchronized static void reloadCommentListShow(Project project) {
        try {
            ToolWindow toolWindow = ToolWindowManager.getInstance(project).getToolWindow("CodeReview");
            if (toolWindow == null) {
                return;
            }
            ProjectLevelService.getService(project).getProjectCache().getManageReviewCommentUI()
                    .ifPresent(ManageReviewCommentUI::refreshTableDataShow);
        } catch (Exception ex) {
            Logger.error("重新加载指定项目的评审信息失败", ex);
        }
    }

    public static String getFileFullName(PsiFile psiFile) {
        String classPath = psiFile.getVirtualFile().getName();
        if (psiFile instanceof PsiJavaFile) {
            // 如果是java文件，则一并存储下packagename，解决
            classPath = ((PsiJavaFile) psiFile).getPackageName() + "," + classPath;
        }
        return classPath;
    }

    /**
     * 计算并返回新窗口的目标出现位置
     *
     * @param component 相对的父窗口位置
     * @param targetWindowWidth 新窗口的宽度
     * @param targetWindowHeight 新窗口的高度
     * @return
     */
    public static Point getWindowRelativePoint(JComponent component, int targetWindowWidth, int targetWindowHeight) {
        int locationX;
        int locationY;
        if (component == null) {
            // 屏幕中心显示
            Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
            locationX = (screenSize.width - targetWindowWidth) / 2;
            locationY = (screenSize.height * 95 / 100 - targetWindowHeight) / 2;
        } else {
            // 相对给定窗口进行显示
            Point locationOnScreen = component.getLocationOnScreen();
            locationX = (int) (locationOnScreen.getX() + (component.getWidth() / 2) - (targetWindowWidth / 2));
            locationY = (int) (locationOnScreen.getY() + (component.getHeight() / 2) - (targetWindowHeight / 2));
        }

        // 某些分辨率原因导致的计算到屏幕外部的情况的兜底处理，固定显示到左上角
        if (locationX <= 0) {
            locationX = 10;
        }

        if (locationY <= 0) {
            locationY = 10;
        }
        return new Point(locationX, locationY);
    }
}
