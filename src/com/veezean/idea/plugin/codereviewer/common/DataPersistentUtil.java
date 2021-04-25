package com.veezean.idea.plugin.codereviewer.common;

import com.intellij.openapi.project.Project;
import com.veezean.idea.plugin.codereviewer.model.CodeReviewCommentCache;

import java.io.*;

/**
 * 数据持久化工具类
 *
 * @author Wang Weiren
 * @since 2019/10/2
 */
public class DataPersistentUtil {

    private static File prepareAndGetCacheDataPath(String projectHash) {
        String usrHome = System.getProperty("user.home");
        File userDir = new File(usrHome);
        File cacheDir = new File(userDir, ".idea_code_review_data");
        if (!cacheDir.exists() || !cacheDir.isDirectory()) {
            boolean mkdirs = cacheDir.mkdirs();
            if (!mkdirs) {
                System.out.println("create cache path failed...");
            }
        }

        File cacheDataFile = new File(cacheDir, projectHash + ".dat");
        return cacheDataFile;
    }

    /**
     * 序列化评审信息
     *
     * @param cache 评审信息缓存数据
     * @param project 当前项目
     */
    public synchronized static void serialize(CodeReviewCommentCache cache, Project project) {
        File file = prepareAndGetCacheDataPath(project.getLocationHash());
        ObjectOutputStream oout = null;
        try {
            oout = new ObjectOutputStream(new FileOutputStream(file));
            oout.writeObject(cache);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            CommonUtil.closeQuitely(oout);
        }
    }

    /**
     * 反序列化评审数据
     *
     * @param project 当前项目
     * @return 反序列化后的评审数据
     */
    public synchronized static CodeReviewCommentCache deserialize(Project project) {
        File file = prepareAndGetCacheDataPath(project.getLocationHash());
        ObjectInputStream oin = null;
        CodeReviewCommentCache cache = null;
        try {
            oin = new ObjectInputStream(new FileInputStream(file));
            cache = (CodeReviewCommentCache) oin.readObject(); // 强制转换到Person类型
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            CommonUtil.closeQuitely(oin);
        }
        return cache;
    }



}
