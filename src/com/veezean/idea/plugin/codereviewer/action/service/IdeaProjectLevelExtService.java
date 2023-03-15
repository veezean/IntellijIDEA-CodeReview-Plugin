package com.veezean.idea.plugin.codereviewer.action.service;

import com.intellij.openapi.components.ComponentManager;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.project.Project;
import com.veezean.idea.plugin.codereviewer.util.Logger;

/**
 * <类功能简要描述>
 *
 * @author Veezean, 公众号 @架构悟道
 * @since 2023/3/15
 */
@Service
public final class IdeaProjectLevelExtService implements IProjectLevelService {
    private final Project project;
    public IdeaProjectLevelExtService(Project project) {
        this.project = project;
        Logger.info("IdeaProjectLevelExtService服务注册成功");
    }

    @Override
    public void dispose() {
        // 执行清理操作
        Logger.info("项目销毁，执行清理操作:"  + project.getName());
    }
}
