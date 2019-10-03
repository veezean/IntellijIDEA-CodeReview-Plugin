package com.veezean.idea.plugin.codereviewer.common;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * <类功能简要描述>
 *
 * @author Wang Weiren
 * @since 2019/9/30
 */
public class DateTimeUtil {
    private static final ThreadLocal<SimpleDateFormat> SDF = ThreadLocal.withInitial(() -> new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));
    public static String time2String(long millis) {
        return SDF.get().format(new Date(millis));
    }

    public static String getFormattedTimeForFileName() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        return simpleDateFormat.format(new Date());
    }
}
