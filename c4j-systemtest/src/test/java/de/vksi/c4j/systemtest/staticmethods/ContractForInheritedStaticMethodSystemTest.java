package de.vksi.c4j.systemtest.staticmethods;

import static de.vksi.c4j.Condition.old;
import static de.vksi.c4j.Condition.postCondition;
import static de.vksi.c4j.Condition.preCondition;

import org.junit.Rule;
import org.junit.Test;

import de.vksi.c4j.ContractReference;
import de.vksi.c4j.systemtest.TransformerAwareRule;

public class ContractForInheritedStaticMethodSystemTest {
	@Rule
	public TransformerAwareRule transformerAwareRule = new TransformerAwareRule();

	@Test
	public void testStaticMethodPrePostAndOld() {
		TargetClass.main("test");
	}

	@Test(expected = AssertionError.class)
	public void testStaticMethodPreFailing() {
		TargetClass.main();
	}

	@Test(expected = AssertionError.class)
	public void testStaticMethodPostFailing() {
		TargetClass.main("test1", "test2");
	}

	@SuppressWarnings("unused")
	private static class SuperClass {
		protected static int numCalls = 0;

		public static void main(String... args) {
			numCalls += args.length;
		}

		public static int staticMethodWithReturnValue() {
			return 0;
		}
	}

	@ContractReference(ContractClass.class)
	private static class TargetClass extends SuperClass {
	}

	private static class ContractClass extends TargetClass {
		public static void main(String... args) {
			if (preCondition()) {
				assert args.length > 0;
			}
			if (postCondition()) {
				assert TargetClass.numCalls == old(TargetClass.numCalls) + 1;
			}
		}
	}
}
