package com.lang.action;

import com.intellij.lang.jvm.JvmModifier;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.CompilerModuleExtension;
import com.intellij.openapi.roots.libraries.LibraryUtil;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.task.ProjectTaskManager;
import com.lang.runner.JavaRunner;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

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
        CompilerModuleExtension compilerModuleExtension = CompilerModuleExtension.getInstance(module);
        String sourceUrl = compilerModuleExtension.getCompilerOutputUrl();
        String testUrl = compilerModuleExtension.getCompilerOutputUrlForTests();
        VirtualFile[] libraryRoots = LibraryUtil.getLibraryRoots(project, true, false);
        List<String> classpaths = Arrays.stream(libraryRoots).map(VirtualFile::getPath).collect(Collectors.toList());
        classpaths.add(0, testUrl);
        classpaths.add(0, sourceUrl);

        Editor editor = CommonDataKeys.EDITOR.getData(event.getDataContext());
        int offset = editor.getCaretModel().getOffset();
        PsiElement element = psiFile.findElementAt(offset);
        PsiMethod containingMethod = PsiTreeUtil.getParentOfType(element, PsiMethod.class);
        PsiClass containingClass = containingMethod.getContainingClass();

//        PluginClassLoader classLoader = (PluginClassLoader) Thread.currentThread().getContextClassLoader();
//        classLoader.addLibDirectories(Arrays.asList("E:\\guoliang\\workspace\\idea-plugin-space\\fastur-plugin\\out\\production\\fastur-plugin",
//                ""));

        ProjectTaskManager.getInstance(project).rebuild(new Module[]{module}, executionResult -> {
            if (!executionResult.isAborted() && executionResult.getErrors() > 0) {
                Messages.showErrorDialog("compile error", "Error");
            }

            if (!executionResult.isAborted() && executionResult.getErrors() == 0) {
                String className = containingClass.getQualifiedName();
                String methodName = containingMethod.getName();
                boolean isStatic = containingMethod.hasModifier(JvmModifier.STATIC);
                JavaRunner.invokeMethod(className, methodName, isStatic, classpaths, event);
            }
        });

    }
}
