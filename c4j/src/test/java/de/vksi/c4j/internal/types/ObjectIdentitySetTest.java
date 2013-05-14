package de.vksi.c4j.internal.types;

import static org.junit.Assert.assertFalse;

import org.junit.Test;

import de.vksi.c4j.internal.types.ObjectIdentitySet;

public class ObjectIdentitySetTest {
	@Test
	public void testAddTwiceRemoveOnce() throws Exception {
		ObjectIdentitySet set = new ObjectIdentitySet();
		Object element = new Object();
		set.add(element);
		set.add(element);
		set.remove(element);
		assertFalse(set.contains(element));
	}
}
