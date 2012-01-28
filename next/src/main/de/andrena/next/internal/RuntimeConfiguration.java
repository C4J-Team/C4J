package de.andrena.next.internal;

import java.lang.reflect.Constructor;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtConstructor;
import javassist.CtMethod;
import javassist.NotFoundException;
import javassist.bytecode.Descriptor;

import org.apache.log4j.Logger;

import de.andrena.next.Configuration;

public class RuntimeConfiguration {
	private Set<CtMethod> whitelistMethods = new HashSet<CtMethod>();
	private Set<CtConstructor> whitelistConstructors = new HashSet<CtConstructor>();
	private Set<String> rootPackages;
	private boolean writeTransformedClasses;
	private ClassPool pool;
	private Logger logger = Logger.getLogger(getClass());

	public RuntimeConfiguration(Configuration configuration, ClassPool pool) throws Exception {
		this.rootPackages = configuration.getRootPackages();
		this.writeTransformedClasses = configuration.writeTransformedClasses();
		this.pool = pool;
		convertWhitelist(configuration.getPureWhitelist());
	}

	private void convertWhitelist(Set<Member> whitelistMembers) throws NotFoundException {
		for (Member whitelistMember : whitelistMembers) {
			if (whitelistMember instanceof Method) {
				whitelistMethods.add(convertToCtMethod((Method) whitelistMember));
			} else if (whitelistMember instanceof Constructor) {
				whitelistConstructors.add(convertToCtConstructor((Constructor<?>) whitelistMember));
			} else {
				logger.warn("invalid member found in Configuration: " + whitelistMember);
			}
		}
	}

	private CtConstructor convertToCtConstructor(Constructor<?> constructor) throws NotFoundException {
		return pool.get(constructor.getDeclaringClass().getName()).getConstructor(
				Descriptor.ofConstructor(convertToCtParams(constructor.getParameterTypes())));
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

	public Set<CtMethod> getWhitelistMethods() {
		return whitelistMethods;
	}

	public Set<CtConstructor> getWhitelistConstructors() {
		return whitelistConstructors;
	}

	public boolean isWithinRootPackages(CtClass clazz) {
		return isWithinRootPackages(clazz.getName());
	}

	public boolean isWithinRootPackages(String className) {
		for (String rootPackage : rootPackages) {
			if (className.startsWith(rootPackage)) {
				return true;
			}
		}
		return false;
	}

	public boolean writeTransformedClasses() {
		return writeTransformedClasses;
	}

}
