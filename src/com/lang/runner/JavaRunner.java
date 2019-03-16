package com.lang.runner;

import com.intellij.ide.plugins.IdeaPluginDescriptor;
import com.intellij.ide.plugins.PluginManager;
import com.intellij.openapi.extensions.PluginId;
import com.intellij.openapi.ui.Messages;
import com.lang.util.PluginUtil;
import java.util.List;
import org.apache.commons.lang3.reflect.MethodUtils;

public class JavaRunner {
    
    public static void invokeMethod(String className, String method, boolean isStatic, List<String> classpaths, Object[] args) {
        IdeaPluginDescriptor plugin = PluginManager.getPlugin(PluginId.getId(PluginUtil.PLUGIN_ID));
        ClassLoader pluginClassLoader = null == plugin ? null : plugin.getPluginClassLoader();
        MyClassLoader myClassLoader = new MyClassLoader(classpaths, pluginClassLoader);
        try {
            Class<?> clazz = myClassLoader.loadClass(className);
            if (isStatic) {
                MethodUtils.invokeStaticMethod(clazz, method, args);
            } else {
                Object newInstance = clazz.newInstance();
                MethodUtils.invokeMethod(newInstance, method, args);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Messages.showErrorDialog("Load Class Error:" + e.getMessage(), "Error");
        }
    }

}
