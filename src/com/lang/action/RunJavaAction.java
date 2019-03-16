package com.lang.action;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.task.ProjectTaskManager;
import com.lang.util.PluginUtil;
import org.jetbrains.annotations.NotNull;

public class RunJavaAction extends AnAction {

    @Override
    public void update(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        if (project == null) {
            e.getPresentation().setEnabledAndVisible(false);
            return;
        }

        PsiFile psiFile = e.getData(CommonDataKeys.PSI_FILE);
        if (psiFile == null) {
            e.getPresentation().setEnabledAndVisible(false);
            return;
        }

        Editor editor = e.getData(CommonDataKeys.EDITOR);
        if (editor == null) {
            e.getPresentation().setEnabledAndVisible(false);
            return;
        }

        PsiElement element = psiFile.findElementAt(editor.getCaretModel().getOffset());
        PsiMethod containingMethod = PsiTreeUtil.getParentOfType(element, PsiMethod.class);
        if (containingMethod == null) {
            e.getPresentation().setEnabledAndVisible(false);
            return;
        }

        PsiClass containingClass = containingMethod.getContainingClass();
        if (containingClass == null) {
            e.getPresentation().setEnabledAndVisible(false);
            return;
        }
        e.getPresentation().setEnabledAndVisible(true);
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {
        Project project = event.getProject();
        PsiFile psiFile = event.getData(CommonDataKeys.PSI_FILE);
        Module module = ModuleUtil.findModuleForFile(psiFile);
        
        ProjectTaskManager.getInstance(project).rebuild(new Module[]{module}, executionResult -> {
            if (!executionResult.isAborted() && executionResult.getErrors() > 0) {
                Messages.showErrorDialog("compile error", "Error");
                return;
            }

            if (!executionResult.isAborted() && executionResult.getErrors() == 0) {
                PluginUtil.showExecuteResultConsole(event);
            }
        });

    }
}
