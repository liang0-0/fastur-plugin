package com.lang.runner;

import java.io.*;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MyClassLoader extends ClassLoader {
    /**
     * key is loaded classes, value is last loaded timestamp
     */
    private static final Map<Class, Long> loadedClassMap = new HashMap<>();
    /**
     * where to find class when load classes
     */
    private List<String> classpaths;
    private ClassLoader parentClassLoader;

    public MyClassLoader(List<String> classpaths, ClassLoader parentClassLoader) {
        this.classpaths = classpaths;
        this.parentClassLoader = parentClassLoader;
    }


    @Override
    public Class<?> loadClass(String name) throws ClassNotFoundException {
        Class aClass = this.findClass(name);
        if (null != aClass) {
            return aClass;
        }
        return super.loadClass(name);
    }

    //用于寻找类文件
    public Class findClass(String name) {
        byte[] b = loadClassData(name);
        return defineClass(name, b, 0, b.length);
    }

    //用于加载类文件
    private byte[] loadClassData(String name) {
        // TODO: 2019/3/15 analysis classpaths, recursive file class file
        name = classpaths + name + ".class";
        //使用输入流读取类文件
        InputStream in = null;
        //使用byteArrayOutputStream保存类文件。然后转化为byte数组
        ByteArrayOutputStream out = null;
        try {
            in = new FileInputStream(new File(new URI(name)));
            out = new ByteArrayOutputStream();
            int i = 0;
            while ((i = in.read()) != -1) {
                out.write(i);
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (null != out) {
                    out.close();
                }
                if (null != in) {
                    in.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        return out.toByteArray();

    }

}