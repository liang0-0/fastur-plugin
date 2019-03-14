package com.lang.action;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.wm.WindowManager;

import java.awt.*;

@SuppressWarnings("ALL")
public class EditorInfoAction  {

    public static void showEditorInfo(AnActionEvent event) {
        Project project = null;
        for (Project project1 : ProjectManager.getInstance().getOpenProjects()) {
            Window window = WindowManager.getInstance().suggestParentWindow(project1);
            if (null != window && window.isActive()) {
                project = project1;
                break;
            }
        }
        if (null == project) {
            System.out.println("project = " + project);
            return;
        }
        System.out.println("editor.getProject().getBasePath() = " + project.getBasePath());
        FileEditor fileEditor = FileEditorManager.getInstance(project).getSelectedEditor();
        System.out.println("fileEditor.getName() = " + fileEditor.getName());
        System.out.println("fileEditor.getFile().getPath() = " + fileEditor.getFile().getPath());
        System.out.println("fileEditor.getFile().getUrl() = " + fileEditor.getFile().getUrl());
    }
}
