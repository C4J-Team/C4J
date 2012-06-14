package de.vksi.c4j;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import de.vksi.c4j.PureRegistryException;
import de.vksi.c4j.PureRegistryType;

public class PureRegistryTypeTest {
	private static final int NUMBER_OF_METHODS_IN_INVESTIGATED_CLASS = 8;
	@SuppressWarnings("rawtypes")
	private static final Class<ArrayListDummyForPureRegistryTypeTest> INVESTIGATED_CLASS = ArrayListDummyForPureRegistryTypeTest.class;
	private PureRegistryType pureType;

	@Before
	public void before() {
		pureType = new PureRegistryType(INVESTIGATED_CLASS);
	}

	@Test
	public void testPureMethod() throws Throwable {
		pureType.pureMethod("size");
		assertEquals(1, pureType.getPureMethods().size());
		assertEquals(0, pureType.getUnpureMethods().size());
		assertTrue(pureType.getPureMethods().contains(
				INVESTIGATED_CLASS.getMethod("size")));
	}

	@Test(expected = PureRegistryException.class)
	public void testPureMethodNotFound() throws Throwable {
		pureType.pureMethod("size123");
	}

	@Test(expected = PureRegistryException.class)
	public void testPureMethodNull() throws Throwable {
		pureType.pureMethod(null);
	}

	@Test
	public void testPureMethodProtected() throws Throwable {
		pureType.pureMethod("removeRange", int.class, int.class);
		assertEquals(1, pureType.getPureMethods().size());
		assertEquals(0, pureType.getUnpureMethods().size());
		assertTrue(pureType.getPureMethods().contains(
				INVESTIGATED_CLASS.getDeclaredMethod("removeRange", int.class,
						int.class)));
	}

	@Test
	public void testPureMethods() throws Throwable {
		pureType.pureMethods("toArray");
		assertEquals(2, pureType.getPureMethods().size());
		assertEquals(0, pureType.getUnpureMethods().size());
		assertTrue(pureType.getPureMethods().contains(
				INVESTIGATED_CLASS.getMethod("toArray")));
		assertTrue(pureType.getPureMethods().contains(
				INVESTIGATED_CLASS.getMethod("toArray", Object[].class)));
	}

	@Test(expected = PureRegistryException.class)
	public void testPureMethodsNotFound() throws Throwable {
		pureType.pureMethods("toArray123");
	}

	@Test(expected = PureRegistryException.class)
	public void testPureMethodsNull() throws Throwable {
		pureType.pureMethods(null);
	}

	@Test
	public void testPureMethodsPrivate() throws Throwable {
		pureType.pureMethods("fastRemove");
		assertEquals(1, pureType.getPureMethods().size());
		assertEquals(0, pureType.getUnpureMethods().size());
		assertTrue(pureType.getPureMethods().contains(
				INVESTIGATED_CLASS.getDeclaredMethod("fastRemove", int.class)));
	}

	@Test
	public void testUnpureMethod() throws Throwable {
		pureType.unpureMethod("add", Object.class);
		assertEquals(0, pureType.getPureMethods().size());
		assertEquals(1, pureType.getUnpureMethods().size());
		assertTrue(pureType.getUnpureMethods().contains(
				INVESTIGATED_CLASS.getMethod("add", Object.class)));
	}

	@Test(expected = PureRegistryException.class)
	public void testUnpureMethodNotFound() throws Throwable {
		pureType.unpureMethod("add");
	}

	@Test(expected = PureRegistryException.class)
	public void testUnpureMethodNull() throws Throwable {
		pureType.unpureMethod(null);
	}

	@Test
	public void testUnpureMethods() throws Throwable {
		pureType.unpureMethods("add");
		assertEquals(0, pureType.getPureMethods().size());
		assertEquals(2, pureType.getUnpureMethods().size());
		assertTrue(pureType.getUnpureMethods().contains(
				INVESTIGATED_CLASS.getMethod("add", Object.class)));
		assertTrue(pureType.getUnpureMethods().contains(
				INVESTIGATED_CLASS.getMethod("add", int.class, Object.class)));
	}

	@Test(expected = PureRegistryException.class)
	public void testUnpureMethodsNotFound() throws Throwable {
		pureType.unpureMethods("add123");
	}

	@Test(expected = PureRegistryException.class)
	public void testUnpureMethodsNull() throws Throwable {
		pureType.unpureMethods(null);
	}

	@Test
	public void testOnlyPureMethods() throws Throwable {
		pureType.onlyPureMethods();
		assertEquals(NUMBER_OF_METHODS_IN_INVESTIGATED_CLASS, pureType
				.getPureMethods().size());
		assertEquals(0, pureType.getUnpureMethods().size());
	}

	@Test(expected = PureRegistryException.class)
	public void testOnlyPureMethodsAndThenPureMethodCall() throws Throwable {
		pureType.onlyPureMethods();
		pureType.pureMethod("get", int.class);
	}

	@Test(expected = PureRegistryException.class)
	public void testOnlyPureMethodsAndThenPureMethodsCall() throws Throwable {
		pureType.onlyPureMethods();
		pureType.pureMethods("get");
	}

	@Test(expected = PureRegistryException.class)
	public void testOnlyPureMethodsAndThenOnlyPureMethodsCall()
			throws Throwable {
		pureType.onlyPureMethods();
		pureType.onlyPureMethods();
	}

	@Test(expected = PureRegistryException.class)
	public void testOnlyPureMethodsAndThenUnpureMethodCall() throws Throwable {
		pureType.onlyPureMethods();
		pureType.unpureMethod("add", Object.class);
	}

	@Test(expected = PureRegistryException.class)
	public void testOnlyPureMethodsAndThenUnpureMethodsCall() throws Throwable {
		pureType.onlyPureMethods();
		pureType.unpureMethods("add");
	}

	@Test(expected = PureRegistryException.class)
	public void testOnlyPureMethodsAndThenOnlyUnpureMethodsCall()
			throws Throwable {
		pureType.onlyPureMethods();
		pureType.onlyUnpureMethods();
	}

	@Test
	public void testOnlyUnpureMethods() throws Throwable {
		pureType.onlyUnpureMethods();
		assertEquals(0, pureType.getPureMethods().size());
		assertEquals(NUMBER_OF_METHODS_IN_INVESTIGATED_CLASS, pureType
				.getUnpureMethods().size());
	}

}
