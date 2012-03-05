package de.andrena.c4j;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;

import org.junit.Test;

public class PureRegistryTest {
	@Test
	public void testWithArrayListAndString() throws Throwable {
		PureRegistry registry = PureRegistry.union(
				PureRegistry.register(ArrayList.class)
						.pureMethod("size")
						.pureMethod("get", int.class)
						.unpureMethods("add"),
				PureRegistry.register(String.class)
						.onlyPureMethods());
		assertEquals(72, registry.getPureMethods().size());
		assertEquals(2, registry.getUnpureMethods().size());
	}
}
