package de.vksi.c4j.internal;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import javassist.ClassPool;
import javassist.CtClass;

import org.apache.log4j.Logger;

public class ClasspathScanner {
	private static final String FILE_EXT_CLASS = ".class";
	private static final String PROTOCOL_FILE = "file";
	private Logger logger = Logger.getLogger(ClasspathScanner.class);
	private final String packageName;
	private final boolean includeSubpackages;
	private final ClassLoader classLoader;
	private final ClassPool pool;
	private List<CtClass> classes = new ArrayList<CtClass>();

	public ClasspathScanner(ClassPool pool, String packageName, boolean includeSubpackages, ClassLoader classLoader)
			throws Exception {
		this.pool = pool;
		this.packageName = packageName;
		this.includeSubpackages = includeSubpackages;
		this.classLoader = classLoader;
		scanPackage();
	}

	public List<CtClass> getAllClasses() throws Exception {
		return classes;
	}

	private void scanPackage() throws IOException, Exception {
		Enumeration<URL> packageResources = classLoader.getResources(getPackagePath(packageName));
		if (packageResources == null) {
			logger.error("Couldn't find package " + packageName);
		}
		scanExistingPackages(packageName, includeSubpackages, packageResources);
	}

	private String getPackagePath(String packageName) {
		return packageName.replace('.', '/');
	}

	private void scanExistingPackages(String packageName, boolean includeSubpackages, Enumeration<URL> packageResources)
			throws Exception {
		while (packageResources.hasMoreElements()) {
			scanExistingPackage(packageName, includeSubpackages, packageResources.nextElement());
		}
	}

	private void scanExistingPackage(String packageName, boolean includeSubpackages, URL packageUrl) throws Exception {
		if (PROTOCOL_FILE.equals(packageUrl.getProtocol())) {
			scanPackageInFileSystem(packageUrl, includeSubpackages);
			return;
		}
		URLConnection packageUrlConnection = packageUrl.openConnection();
		if (packageUrlConnection instanceof JarURLConnection) {
			scanPackageInJarFile((JarURLConnection) packageUrlConnection, includeSubpackages, packageName);
			return;
		}
		logger.error("Cannot scan packages in protocol " + packageUrl.getProtocol() + " for package " + packageUrl);
	}

	private void scanPackageInJarFile(JarURLConnection packageUrlConnection, boolean includeSubpackages,
			String packageName) throws Exception {
		JarFile jarFile = packageUrlConnection.getJarFile();
		Enumeration<JarEntry> jarEntries = jarFile.entries();
		while (jarEntries.hasMoreElements()) {
			handleJarEntry(packageName, includeSubpackages, jarEntries.nextElement(), jarFile);
		}
		jarFile.close();
	}

	private void handleJarEntry(String packageName, boolean includeSubpackages, JarEntry jarEntry, JarFile jarFile)
			throws Exception {
		String packagePath = getPackagePath(packageName);
		if (!jarEntry.isDirectory() && jarEntry.getName().startsWith(packagePath)) {
			handleJarEntryInPackage(jarEntry, packagePath, includeSubpackages, jarFile);
		}
	}

	private void handleJarEntryInPackage(JarEntry jarEntry, String packagePath, boolean includeSubpackages,
			JarFile jarFile) throws Exception {
		String entryPath = jarEntry.getName();
		if (entryPath.substring(packagePath.length()).contains("/") == includeSubpackages
				&& entryPath.endsWith(FILE_EXT_CLASS)) {
			handleClassFileInPackage(jarFile.getInputStream(jarEntry));
		}
	}

	private void scanPackageInFileSystem(URL packageUrl, boolean includeSubpackages) throws Exception {
		File packageAsFile = new File(packageUrl.toURI());
		if (!packageAsFile.exists()) {
			logger.error("Cannot scan package " + packageUrl + " as it doesn't exist.");
			return;
		}
		if (!packageAsFile.isDirectory()) {
			logger.error("Cannot scan package " + packageUrl + " as it is not a directory.");
			return;
		}
		scanPackageAsDirectory(packageAsFile, includeSubpackages);
	}

	private void scanPackageAsDirectory(File packageAsDirectory, boolean includeSubpackages) throws Exception {
		for (File file : packageAsDirectory.listFiles()) {
			handleFileInPackage(file, includeSubpackages);
		}
	}

	private void handleFileInPackage(File file, boolean includeSubpackages) throws Exception {
		if (file.isDirectory() && includeSubpackages) {
			scanPackageAsDirectory(file, true);
		}
		if (file.isFile() && file.getName().endsWith(FILE_EXT_CLASS)) {
			handleClassFileInPackage(new FileInputStream(file));
		}
	}

	private void handleClassFileInPackage(InputStream inputStream) throws Exception {
		CtClass loadedClass = pool.makeClassIfNew(inputStream);
		classes.add(loadedClass);
	}
}
