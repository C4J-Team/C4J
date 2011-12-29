package de.andrena.next.internal.compiler;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;

import org.junit.Before;
import org.junit.Test;

import de.andrena.next.internal.util.ObjectConverter;

public class ArrayExpTest {

	private ClassPool pool;
	private CtClass dummyClass;
	private CtMethod dummyMethod;

	@Before
	public void before() throws Exception {
		pool = ClassPool.getDefault();
		dummyClass = pool.get(DummyClass.class.getName());
		dummyMethod = dummyClass.getDeclaredMethod("dummyMethod");
	}

	@Test
	public void testArrayExpWithList() {
		assertEquals(
				"new java.lang.String[] { \"one\", \"two\", \"three\" }",
				new ArrayExp(String.class, Arrays.<NestedExp> asList(new ValueExp("one"), new ValueExp("two"),
						new ValueExp("three"))).getCode());
	}

	@Test
	public void testArrayExpWithEllipse() {
		assertEquals("new java.lang.String[] { \"one\", \"two\", \"three\" }", new ArrayExp(String.class, new ValueExp(
				"one"), new ValueExp("two"), new ValueExp("three")).getCode());
	}

	@Test
	public void testArrayExpEmpty() {
		assertEquals("new java.lang.String[0]", new ArrayExp(String.class).getCode());
	}

	@Test
	public void testForParamTypes() throws Exception {
		assertEquals("new java.lang.Class[] { int.class, java.lang.String.class }", ArrayExp.forParamTypes(dummyMethod)
				.getCode());
	}

	@Test
	public void testForArgs() throws Exception {
		assertEquals("new java.lang.Object[] { " + ObjectConverter.toObject.getCode() + "($1), "
				+ ObjectConverter.toObject.getCode() + "($2) }", ArrayExp.forArgs(dummyMethod).getCode());
	}

	public static class DummyClass {
		public void dummyMethod(int arg1, String arg2) {
		}
	}
}
