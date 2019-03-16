package com.lang.util;

import java.io.IOException;
import java.util.jar.JarFile;
import org.apache.commons.lang3.StringUtils;

/**
 * @author guoliang
 * @date 2019-03-16 10:45
 */
public class JarUtils {
	
	public static final String JAR_SEPARATOR = "!/";
	
	public static boolean containClass(String jarFilePath, String className) {
		if (StringUtils.isBlank(jarFilePath) || StringUtils.isBlank(className)) {
			return false;
		}
		
		if (jarFilePath.startsWith("file:")) return false;
		if (jarFilePath.endsWith(JAR_SEPARATOR)) {
			jarFilePath = jarFilePath.substring(0, jarFilePath.length() - 2);
		}
		
		try {
			JarFile jarFile = new JarFile(jarFilePath);
			String entryPath = className.replace('.', '/') + ".class";
			return jarFile.getEntry(entryPath) != null;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}

}
