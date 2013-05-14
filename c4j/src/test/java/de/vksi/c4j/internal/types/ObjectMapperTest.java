package de.vksi.c4j.internal.types;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;

import org.junit.Before;
import org.junit.Test;

import de.vksi.c4j.internal.types.ObjectMapper;import de.vksi.c4j.internal.util.TestUtil;


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

	@Test
	public void testSize() {
		assertEquals(0, mapper.size());
		mapper.put(dummy1, "test1", dummy1);
		mapper.put(dummy1, "test2", dummy1);
		mapper.put(dummy2, "test2", dummy1);
		assertEquals(2, mapper.size());
	}

	@Test
	public void testCleanup() throws Exception {
		DummyObject dummy = new DummyObject(3, "abc");
		ReferenceQueue<DummyObject> referenceQueue = new ReferenceQueue<DummyObject>();
		@SuppressWarnings("unused")
		WeakReference<DummyObject> dummyReference = new WeakReference<DummyObject>(dummy, referenceQueue);
		DummyObject otherDummy = new DummyObject(4, "def");
		mapper.put(dummy, "test", otherDummy);
		dummy = null;
		TestUtil.forceGarbageCollection();
		// Second referenceQueue is necessary, as there is a slight delay before the released WeakReference can
		// be pulled from the queue. This delay is simulated with an own referenceQueue in the test.
		referenceQueue.remove();
		mapper.cleanup();
		assertEquals(0, mapper.size());
	}

	@Test
	public void testCleanupBeforePut() throws Exception {
		DummyObject dummy = new DummyObject(3, "abc");
		ReferenceQueue<DummyObject> referenceQueue = new ReferenceQueue<DummyObject>();
		@SuppressWarnings("unused")
		WeakReference<DummyObject> dummyReference = new WeakReference<DummyObject>(dummy, referenceQueue);
		DummyObject otherDummy = new DummyObject(4, "def");
		mapper.put(dummy, "test", otherDummy);
		dummy = null;
		TestUtil.forceGarbageCollection();
		referenceQueue.remove();
		mapper.put(otherDummy, "test2", otherDummy);
		assertEquals(1, mapper.size());
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
