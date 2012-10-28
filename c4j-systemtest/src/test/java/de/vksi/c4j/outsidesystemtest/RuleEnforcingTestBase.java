package de.vksi.c4j.outsidesystemtest;

import static org.junit.Assert.fail;

import java.util.List;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtField;
import javassist.CtMethod;
import javassist.Modifier;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;

import de.vksi.c4j.internal.ClasspathScanner;
import de.vksi.c4j.systemtest.TransformerAwareRule;

public class RuleEnforcingTestBase {

	private final ClassPool pool = new ClassPool(true);
	private final Class<? extends TestRule> ruleClass = TransformerAwareRule.class;

	public void enforceRuleInPackage(String packageName) throws Exception {
		List<CtClass> classes = new ClasspathScanner(pool, packageName, true, Thread.currentThread()
				.getContextClassLoader()).getAllClasses();
		for (CtClass clazz : classes) {
			handleClassFileInPackage(clazz);
		}
	}

	private void handleClassFileInPackage(CtClass clazz) throws Exception {
		if (!containsTestMethods(clazz)) {
			return;
		}
		for (CtField field : clazz.getFields()) {
			if (field.hasAnnotation(Rule.class) && field.getType().getName().equals(ruleClass.getName())
					&& Modifier.isPublic(field.getModifiers())) {
				return;
			}
		}
		fail("Mandatory Rule " + ruleClass + " was not found in test-class " + clazz.getName() + ".");
	}

	private boolean containsTestMethods(CtClass clazz) {
		for (CtMethod method : clazz.getDeclaredMethods()) {
			if (method.hasAnnotation(Test.class)) {
				return true;
			}
		}
		return false;
	}

}