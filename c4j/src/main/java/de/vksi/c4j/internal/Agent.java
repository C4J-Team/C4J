package de.vksi.c4j.internal;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import java.security.ProtectionDomain;

import javassist.CtClass;

import org.apache.log4j.Logger;

public class Agent implements ClassFileTransformer {
	private Logger logger = Logger.getLogger(Agent.class);
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
		if (logger.isTraceEnabled()) {
			logger.trace("transformation started for class " + className);
		}
		try {
			rootTransformer.updateClassPath(loader, classfileBuffer, className);
			CtClass affectedClass = RootTransformer.INSTANCE.getPool().get(className);
			return rootTransformer.transformType(affectedClass);
		} catch (Exception e) {
			lastException = e;
			logger.fatal("Transformation failed for class '" + className + "'.", e);
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
