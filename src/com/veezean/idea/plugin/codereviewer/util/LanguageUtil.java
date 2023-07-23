package com.veezean.idea.plugin.codereviewer.util;

import cn.hutool.core.io.FileUtil;
import com.veezean.idea.plugin.codereviewer.common.GlobalConfigManager;
import com.veezean.idea.plugin.codereviewer.consts.LanguageType;
import org.apache.commons.lang.StringUtils;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;

/**
 * <类功能简要描述>
 *
 * @author Veezean
 * @since 2023/3/19
 */
public class LanguageUtil {
    private static Map<String, String> englishProperties = new ConcurrentHashMap<>();
    private static Map<String, String> chineseProperties = new ConcurrentHashMap<>();

    private static void loadLanguageProperties() {
        loadLanguageProperties("language_en.properties", (k, v) -> englishProperties.put(k, v));
        loadLanguageProperties("language_zh.properties", (k, v) -> chineseProperties.put(k, v));
    }

    private static synchronized void loadLanguageProperties(String fileName, BiConsumer<String, String> consumer) {
        URL url = LanguageUtil.class.getClassLoader().getResource(fileName);
        String fileContent = FileUtil.readString(url, StandardCharsets.UTF_8.name());
        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(fileContent.getBytes())) {
            Properties properties = new Properties();
            properties.load(inputStream);
            properties.forEach((name, value) -> {
                consumer.accept(String.valueOf(name), String.valueOf(value));
            });
        } catch (Exception e) {
            Logger.error("读取语言配置文件失败", e);
        }
    }

    public static String getString(LanguageType languageType, String key) {
        if (chineseProperties.isEmpty() || englishProperties.isEmpty()) {
            loadLanguageProperties();
        }

        String value;
        if (LanguageType.CHINESE.equals(languageType)) {
            value = Optional.ofNullable(chineseProperties.get(key))
                    .filter(StringUtils::isNotEmpty)
                    .orElse(englishProperties.get(key));
        } else {
            value = englishProperties.get(key);
        }

        if (StringUtils.isEmpty(value)) {
            value = "{{" + key + "}}";
        }
        return value;
    }

    /**
     * 根据key值获取对应语言的实际值
     *
     * @param key
     * @return
     */
    public static String getString(String key) {
        int language = GlobalConfigManager.getInstance().getGlobalConfig().getLanguage();
        LanguageType languageType = LanguageType.languageType(language);
        return getString(languageType, key);
    }


}
