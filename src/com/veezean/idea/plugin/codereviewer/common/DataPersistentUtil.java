package com.veezean.idea.plugin.codereviewer.common;

import com.veezean.idea.plugin.codereviewer.model.CodeReviewCommentCache;

import java.io.*;

/**
 * <类功能简要描述>
 *
 * @author admin
 * @since 2019/10/2
 */
public class DataPersistentUtil {

    private static int projectIdentifier = -1;

    public static int getProjectIdentifier() {
        return projectIdentifier;
    }

    public synchronized static void setProjectIdentifier(int projectIdentifier) {
        DataPersistentUtil.projectIdentifier = projectIdentifier;
    }

    private static File prepareAndGetCacheDataPath() {
        String usrHome = System.getProperty("user.home");
        File userDir = new File(usrHome);
        File cacheDir = new File(userDir, ".idea_code_review_data");
        if (!cacheDir.exists() || !cacheDir.isDirectory()) {
            boolean mkdirs = cacheDir.mkdirs();
            if (!mkdirs) {
                System.out.println("create cache path failed...");
            }
        }

        File cacheDataFile = new File(cacheDir, projectIdentifier + ".dat");
        return cacheDataFile;
    }

    public synchronized static void serialize(CodeReviewCommentCache cache) {
        File file = prepareAndGetCacheDataPath();
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

    public synchronized static CodeReviewCommentCache deserialize() {
        File file = prepareAndGetCacheDataPath();
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
