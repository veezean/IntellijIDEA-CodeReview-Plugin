package com.veezean.idea.plugin.codereviewer.common;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 项目实例管理类
 *
 * @author Wang Weiren
 * @since 2019/10/8
 */
public class ProjectInstanceManager {
    private static ProjectInstanceManager instance;

    private final Map<String, InnerProjectCache> projectCacheMap;

    private ProjectInstanceManager() {
        projectCacheMap = new ConcurrentHashMap<>();
    }

    public synchronized static ProjectInstanceManager getInstance() {
        if (instance == null) {
            instance = new ProjectInstanceManager();
        }
        return instance;
    }

    public InnerProjectCache getProjectCache(String projectHash) {
        return projectCacheMap.get(projectHash);
    }

    public void addProjectCache(String projectHash, InnerProjectCache cache) {
        this.projectCacheMap.put(projectHash, cache);
    }
}
