package de.andrena.c4j.systemtest.staticmethods;

import static de.andrena.c4j.Condition.postCondition;
import static de.andrena.c4j.Condition.preCondition;
import static org.junit.Assert.assertEquals;

import org.junit.Rule;
import org.junit.Test;

import de.andrena.c4j.ContractReference;
import de.andrena.c4j.systemtest.TransformerAwareRule;

public class ContractForStaticInitializerSystemTest {
	@Rule
	public TransformerAwareRule transformerAwareRule = new TransformerAwareRule();

	@Test
	public void testStaticInitializer() {
		TargetClass.main();
	}

	@Test(expected = AssertionError.class)
	public void testStaticInitializerPreFailing() {
		TargetClassFailingPre.main();
	}

	@Test(expected = AssertionError.class)
	public void testStaticInitializerPostFailing() {
		TargetClassFailingPost.main();
	}

	@Test
	public void testStaticInitializerOfContractClassExecutedWhenTargetClassDoesntHaveOne() {
		TargetClassWithoutStaticInitializer.main();
		assertEquals(1, ContractClassWithStaticInitializer.STATIC_FIELD);
	}

	@Test
	public void testStaticInitializerOfTargetClassWhenContractClassDoesntHaveOne() {
		TargetClassWithStaticInitializer.main();
	}

	@ContractReference(ContractClass.class)
	public static class TargetClass {
		private static int numCalls = 0;

		static {
			numCalls = 1;
		}

		public static void main(String... args) {
		}
	}

	public static class ContractClass extends TargetClass {
		static {
			if (preCondition()) {
				assert TargetClass.numCalls == 0;
			}
			if (postCondition()) {
				assert TargetClass.numCalls == 1;
			}
		}
	}

	@ContractReference(ContractClassFailingPre.class)
	public static class TargetClassFailingPre {
		private static int numCalls = 0;

		static {
			numCalls = 1;
		}

		public static void main(String... args) {
		}
	}

	public static class ContractClassFailingPre extends TargetClassFailingPre {
		static {
			if (preCondition()) {
				assert TargetClassFailingPre.numCalls == -1;
			}
			if (postCondition()) {
				assert TargetClassFailingPre.numCalls == 1;
			}
		}
	}

	@ContractReference(ContractClassFailingPost.class)
	public static class TargetClassFailingPost {
		private static int numCalls = 0;

		static {
			numCalls = 1;
		}

		public static void main(String... args) {
		}
	}

	public static class ContractClassFailingPost extends TargetClassFailingPost {
		static {
			if (preCondition()) {
				assert TargetClassFailingPost.numCalls == 0;
			}
			if (postCondition()) {
				assert TargetClassFailingPost.numCalls == 2;
			}
		}
	}

	@ContractReference(ContractClassWithStaticInitializer.class)
	public static class TargetClassWithoutStaticInitializer {
		public static void main(String... args) {
		}
	}

	public static class ContractClassWithStaticInitializer extends TargetClassWithoutStaticInitializer {
		public static int STATIC_FIELD;

		static {
			STATIC_FIELD = 1;
		}

		public static void main(String... args) {
			if (preCondition()) {
				assert args.length == 0;
			}
		}
	}

	@ContractReference(ContractClassWithoutStaticInitializer.class)
	public static class TargetClassWithStaticInitializer {
		public static int STATIC_FIELD;

		static {
			STATIC_FIELD = 1;
		}

		public static void main(String... args) {
		}
	}

	public static class ContractClassWithoutStaticInitializer extends TargetClassWithStaticInitializer {
	}

}