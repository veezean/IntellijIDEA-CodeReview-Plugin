package com.veezean.idea.plugin.codereviewer.common;

import com.veezean.idea.plugin.codereviewer.util.Logger;

import java.io.File;
import java.util.concurrent.atomic.AtomicReference;

public class LocalDirUtil {
    public static final AtomicReference<String> localBaseDir;
    static {
        String dirPath;
        try {
            dirPath = System.getProperty("user.home");
        } catch (Exception e) {
            e.printStackTrace();
            dirPath = new File("").getAbsolutePath();
        }
        localBaseDir = new AtomicReference<>(dirPath);
    }

    public static String getBaseDir() {
        return localBaseDir.get();
    }

    public static void changeBaseDir(String baseDir) {
        localBaseDir.set(baseDir);

        Logger.changeLogDir();
    }
}
