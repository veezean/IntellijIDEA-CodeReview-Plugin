package com.veezean.idea.plugin.codereviewer.common;

import cn.hutool.core.io.FileUtil;
import cn.hutool.json.JSONUtil;
import com.veezean.idea.plugin.codereviewer.util.CommonUtil;
import com.veezean.idea.plugin.codereviewer.util.Logger;

import java.io.*;

/**
 * 序列化反序列化工具类
 *
 * @author Veezean, 公众号 @架构悟道
 * @since 2021/4/26
 */
public final class SerializeUtils {

    synchronized static <T> void saveConfigAsJson(T data, String parentDirName, String fileName) {
        try {
            File filePathName = prepareAndGetCacheDataPath(parentDirName, fileName);
            FileUtil.writeUtf8String(JSONUtil.toJsonStr(data), filePathName);
        } catch (Exception e) {
            throw new CodeReviewException("JSON序列化存储本地缓存数据异常:" + e.getMessage());
        }
    }

    synchronized static <T> T readConfigAsJson(Class<T> clazz, String parentDirName, String fileName) {
        try {
            File filePathName = prepareAndGetCacheDataPath(parentDirName, fileName);
            String jsonContent = FileUtil.readUtf8String(filePathName);
            return JSONUtil.toBean(jsonContent, clazz);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 生成文件序列化地址
     *
     * @param parentDirName 父目录名称（非绝对路径，仅仅是父级目录名称，最终会放在user.home下面）
     * @param fileName 具体文件名称
     * @return 存储的文件File对象
     */
    private static File prepareAndGetCacheDataPath(String parentDirName, String fileName) {
        String usrHome = System.getProperty("user.home");
        File userDir = new File(usrHome);
        File cacheDir = new File(userDir, parentDirName);
        if (!cacheDir.exists() || !cacheDir.isDirectory()) {
            boolean mkdirs = cacheDir.mkdirs();
            if (!mkdirs) {
                Logger.error("本地文件缓存路径创建失败，地址：" + cacheDir.getAbsolutePath());
            }
        }

        return new File(cacheDir, fileName);
    }

    /**
     * 序列化相关信息
     *
     * @param data 待序列化的数据
     * @param parentDirName 父目录名称（非绝对路径，仅仅是父级目录名称，最终会放在user.home下面）
     * @param fileName 文件名
     */
    synchronized static <T extends Serializable> void serialize(T data, String parentDirName,
                                                                String fileName) {
        File file = prepareAndGetCacheDataPath(parentDirName, fileName);
        ObjectOutputStream oout = null;
        try {
            oout = new ObjectOutputStream(new FileOutputStream(file));
            oout.writeObject(data);
        } catch (Exception e) {
            throw new CodeReviewException("序列化本地缓存数据异常:" + e.getMessage());
        } finally {
            CommonUtil.closeQuitely(oout);
        }
    }

    /**
     * 反序列化评审数据
     *
     * @param parentDirName 父目录名称（非绝对路径，仅仅是父级目录名称，最终会放在user.home下面）
     * @param fileName 文件名
     * @param <T> 反序列化的实体类型
     * @return 反序列化后的评审数据
     */
    synchronized static <T extends Serializable> T deserialize(String parentDirName, String fileName) {
        File file = prepareAndGetCacheDataPath(parentDirName, fileName);
        ObjectInputStream oin = null;
        T cache;
        try {
            oin = new ObjectInputStream(new FileInputStream(file));
            cache = (T) oin.readObject(); // 强制转换到Person类型
        } catch (Exception e) {
            return null;
        } finally {
            CommonUtil.closeQuitely(oin);
        }
        return cache;
    }
}
