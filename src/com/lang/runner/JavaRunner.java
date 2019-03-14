package com.lang.runner;

import com.intellij.ide.plugins.PluginManager;
import com.intellij.openapi.extensions.PluginId;
import org.apache.commons.lang3.reflect.MethodUtils;

import java.io.File;

public class JavaRunner {
    private static File projectDir = new File("E:\\guoliang\\workspace\\idea-plugin-space\\fastur-plugin");
    private static final CachedCompiler compiler = new CachedCompiler(null, new File(projectDir, "generated\\compiled"));
    private static MyClassLoader myClassLoader;

    public static void invokeMethod(String className, String javaCode, String method, Object... args) {
        String path = "file:\\\\E:\\guoliang\\workspace\\idea-plugin-space\\live-coding\\out";
        ClassLoader pluginClassLoader = PluginManager.getPlugin(PluginId.getId("com.lang")).getPluginClassLoader();
        myClassLoader = new MyClassLoader(path, pluginClassLoader);
        try {
            CompilerUtils.CACHED_COMPILER = compiler;
            Class aClass = compiler.loadFromJava(myClassLoader, className, javaCode);
            Object newInstance = aClass.newInstance();
            Object result = MethodUtils.invokeStaticMethod(aClass, method, args);
            System.out.println("result = " + result);
//            Object hello = MethodUtils.invokeMethod(newInstance, "hello");
//            System.out.println("hello = " + hello);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
