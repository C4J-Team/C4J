package de.vksi.c4j.systemtest.staticmethods;

import static de.vksi.c4j.Condition.postCondition;
import static de.vksi.c4j.Condition.preCondition;
import static org.junit.Assert.assertEquals;

import org.junit.Rule;
import org.junit.Test;

import de.vksi.c4j.ConstructorContract;
import de.vksi.c4j.ContractReference;
import de.vksi.c4j.systemtest.TransformerAwareRule;

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
	private static class TargetClass {
		private static int numCalls = 0;

		static {
			numCalls = 1;
		}

		public static void main(String... args) {
		}
	}

	private static class ContractClass extends TargetClass {
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
	private static class TargetClassFailingPre {
		private static int numCalls = 0;

		static {
			numCalls = 1;
		}

		public static void main(String... args) {
		}
	}

	private static class ContractClassFailingPre extends TargetClassFailingPre {
		@ConstructorContract
		public static void staticInitializer() {
			if (preCondition()) {
				assert TargetClassFailingPre.numCalls == -1;
			}
			if (postCondition()) {
				assert TargetClassFailingPre.numCalls == 1;
			}
		}
	}

	@ContractReference(ContractClassFailingPost.class)
	private static class TargetClassFailingPost {
		private static int numCalls = 0;

		static {
			numCalls = 1;
		}

		public static void main(String... args) {
		}
	}

	private static class ContractClassFailingPost extends TargetClassFailingPost {
		@ConstructorContract
		public static void staticInitializer() {
			if (preCondition()) {
				assert TargetClassFailingPost.numCalls == 0;
			}
			if (postCondition()) {
				assert TargetClassFailingPost.numCalls == 2;
			}
		}
	}

	@ContractReference(ContractClassWithStaticInitializer.class)
	private static class TargetClassWithoutStaticInitializer {
		public static void main(String... args) {
		}
	}

	private static class ContractClassWithStaticInitializer extends TargetClassWithoutStaticInitializer {
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
	private static class TargetClassWithStaticInitializer {
		public static int STATIC_FIELD;

		static {
			STATIC_FIELD = 1;
		}

		public static void main(String... args) {
		}
	}

	private static class ContractClassWithoutStaticInitializer extends TargetClassWithStaticInitializer {
	}

}