package de.andrena.next.internal.util;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

public class ObjectMapperTest {
	private ObjectMapper<String, DummyObject> mapper;
	private DummyObject dummy1;
	private DummyObject dummy2;

	@Before
	public void before() {
		mapper = new ObjectMapper<String, DummyObject>(false);
		dummy1 = new DummyObject(1, "dummy1");
		dummy2 = new DummyObject(1, "dummy2");
	}
	
	@Test
	public void testStoreOneObject() {
		mapper.put(dummy1, "test", dummy1);
		assertTrue(mapper.contains(dummy1, "test"));
		assertTrue(mapper.get(dummy1, "test") == dummy1);
	}
	
	@Test
	public void testStoreTwoObjectsSameHashcode() {
		mapper.put(dummy1, "test", dummy1);
		mapper.put(dummy2, "test", dummy2);
		assertTrue(mapper.contains(dummy1, "test"));
		assertTrue(mapper.contains(dummy2, "test"));
		assertTrue(mapper.get(dummy1, "test") == dummy1);
		assertTrue(mapper.get(dummy2, "test") == dummy2);
	}
	
	@Test
	public void testStoreOneObjectAndAnotherIsntContained() {
		mapper.put(dummy1, "test", dummy1);
		assertTrue(mapper.contains(dummy1, "test"));
		assertFalse(mapper.contains(dummy2, "test"));
	}
	
	@Test
	public void testStoreOneObjectWithTwoDifferentValues() {
		mapper.put(dummy1, "test", dummy1);
		mapper.put(dummy1, "test", dummy2);
		assertTrue(mapper.contains(dummy1, "test"));
		assertTrue(mapper.get(dummy1, "test") == dummy2);
	}
	
	@Test
	public void testStoreOneObjectWithTwoDifferentKeys() {
		mapper.put(dummy1, "test1", dummy1);
		mapper.put(dummy1, "test2", dummy2);
		assertTrue(mapper.get(dummy1, "test1") == dummy1);
		assertTrue(mapper.get(dummy1, "test2") == dummy2);
	}
	
	private class DummyObject {
		private int field;
		private String identifier;
		
		public DummyObject(int field, String identifier) {
			this.field = field;
			this.identifier = identifier;
		}

		@Override
		public int hashCode() {
			return field;
		}
		@Override
		public boolean equals(Object obj) {
			return true;
		}

		@Override
		public String toString() {
			return "DummyObject [identifier=" + identifier + "]";
		}
	}
}
