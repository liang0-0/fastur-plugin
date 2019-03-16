package com.lang.util;

import com.intellij.execution.filters.TextConsoleBuilder;
import com.intellij.execution.filters.TextConsoleBuilderFactory;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.icons.AllIcons.Debugger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowAnchor;
import com.intellij.openapi.wm.ToolWindowId;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory.SERVICE;

public class ToolWindowUtils {
	public static ConsoleView CONSOLE_VIEW;
	public static ToolWindow TOOL_WINDOW;
	
	public static void invoke(Project project, String title) {
		TextConsoleBuilder consoleBuilder = TextConsoleBuilderFactory.getInstance().createBuilder(project);
		CONSOLE_VIEW = consoleBuilder.getConsole();
		ToolWindowManager toolWindowManager = ToolWindowManager.getInstance(project);
		TOOL_WINDOW = toolWindowManager.getToolWindow(ToolWindowId.RUN);
		if (null == TOOL_WINDOW) {
			TOOL_WINDOW = toolWindowManager.registerToolWindow(ToolWindowId.RUN, true, ToolWindowAnchor.BOTTOM);
		}
		TOOL_WINDOW.setIcon(Debugger.Console);
		
		Content content = SERVICE.getInstance().createContent(CONSOLE_VIEW.getComponent(), title, true);
		TOOL_WINDOW.getContentManager().addContent(content);
		PluginUtil.cleanupContents(content, project, title);
		TOOL_WINDOW.getContentManager().setSelectedContent(content);
	}
}
