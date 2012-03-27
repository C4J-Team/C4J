package de.andrena.c4j.internal.util;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.apache.log4j.Logger;

public class LocalClassLoader extends ClassLoader {
	private Logger logger = Logger.getLogger(LocalClassLoader.class);

	public LocalClassLoader(ClassLoader parent) {
		super(parent);
	}

	// stolen from http://www.javablogging.com/java-classloader-2-write-your-own-classloader/
	// also interesting: http://dow.ngra.de/2009/06/15/classloaderlocal-how-to-avoid-classloader-leaks-on-application-redeploy/
	@Override
	protected Class<?> findClass(String name) throws ClassNotFoundException {
		try {
			byte[] classBytes = loadClassData(name);
			Class<?> c = defineClass(name, classBytes, 0, classBytes.length);
			resolveClass(c);
			return c;
		} catch (IOException e) {
			throw new ClassNotFoundException("class not found: " + name, e);
		} catch (SecurityException e) {
			throw new ClassNotFoundException("class not loadable: " + name, e);
		}
	}

	private byte[] loadClassData(String name) throws IOException {
		InputStream stream = getResourceStream(name);
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		int read;
		byte[] data = new byte[4096];
		while ((read = stream.read(data, 0, data.length)) != -1) {
			buffer.write(data, 0, read);
		}
		buffer.flush();
		return buffer.toByteArray();
	}

	private InputStream getResourceStream(String name) throws IOException {
		String fileName = name.replace('.', File.separatorChar) + ".class";
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
				logger.trace("locally loaded class " + name);
			} catch (ClassNotFoundException e) {
				loadedClass = super.loadClass(name);
			}
		}
		return loadedClass;
	}
}
