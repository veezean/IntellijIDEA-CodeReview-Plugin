package com.veezean.idea.plugin.codereviewer.common;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * 日期时间处理工具类
 *
 * @author Wang Weiren
 * @since 2019/9/30
 */
public class DateTimeUtil {
    private static final ThreadLocal<SimpleDateFormat> SDF = ThreadLocal.withInitial(() -> new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));
    public static String time2String(long millis) {
        return SDF.get().format(new Date(millis));
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
     * 将数字时间转换为年月时间格式
     *
     * @param numberValue 数字时间
     * @return 格式化后的时间字符串
     */
    public static String formatNumberYearMonth(double numberValue) {
        int intValue = (int) numberValue;
        Calendar calendar = new GregorianCalendar(1900, 0, -1);
        calendar.add(Calendar.DATE, intValue);
        Date time = calendar.getTime();
        return SDF.get().format(time);
    }
}
