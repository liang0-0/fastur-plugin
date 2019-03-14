package com.lang.runner;

import com.intellij.ide.plugins.IdeaPluginDescriptor;
import com.intellij.ide.plugins.PluginManager;
import com.intellij.openapi.extensions.PluginId;
import com.intellij.openapi.ui.Messages;
import com.lang.util.PluginUtil;
import org.apache.commons.lang3.reflect.MethodUtils;

import java.io.File;
import java.util.List;

public class JavaRunner {
    private static File projectDir = new File("E:\\guoliang\\workspace\\idea-plugin-space\\fastur-plugin");
    private static final CachedCompiler compiler = new CachedCompiler(null, new File(projectDir, "generated\\compiled"));
    private static MyClassLoader myClassLoader;

/*
    public static void invokeMethod(String className, String method, List<String> classpaths, Object... args) {
        ClassLoader pluginClassLoader = PluginManager.getPlugin(PluginId.getId("com.lang")).getPluginClassLoader();
        myClassLoader = new MyClassLoader(classpaths, pluginClassLoader);
        try {
            CompilerUtils.CACHED_COMPILER = compiler;
            Class aClass = compiler.loadFromJava(myClassLoader, className, javaCode);
            Object newInstance = aClass.newInstance();
            Object result = MethodUtils.invokeStaticMethod(aClass, method, args);
            System.out.println("result = " + result);
            Object hello = MethodUtils.invokeMethod(newInstance, "hello");
            System.out.println("hello = " + hello);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
*/

    public static void invokeMethod(String className, String method, boolean isStatic, List<String> classpaths, Object... args) {
        IdeaPluginDescriptor plugin = PluginManager.getPlugin(PluginId.getId(PluginUtil.PLUGIN_ID));
        ClassLoader pluginClassLoader = null == plugin ? null : plugin.getPluginClassLoader();
        myClassLoader = new MyClassLoader(classpaths, pluginClassLoader);
        try {
            Class<?> clazz = myClassLoader.loadClass(className);
            if (isStatic) {
                MethodUtils.invokeStaticMethod(clazz, method, args);
                return;
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
