package com.lang.runner;

import com.lang.util.JarUtils;
import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.lang3.StringUtils;

public class MyClassLoader extends ClassLoader {
    
    /**
     * key is loaded classes, value is last loaded timestamp
     */
    private static final List<Class> loadedClasses = new ArrayList<>();
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
        Class clazz = loadClassData(name);
        if (null == clazz && null != parentClassLoader) {
            try {
                return this.parentClassLoader.loadClass(name);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
                return null;
            }
        }
        return clazz;
    }

    //用于加载类文件
    private Class loadClassData(String name) {
        File filepath = null;
        for (String classpath : classpaths) {
            File dirToFind;
            try {
    
                if (JarUtils.containClass(classpath, name)) {
                    URLClassLoader loader = new URLClassLoader(new URL[]{new URL("file:" + classpath)});
                    Class<?> aClass = loader.loadClass(name);
                    if (null != aClass) {
                        return aClass;
                    }
                }
                
                if (classpath.startsWith("file:")) {
                    URI uri = new URI(classpath);
                    dirToFind = new File(uri);
                    if (!dirToFind.exists()) {
                        continue;
                    }
                } else {
                    continue;
                }
            } catch (Exception e) {
                e.printStackTrace();
                continue;
            }
    
            String suffix = name.replace(".", "/") + ".class";
            Collection<File> files = FileUtils.listFiles(dirToFind, new IOFileFilter() {
                    @Override
                    public boolean accept(File file) {
                        return file.getAbsolutePath().endsWith(suffix);
                    }
        
                    @Override
                    public boolean accept(File file, String name) {
                        return file.getAbsolutePath().endsWith(suffix);
                    }
                },
                FileFilterUtils.directoryFileFilter());
            if (!files.isEmpty()) {
                filepath = files.toArray(new File[0])[0];
                break;
            }
        }
    
        if (null == filepath) {
            return null;
        }
        //使用输入流读取类文件
        InputStream in = null;
        //使用byteArrayOutputStream保存类文件。然后转化为byte数组
        ByteArrayOutputStream out = null;
        try {
            in = new FileInputStream(filepath);
            out = new ByteArrayOutputStream();
            int len;
            byte[] buffer = new byte[1024];
            while ((len = in.read(buffer)) != -1) {
                out.write(buffer, 0, len);
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
        if (null == out) {
            return null;
        }
        byte[] b = out.toByteArray();
        if (null == b) return null;
        return defineClass(null, b, 0, b.length);
    }
    
}
