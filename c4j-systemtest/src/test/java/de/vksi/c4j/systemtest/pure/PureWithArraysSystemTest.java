package de.vksi.c4j.systemtest.pure;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import de.vksi.c4j.Pure;
import de.vksi.c4j.systemtest.MutableString;
import de.vksi.c4j.systemtest.TransformerAwareRule;

public class PureWithArraysSystemTest {
	@Rule
	public TransformerAwareRule transformerAware = new TransformerAwareRule();

	private DummyClass dummy;

	@Before
	public void before() {
		dummy = new DummyClass();
	}

	@Test
	public void testGetIntArray() {
		dummy.getIntArray();
	}

	@Test
	public void testGetFirstInt() {
		dummy.getFirstInt();
	}

	@Test(expected = AssertionError.class)
	public void testSetFirstInt() {
		dummy.setFirstInt();
	}

	@Test
	public void testGetStringArray() {
		dummy.getStringArray();
	}

	@Test
	public void testGetFirstString() {
		dummy.getFirstString();
	}

	@Test(expected = AssertionError.class)
	public void testSetFirstString() {
		dummy.setFirstString();
	}

	@Test(expected = AssertionError.class)
	public void testChangeFirstString() {
		dummy.changeFirstString();
	}

	public static class DummyClass {
		private int[] intArray = { 1, 2, 3 };
		private MutableString[] stringArray = { new MutableString("A"), new MutableString("B"), new MutableString("C") };

		@Pure
		public int[] getIntArray() {
			return intArray;
		}

		@Pure
		public int getFirstInt() {
			return intArray[0];
		}

		@Pure
		public void setFirstInt() {
			intArray[0] = 3;
		}

		@Pure
		public MutableString[] getStringArray() {
			return stringArray;
		}

		@Pure
		public MutableString getFirstString() {
			return stringArray[0];
		}

		@Pure
		public void setFirstString() {
			stringArray[0] = new MutableString("D");
		}

		@Pure
		public void changeFirstString() {
			stringArray[0].setValue("E");
		}

	}
}
