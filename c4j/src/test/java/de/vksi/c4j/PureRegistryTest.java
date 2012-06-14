package de.vksi.c4j;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import de.vksi.c4j.PureRegistry;

public class PureRegistryTest {
	@Test
	public void testWithArrayListAndString() throws Throwable {
		PureRegistry registry = PureRegistry.union(
				PureRegistry.register(ArrayListDummyForPureRegistryTypeTest.class)
						.pureMethod("size")
						.pureMethod("get", int.class)
						.unpureMethods("add"),
				PureRegistry.register(StringForPureRegistryTest.class)
						.onlyPureMethods());
		assertEquals(5, registry.getPureMethods().size());
		assertEquals(2, registry.getUnpureMethods().size());
	}
}
