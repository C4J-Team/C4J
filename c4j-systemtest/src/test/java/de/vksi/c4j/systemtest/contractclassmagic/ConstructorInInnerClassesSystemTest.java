package de.vksi.c4j.systemtest.contractclassmagic;

import static de.vksi.c4j.Condition.preCondition;
import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import de.vksi.c4j.AllowPureAccess;
import de.vksi.c4j.ConstructorContract;
import de.vksi.c4j.ContractReference;
import de.vksi.c4j.systemtest.TransformerAwareRule;

public class ConstructorInInnerClassesSystemTest {
	@Rule
	public TransformerAwareRule transformerAware = new TransformerAwareRule();

	@AllowPureAccess
	private static int expectedNumCalls;

	@Before
	public void before() {
		expectedNumCalls = 0;
	}

	@Test
	public void testConstructorInInnerClass() {
		new InnerClass();
		assertEquals(1, expectedNumCalls);
	}

	@Test
	public void testConstructorInStaticInnerClass() {
		new StaticInnerClass();
		assertEquals(1, expectedNumCalls);
	}

	@Test
	public void testConstructorInAnonymousClass() {
		new SuperClass() {
		};
		assertEquals(1, expectedNumCalls);
	}

	@Test
	public void testConstructorInLocalClass() {
		class LocalClass extends SuperClass {
		}
		new LocalClass();
		assertEquals(1, expectedNumCalls);
	}

	@Test
	public void testConstructorWithParameter() {
		new InnerClassWithParameter(3);
		assertEquals(1, expectedNumCalls);
	}

	public class InnerClassWithParameter extends SuperClassWithParameter {
		public InnerClassWithParameter(int value) {
			super(value);
		}
	}

	@ContractReference(SuperClassWithParameterContract.class)
	private static class SuperClassWithParameter {
		public SuperClassWithParameter(int value) {
		}
	}

	@SuppressWarnings("unused")
	private static class SuperClassWithParameterContract extends SuperClassWithParameter {
		public SuperClassWithParameterContract() {
			super(0);
		}

		@ConstructorContract
		public void constructor(int value) {
			if (preCondition()) {
				expectedNumCalls++;
			}
		}
	}

	private static class StaticInnerClass extends SuperClass {
	}

	private class InnerClass extends SuperClass {
	}

	@ContractReference(SuperClassContract.class)
	private static class SuperClass {
		public SuperClass() {
		}
	}

	@SuppressWarnings("unused")
	private static class SuperClassContract extends SuperClass {
		@ConstructorContract
		public void constructor() {
			if (preCondition()) {
				expectedNumCalls++;
			}
		}
	}
}
