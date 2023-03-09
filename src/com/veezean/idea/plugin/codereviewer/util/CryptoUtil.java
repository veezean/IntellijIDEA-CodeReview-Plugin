package com.veezean.idea.plugin.codereviewer.util;

import cn.hutool.crypto.digest.MD5;

import java.nio.charset.StandardCharsets;

/**
 * <类功能简要描述>
 *
 * @author Wang Weiren
 * @since 2023/3/8
 */
public class CryptoUtil {

    public static String md5(String original) {
        return MD5.create().digestHex(original, StandardCharsets.UTF_8);
    }
}
