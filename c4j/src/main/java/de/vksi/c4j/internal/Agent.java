package de.vksi.c4j.internal;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import java.security.ProtectionDomain;

import javassist.CtClass;

import org.apache.log4j.Logger;

import de.vksi.c4j.internal.classfile.ClassFilePool;

public class Agent implements ClassFileTransformer {
	private static final Logger LOGGER = Logger.getLogger(Agent.class);
	private RootTransformer rootTransformer = RootTransformer.INSTANCE;

	private static Throwable lastException;

	public static void premain(String agentArgs, Instrumentation inst) throws Exception {
		RootTransformer.INSTANCE.init();
		inst.addTransformer(new Agent());
	}

	@Override
	public byte[] transform(ClassLoader loader, String classNameWithSlashes, Class<?> classBeingRedefined,
			ProtectionDomain protectionDomain, byte[] classfileBuffer) {
		String className = classNameWithSlashes.replace('/', '.');
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("transformation started for class " + className);
		}
		try {
			rootTransformer.updateClassPath(loader, classfileBuffer, className);
			CtClass affectedClass = ClassFilePool.INSTANCE.getClass(className);
			return rootTransformer.transformType(affectedClass);
		} catch (Exception e) {
			lastException = e;
			LOGGER.fatal("Transformation failed for class '" + className + "'.", e);
		}
		return null;
	}

	public static Throwable getLastException() {
		return lastException;
	}

	public static void resetLastException() {
		lastException = null;
	}
}
