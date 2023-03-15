package com.veezean.idea.plugin.codereviewer.action.service;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.veezean.idea.plugin.codereviewer.util.Logger;

/**
 * <类功能简要描述>
 *
 * @author Veezean, 公众号 @架构悟道
 * @since 2023/3/15
 */
@Service
public class IdeaApplicationLevelExtService implements IApplicationLevelService{
    public IdeaApplicationLevelExtService() {
        Logger.info("IdeaApplicationLevelExtService服务注册成功");
    }

    @Override
    public void dispose() {
        Logger.info("IdeaApplicationLevelExtService 退出前资源释放");
    }
}
