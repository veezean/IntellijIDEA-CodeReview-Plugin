package com.veezean.idea.plugin.codereviewer.util;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.util.IconLoader;

import javax.swing.*;

/**
 * <类功能简要描述>
 *
 * @author Wang Weiren
 * @since 2024/5/11
 */
public final class IconCollections {

    public static final Icon clear = IconLoader.getIcon("/icons/clear.svg");
    public static final Icon delete = IconLoader.getIcon("/icons/delete.svg");
    public static final Icon importFile = IconLoader.getIcon("/icons/import.svg");
    public static final Icon exportFile = IconLoader.getIcon("/icons/export.svg");
    public static final Icon settings = IconLoader.getIcon("/icons/settings.svg");
    public static final Icon help = IconLoader.getIcon("/icons/help.svg");
    public static final Icon personal_settings_local = IconLoader.getIcon("/icons/personal_config_local.svg");
    public static final Icon personal_settings_server = IconLoader.getIcon("/icons/personal_config_server.svg");

    public static final Icon server_config_sync = IconLoader.getIcon("/icons/server_cfg_sync.svg");
    public static final Icon server_open_web = IconLoader.getIcon("/icons/server_web_open.svg");
    public static final Icon server_commit = IconLoader.getIcon("/icons/server_commit.svg");
    public static final Icon server_download = IconLoader.getIcon("/icons/server_download.svg");


    public static final Icon warning = IconLoader.getIcon("/icons/warning.svg");
    public static final Icon success = IconLoader.getIcon("/icons/success.svg");
    public static final Icon failed = IconLoader.getIcon("/icons/failed.svg");

    /**
     * 代码区域左侧标记图标
     */
    public static final Icon leftMarkIcon = AllIcons.Debugger.Db_verified_no_suspend_field_breakpoint;
    /**
     * 插件在工具栏中显示的时候的图标，必须13*13，否则会报错
     */
    public static final Icon toolWindowIcon = AllIcons.Toolwindows.ToolWindowCommander;
}
