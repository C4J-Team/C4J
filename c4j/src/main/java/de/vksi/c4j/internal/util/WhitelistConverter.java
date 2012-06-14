package de.vksi.c4j.internal.util;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.NotFoundException;

public class WhitelistConverter {
	private ClassPool pool;

	public WhitelistConverter(ClassPool pool) {
		this.pool = pool;
	}

	public Set<CtMethod> convertWhitelist(Set<Method> whitelistMembers) throws NotFoundException {
		Set<CtMethod> whitelistMethods = new HashSet<CtMethod>();
		for (Method whitelistMember : whitelistMembers) {
			whitelistMethods.add(convertToCtMethod(whitelistMember));
		}
		return whitelistMethods;
	}

	private CtMethod convertToCtMethod(Method method) throws NotFoundException {
		return pool.get(method.getDeclaringClass().getName()).getDeclaredMethod(method.getName(),
				convertToCtParams(method.getParameterTypes()));
	}

	private CtClass[] convertToCtParams(Class<?>[] paramTypes) throws NotFoundException {
		CtClass[] params = new CtClass[paramTypes.length];
		for (int i = 0; i < params.length; i++) {
			params[i] = pool.get(paramTypes[i].getName());
		}
		return params;
	}
}
