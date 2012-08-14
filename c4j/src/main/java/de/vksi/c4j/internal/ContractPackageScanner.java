package de.vksi.c4j.internal;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import javassist.CtClass;

import org.apache.log4j.Logger;

import de.vksi.c4j.Contract;
import de.vksi.c4j.Contract.InheritedType;
import de.vksi.c4j.internal.configuration.C4JLocal.Configuration.ContractScanPackage;
import de.vksi.c4j.internal.util.BackdoorAnnotationLoader;

public class ContractPackageScanner {
	private static final String FILE_EXT_CLASS = ".class";
	private static final String PROTOCOL_FILE = "file";
	private Logger logger = Logger.getLogger(ContractPackageScanner.class);
	private final List<ContractScanPackage> contractScanPackages;
	private final ClassLoader classLoader;
	private Map<String, String> externalContracts = new HashMap<String, String>();

	public ContractPackageScanner(List<ContractScanPackage> contractScanPackages, ClassLoader classLoader)
			throws Exception {
		this.contractScanPackages = contractScanPackages;
		this.classLoader = classLoader;
		scan();
	}

	public Map<String, String> getExternalContracts() {
		return externalContracts;
	}

	private void scan() throws Exception {
		for (ContractScanPackage contractScanPackage : contractScanPackages) {
			scanPackage(classLoader, contractScanPackage);
		}
	}

	private void scanPackage(ClassLoader classLoader, ContractScanPackage contractScanPackage) throws Exception {
		Enumeration<URL> packageResources = classLoader.getResources(getPackagePath(contractScanPackage));
		if (packageResources == null) {
			logger.error("Couldn't find contract-scan-package " + contractScanPackage.getValue());
			return;
		}
		scanExistingPackages(contractScanPackage, packageResources);
	}

	private String getPackagePath(ContractScanPackage contractScanPackage) {
		return contractScanPackage.getValue().replace('.', '/');
	}

	private void scanExistingPackages(ContractScanPackage contractScanPackage, Enumeration<URL> packageResources)
			throws Exception {
		while (packageResources.hasMoreElements()) {
			scanExistingPackage(contractScanPackage, packageResources.nextElement());
		}
	}

	private void scanExistingPackage(ContractScanPackage contractScanPackage, URL packageUrl) throws Exception {
		if (PROTOCOL_FILE.equals(packageUrl.getProtocol())) {
			scanPackageInFileSystem(packageUrl, contractScanPackage.isIncludeSubpackages());
			return;
		}
		URLConnection packageUrlConnection = packageUrl.openConnection();
		if (packageUrlConnection instanceof JarURLConnection) {
			scanPackageInJarFile((JarURLConnection) packageUrlConnection, contractScanPackage);
			return;
		}
		logger.error("Cannot scan packages in protocol " + packageUrl.getProtocol() + " for package " + packageUrl);
	}

	private void scanPackageInJarFile(JarURLConnection packageUrlConnection, ContractScanPackage contractScanPackage)
			throws Exception {
		JarFile jarFile = packageUrlConnection.getJarFile();
		Enumeration<JarEntry> jarEntries = jarFile.entries();
		while (jarEntries.hasMoreElements()) {
			handleJarEntry(contractScanPackage, jarEntries.nextElement(), jarFile);
		}
		jarFile.close();
	}

	private void handleJarEntry(ContractScanPackage contractScanPackage, JarEntry jarEntry, JarFile jarFile)
			throws Exception {
		String packagePath = getPackagePath(contractScanPackage);
		if (!jarEntry.isDirectory() && jarEntry.getName().startsWith(packagePath)) {
			handleJarEntryInPackage(jarEntry, packagePath, contractScanPackage.isIncludeSubpackages(), jarFile);
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
		CtClass loadedClass = RootTransformer.INSTANCE.getPool().makeClassIfNew(inputStream);
		if (loadedClass.hasAnnotation(Contract.class)) {
			handleContractClass(loadedClass);
		}
	}

	private void handleContractClass(CtClass loadedClass) {
		String targetFromAnnotation = new BackdoorAnnotationLoader(loadedClass).getClassValue(Contract.class,
				"forTarget");
		String contractClass = loadedClass.getName();
		if (targetFromAnnotation != null && !targetFromAnnotation.equals(InheritedType.class.getName())) {
			addExternalContract(targetFromAnnotation, contractClass);
			return;
		}
		handleContractClassInheritingTargetClass(loadedClass, contractClass);
	}

	private void handleContractClassInheritingTargetClass(CtClass loadedClass, String contractClass) {
		if (!loadedClass.getClassFile().getSuperclass().equals(Object.class.getName())) {
			addExternalContract(loadedClass.getClassFile().getSuperclass(), contractClass);
			return;
		}
		if (loadedClass.getClassFile().getInterfaces().length == 1) {
			addExternalContract(loadedClass.getClassFile().getInterfaces()[0], contractClass);
			return;
		}
		logger.error("Contract "
				+ loadedClass.getName()
				+ " was found in ContractsDirectory, but could not be mapped to a target class. "
				+ "Extend from the target class or implement the target interface (while not implementing any other interfaces) "
				+ "or simply declare the target class within the @Contract annotation.");
	}

	private void addExternalContract(String targetClassName, String contractClassName) {
		externalContracts.put(targetClassName, contractClassName);
	}
}
