package com.veezean.idea.plugin.codereviewer.common;

import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiJavaFile;

/**
 * <类功能简要描述>
 *
 * @author Wang Weiren
 * @since 2023/1/14
 */
public class PsiFileUtil {

    public static String getFileFullName(PsiFile psiFile) {
        String classPath = psiFile.getVirtualFile().getName();
        if (psiFile instanceof PsiJavaFile) {
            // 如果是java文件，则一并存储下packagename，解决
            classPath = ((PsiJavaFile)psiFile).getPackageName() + "|" + classPath;
        }
        return classPath;
    }

}
