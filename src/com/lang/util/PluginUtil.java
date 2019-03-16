package com.lang.util;

import com.intellij.execution.ui.ConsoleView;
import com.intellij.execution.ui.ConsoleViewContentType;
import com.intellij.javaee.model.xml.ParamValue;
import com.intellij.lang.jvm.JvmModifier;
import com.intellij.lang.jvm.JvmParameter;
import com.intellij.lang.jvm.JvmTypeParameter;
import com.intellij.lang.jvm.types.JvmType;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.CompilerModuleExtension;
import com.intellij.openapi.roots.libraries.LibraryUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowId;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiClassType;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.impl.source.PsiClassReferenceType;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.util.PsiUtil;
import com.intellij.ui.content.Content;
import com.lang.runner.JavaRunner;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;

public class PluginUtil {
	
	/**
	 * show the result after execute user selected method
	 * @param event the event provided by intellj
	 */
	@SuppressWarnings("ConstantConditions")
	public static void showExecuteResultConsole(AnActionEvent event) {
		Project project = CommonDataKeys.PROJECT.getData(event.getDataContext());
		PsiFile psiFile = CommonDataKeys.PSI_FILE.getData(event.getDataContext());
		Editor editor = CommonDataKeys.EDITOR.getData(event.getDataContext());
		// find module which contains psiFile
		Module module = ModuleUtil.findModuleForFile(psiFile);
		if (null == module) {
			return;
		}
		
		List<String> classpaths = getModuleClasspathWithSources(project, module);
		
		PsiElement element = psiFile.findElementAt(editor.getCaretModel().getOffset());
		PsiMethod containingMethod = PsiTreeUtil.getParentOfType(element, PsiMethod.class);
		PsiClass containingClass = containingMethod.getContainingClass();
		String qualifiedName = containingClass.getQualifiedName();
		String className = containingClass.getName();
		String methodName = containingMethod.getName();
		boolean isStatic = containingMethod.hasModifier(JvmModifier.STATIC);
		String title = className + "." + methodName;
		
		final PrintStream preOut = System.out;
		
		try {
			ToolWindowUtils.invoke(project, title);
			ConsoleView consoleView = ToolWindowUtils.CONSOLE_VIEW;
			ToolWindow toolWindow = ToolWindowUtils.TOOL_WINDOW;
			
			boolean isShow = toolWindow.isActive();
			System.setOut(new PrintStream(System.out) {
				@Override
				public void println(String text) {
					if (!isShow) {
						toolWindow.activate(null);
					}
					consoleView.print(text + "\n", ConsoleViewContentType.NORMAL_OUTPUT);
				}
			});
			
			JavaRunner.invokeMethod(qualifiedName, methodName, isStatic, classpaths,
				buildParam(event, psiFile, containingMethod));
			System.out.println("\n\nexecute finished!");
		} finally {
			System.setOut(preOut);
		}
	}
	
	private static Object[] buildParam(AnActionEvent event, PsiFile psiFile, PsiMethod psiMethod) {
		List<Object> paramValues = new ArrayList<>();
		JvmParameter[] parameters = psiMethod.getParameters();
		for (JvmParameter parameter : parameters) {
			if (parameter.getType() instanceof PsiClassReferenceType) {
				PsiClassReferenceType type = (PsiClassReferenceType) parameter.getType();
				String qualifiedName = type.getReference().getQualifiedName();
				String parameterName = parameter.getName();
				
				if (AnActionEvent.class.getName().equals(qualifiedName)) {
					paramValues.add(event);
				} else if (Project.class.getName().equalsIgnoreCase(qualifiedName)) {
					paramValues.add(event.getProject());
				} else if (PsiFile.class.getName().equalsIgnoreCase(qualifiedName)) {
					paramValues.add(psiFile);
				} else if (Editor.class.getName().equalsIgnoreCase(qualifiedName)) {
					paramValues.add(CommonDataKeys.EDITOR.getData(event.getDataContext()));
				} else if (String.class.getName().equals(qualifiedName)) {
					if ("projectPath".equalsIgnoreCase(parameterName)) {
						paramValues.add(event.getProject().getBasePath());
					} else if ("projectName".equalsIgnoreCase(parameterName)) {
						paramValues.add(event.getProject().getName());
					} else if ("filename".equalsIgnoreCase(parameterName)) {
						paramValues.add(psiFile.getName());
					} else if ("filepath".equalsIgnoreCase(parameterName)) {
						paramValues.add(psiFile.getVirtualFile().getPath());
					} else {
						paramValues.add(null);
					}
				} else {
					paramValues.add(null);
				}
			}
		}
		return paramValues.toArray();
	}
	
	public static final String PLUGIN_ID = "com.lang.fastur.plugin";
	
	/**
	 * get module classpath with compiled output path(src, test)
	 *
	 * @param project the project
	 * @param module the module in the project
	 * @return the classpath list, NOTE: the reuslt is not null when classpath is empty.
	 */
	private static List<String> getModuleClasspathWithSources(Project project, Module module) {
		List<String> classpaths = new ArrayList<>();
		CompilerModuleExtension moduleExtension = CompilerModuleExtension.getInstance(module);
		if (null == moduleExtension) {
			return Collections.emptyList();
		}
		
		String testsUrl = moduleExtension.getCompilerOutputUrlForTests();
		if (StringUtils.isNoneBlank(testsUrl)) {
			classpaths.add(testsUrl);
		}
		String sourceUrl = moduleExtension.getCompilerOutputUrl();
		if (StringUtils.isNoneBlank(sourceUrl)) {
			classpaths.add(sourceUrl);
		}
		VirtualFile[] libraryRoots = LibraryUtil.getLibraryRoots(project, true, false);
		classpaths.addAll(Arrays.stream(libraryRoots).map(VirtualFile::getPath).collect(Collectors.toList()));
		return classpaths;
	}
	
	public static void cleanupContents(Content notToRemove, Project project, String contentName) {
		ToolWindow toolWindow = ToolWindowManager.getInstance(project).getToolWindow(ToolWindowId.RUN);
		
		for (Content content : toolWindow.getContentManager().getContents()) {
			if (content.isPinned()) continue;
			if (contentName.equals(content.getDisplayName()) && content != notToRemove) {
				if (toolWindow.getContentManager().removeContent(content, true)) {
					content.release();
				}
			}
		}
	}
}
