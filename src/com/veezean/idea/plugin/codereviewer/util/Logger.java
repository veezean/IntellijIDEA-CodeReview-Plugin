package com.veezean.idea.plugin.codereviewer.util;

import java.io.File;
import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.SimpleFormatter;

/**
 * <类功能简要描述>
 *
 * @author Wang Weiren
 * @since 2023/2/3
 */
public class Logger {

    private static volatile java.util.logging.Logger logger;

    static {
        logger = java.util.logging.Logger.getLogger("CodeReviewHelperPlugin");
        logger.setLevel(Level.INFO);

        File logDir = new File(System.getProperty("user.home"), ".CodeReviewHelperPlugin_");
        if (!logDir.exists() || !logDir.isDirectory()) {
            logDir.mkdirs();
        }
        File logFile = new File(logDir, "CodeReviewHelperPlugin_" + System.currentTimeMillis() + ".log");
        try {
            FileHandler fileHandler = new FileHandler(logFile.getAbsolutePath());
            fileHandler.setFormatter(new SimpleFormatter());
            logger.addHandler(fileHandler);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void info(String msg) {
        logger.info(msg);
    }
}
