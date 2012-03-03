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

	@Override
	protected Class<?> findClass(String name) throws ClassNotFoundException {
		String fileName = name.replace('.', File.separatorChar) + ".class";
		try {
			byte[] classBytes = loadClassData(fileName);
			Class<?> c = defineClass(name, classBytes, 0, classBytes.length);
			resolveClass(c);
			return c;
		} catch (IOException e) {
			throw new ClassNotFoundException("class not found: " + name, e);
		}
	}

	@Override
	public Class<?> loadClass(String name) throws ClassNotFoundException {
		Class<?> loadedClass = findLoadedClass(name);
		if (loadedClass == null) {
			try {
				loadedClass = findClass(name);
				logger.debug("locally loaded class " + name);
			} catch (ClassNotFoundException e) {
				// failed to load, try parent
			}
			if (loadedClass == null) {
				loadedClass = super.loadClass(name);
			}
		}
		return loadedClass;
	}

	private byte[] loadClassData(String name) throws IOException {
		InputStream stream = getParent().getResourceAsStream(name);
		if (stream == null) {
			throw new IOException("resource " + name + " not found.");
		}
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		int read;
		byte[] data = new byte[4096];
		while ((read = stream.read(data, 0, data.length)) != -1) {
			buffer.write(data, 0, read);
		}
		buffer.flush();
		return buffer.toByteArray();
	}
}
