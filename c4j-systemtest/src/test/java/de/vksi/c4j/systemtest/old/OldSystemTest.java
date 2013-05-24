package de.vksi.c4j.systemtest.old;

import static de.vksi.c4j.Condition.old;
import static de.vksi.c4j.Condition.postCondition;

import java.io.InputStream;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import de.vksi.c4j.ContractReference;
import de.vksi.c4j.Pure;
import de.vksi.c4j.Target;
import de.vksi.c4j.systemtest.TransformerAwareRule;

public class OldSystemTest {
	@Rule
	public TransformerAwareRule transformerAware = new TransformerAwareRule();

	private DummyClass dummy;

	@Before
	public void before() {
		dummy = new DummyClass();
	}

	@Test
	public void testOldWithField() {
		dummy.setValue(5);
		dummy.incrementValueCheckField();
	}

	@Test
	public void testOldWithMethod() {
		dummy.setValue(5);
		dummy.incrementValueCheckMethod();
	}

	@ContractReference(DummyContract.class)
	private static class DummyClass {
		protected int value;
		protected OtherClass otherValue;

		public void setValue(int value) {
			this.value = value;
		}

		@Pure
		public int getValue() {
			return value;
		}

		public void incrementValueCheckField() {
			value++;
		}

		public void incrementValueCheckMethod() {
			value++;
		}
	}

	private static class OtherClass {
		public InputStream stream;

		public int otherMethod() {
			return 0;
		}
	}

	private static class DummyContract extends DummyClass {
		@Target
		private DummyClass target;

		@Override
		public void incrementValueCheckField() {
			if (postCondition()) {
				assert target.value == old(target.value) + 1;
			}
		}

		@Override
		public void incrementValueCheckMethod() {
			if (postCondition()) {
				assert target.getValue() == old(target.getValue()) + 1;
			}
		}
	}

	@Test
	public void testDeepOldAccess() {
		new SubClass().method();
	}

	@ContractReference(SubClassContract.class)
	private static class SubClass extends SuperClass {
	}

	private static class SubClassContract extends SubClass {
		@Target
		private SubClass target;

		@Override
		public int method() {
			if (postCondition()) {
				assert old(target.field) == 0;
				assert old(target.method()) == 0;
			}
			return 0;
		}
	}

	private static class SuperClass {
		protected int field;

		@Pure
		public int method() {
			return 0;
		}
	}

	@Test
	public void testSubClassWithoutMethod() {
		new SubClassWithoutMethod().method();
	}

	@ContractReference(SubClassWithoutMethodContract.class)
	private static class SubClassWithoutMethod extends SuperClassWithOld {
	}

	private static class SubClassWithoutMethodContract extends SubClassWithoutMethod {
	}

	@ContractReference(SuperClassWithOldContract.class)
	private static class SuperClassWithOld {
		@Pure
		public int method() {
			return 0;
		}
	}

	private static class SuperClassWithOldContract extends SuperClassWithOld {
		@Target
		private SuperClassWithOld target;

		@Override
		public int method() {
			if (postCondition()) {
				assert old(target.method()) == target.method();
			}
			return 0;
		}
	}
}
