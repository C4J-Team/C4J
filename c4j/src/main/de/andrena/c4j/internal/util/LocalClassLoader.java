package de.andrena.c4j.internal.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

public class LocalClassLoader extends ClassLoader {
	private Logger logger = Logger.getLogger(LocalClassLoader.class);
	private IOUtil ioUtil = new IOUtil();

	public LocalClassLoader(ClassLoader parent) {
		super(parent);
	}

	// stolen from http://www.javablogging.com/java-classloader-2-write-your-own-classloader/
	// also interesting: http://dow.ngra.de/2009/06/15/classloaderlocal-how-to-avoid-classloader-leaks-on-application-redeploy/
	@Override
	protected Class<?> findClass(String name) throws ClassNotFoundException {
		try {
			if (name.matches(Pattern.quote("de.andrena.c4j.") + "[^\\.]+")) {
				throw new ClassNotFoundException(
						"Framework classes have to be loaded by parent to avoid ClassCastExceptions and LinkageErrors.");
			}
			byte[] classBytes = ioUtil.readInputStream(getResourceStream(name));
			Class<?> c = defineClass(name, classBytes, 0, classBytes.length);
			resolveClass(c);
			return c;
		} catch (IOException e) {
			throw new ClassNotFoundException("class not found: " + name, e);
		} catch (SecurityException e) {
			throw new ClassNotFoundException("class not loadable: " + name, e);
		}
	}

	private InputStream getResourceStream(String name) throws IOException {
		String fileName = name.replace('.', '/') + ".class";
		InputStream stream = getParent().getResourceAsStream(fileName);
		if (stream == null) {
			throw new IOException("resource " + fileName + " not found.");
		}
		return stream;
	}

	// stolen from http://tech.puredanger.com/2006/11/09/classloader/
	@Override
	public Class<?> loadClass(String name) throws ClassNotFoundException {
		Class<?> loadedClass = findLoadedClass(name);
		if (loadedClass == null) {
			try {
				loadedClass = findClass(name);
				if (logger.isTraceEnabled()) {
					logger.trace("LocalClassLoader loads " + name);
				}
			} catch (ClassNotFoundException e) {
				loadedClass = super.loadClass(name);
				if (logger.isTraceEnabled()) {
					logger.trace("LocalClassLoader skips " + name, e);
				}
			}
		}
		return loadedClass;
	}
}
