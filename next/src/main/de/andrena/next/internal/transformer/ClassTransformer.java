package de.andrena.next.internal.transformer;

import javassist.CtClass;

import org.apache.log4j.Logger;

public abstract class ClassTransformer {
	protected Logger logger = Logger.getLogger(getClass());

	public abstract void transform(CtClass targetClass, CtClass contractClass) throws Exception;
}
