package de.vksi.c4j.internal.classfile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;

import javassist.ByteArrayClassPath;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.LoaderClassPath;
import javassist.NotFoundException;

import org.apache.log4j.Logger;

public class ClassFilePool {
	public static final ClassFilePool INSTANCE = new ClassFilePool();

	private static final Logger LOGGER = Logger.getLogger(ClassFilePool.class);

	private ClassPool pool = createClassPool();

	protected ClassFilePool() {
	}

	protected ClassPool createClassPool() {
		return ClassPool.getDefault();
	}

	public ClassPool getPool() {
		return pool;
	}

	public void addClassLoader(ClassLoader loader) {
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("updating classpath with loader " + loader.getClass() + ", parent " + loader.getParent());
		}
		pool.insertClassPath(new LoaderClassPath(loader));
	}

	public void addClassFile(byte[] classfileBuffer, String className) {
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("updating classpath with classfileBuffer for class " + className);
		}
		pool.insertClassPath(new ByteArrayClassPath(className, classfileBuffer));
	}

	public CtClass getClassFromAnnotationValue(CtClass annotatedClass, Class<? extends Annotation> annotationClass,
			String annotationField) throws NotFoundException {
		String contractClassString = new BackdoorAnnotationLoader(annotatedClass).getClassValue(annotationClass,
				annotationField);
		return pool.get(contractClassString);
	}

	public CtClass getClass(String className) throws NotFoundException {
		return pool.get(className);
	}

	public CtClass createClass(File classFile) throws IOException {
		return createClass(new FileInputStream(classFile));
	}

	public CtClass createClass(InputStream inputStream) throws IOException {
		return pool.makeClassIfNew(inputStream);
	}

	public CtClass getClass(Class<?> clazz) throws NotFoundException {
		return getClass(clazz.getName());
	}
}
