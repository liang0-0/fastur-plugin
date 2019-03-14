package com.lang.action;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.roots.impl.ProjectFileIndexImpl;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.util.PsiTreeUtil;
import com.lang.runner.JavaRunner;

public class RunJavaAction extends AnAction {

    @Override
    public void actionPerformed(AnActionEvent e) {
        Project project = e.getProject();
        ProjectFileIndex fileIndex = ProjectRootManager.getInstance(project).getFileIndex();
        PsiFile psiFile = e.getData(CommonDataKeys.PSI_FILE);
        boolean isSource = fileIndex.isInSource(psiFile.getVirtualFile());
        boolean isTest = fileIndex.isInTestSourceContent(psiFile.getVirtualFile());
        VirtualFile classRootForFile = fileIndex.getClassRootForFile(psiFile.getVirtualFile());
        System.out.println("classRootForFile = " + classRootForFile);
        // test file
        boolean forTests = ((ProjectFileIndexImpl) fileIndex).getSourceFolder(psiFile.getVirtualFile()).getRootType().isForTests();
        Editor editor = CommonDataKeys.EDITOR.getData(e.getDataContext());
        int offset = editor.getCaretModel().getOffset();
        PsiElement element = psiFile.findElementAt(offset);
        PsiMethod containingMethod = PsiTreeUtil.getParentOfType(element, PsiMethod.class);
        PsiClass containingClass = containingMethod.getContainingClass();

//        PluginClassLoader classLoader = (PluginClassLoader) Thread.currentThread().getContextClassLoader();
//        classLoader.addLibDirectories(Arrays.asList("E:\\guoliang\\workspace\\idea-plugin-space\\fastur-plugin\\out\\production\\fastur-plugin",
//                ""));

        JavaRunner.invokeMethod(containingClass.getQualifiedName(), editor.getDocument().getText(), containingMethod.getName(), e);
    }
}
