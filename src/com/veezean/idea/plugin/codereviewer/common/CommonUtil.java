package com.veezean.idea.plugin.codereviewer.common;

import java.io.Closeable;
import java.io.IOException;

/**
 * <类功能简要描述>
 *
 * @author admin
 * @since 2019/10/2
 */
public class CommonUtil {

    public static void closeQuitely(Closeable closeable) {
        if (closeable == null) {
            return;
        }

        try {
            closeable.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
