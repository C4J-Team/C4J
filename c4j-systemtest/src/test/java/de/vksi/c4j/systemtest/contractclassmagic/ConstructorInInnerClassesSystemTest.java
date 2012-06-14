package de.vksi.c4j.systemtest.contractclassmagic;

import static de.vksi.c4j.Condition.preCondition;
import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import de.vksi.c4j.AllowPureAccess;
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
		assertEquals(2, expectedNumCalls);
	}

	@Test
	public void testConstructorInStaticInnerClass() {
		new StaticInnerClass();
		assertEquals(2, expectedNumCalls);
	}

	@Test
	public void testConstructorInAnonymousClass() {
		new SuperClass() {
		};
		assertEquals(2, expectedNumCalls);
	}

	@Test
	public void testConstructorInLocalClass() {
		class LocalClass extends SuperClass {
		}
		new LocalClass();
		assertEquals(2, expectedNumCalls);
	}

	@Test
	public void testConstructorWithParameter() {
		new InnerClassWithParameter(3);
		assertEquals(2, expectedNumCalls);
	}

	public class InnerClassWithParameter extends SuperClassWithParameter {
		public InnerClassWithParameter(int value) {
			super(value);
		}
	}

	@ContractReference(SuperClassWithParameterContract.class)
	public static class SuperClassWithParameter {
		public SuperClassWithParameter(int value) {
		}
	}

	public static class SuperClassWithParameterContract extends SuperClassWithParameter {
		public SuperClassWithParameterContract(int value) {
			super(value);
			if (preCondition()) {
				expectedNumCalls++;
			}
		}
	}

	public static class StaticInnerClass extends SuperClass {
	}

	public class InnerClass extends SuperClass {
	}

	@ContractReference(SuperClassContract.class)
	public static class SuperClass {
	}

	public static class SuperClassContract extends SuperClass {
		public SuperClassContract() {
			if (preCondition()) {
				expectedNumCalls++;
			}
		}
	}
}
