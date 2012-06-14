package de.vksi.c4j.systemtest.contractclassmagic;

import org.junit.Rule;
import org.junit.Test;

import de.vksi.c4j.ContractReference;
import de.vksi.c4j.systemtest.TransformerAwareRule;

public class ContractForNativeOrAbstractMethodSystemTest {
	@Rule
	public TransformerAwareRule transformerAwareRule = new TransformerAwareRule();

	@Test
	public void testContractForNativeMethod() {
		new TargetClass();
	}

	public static class TargetClass implements ObjectSpec {
	}

	@ContractReference(ObjectSpecContract.class)
	public interface ObjectSpec {
		// Object.hashCode() is native
		@Override
		public int hashCode();
	}

	public static class ObjectSpecContract implements ObjectSpec {
		@Override
		public int hashCode() {
			return 0;
		}
	}

	@Test
	public void testAbstractClass() {
		new TargetClassExtendingAbstract();
	}

	public static class TargetClassExtendingAbstract extends AbstractClassExtendingAbstract {
		@Override
		public void abstractMethod() {
		}
	}

	@ContractReference(AbstractClassExtendingAbstractContract.class)
	public static abstract class AbstractClassExtendingAbstract extends AbstractClass {
	}

	public static class AbstractClassExtendingAbstractContract extends AbstractClassExtendingAbstract {
		@Override
		public void abstractMethod() {
		}
	}

	@ContractReference(AbstractClassContract.class)
	public static abstract class AbstractClass {
		public abstract void abstractMethod();
	}

	public static class AbstractClassContract extends AbstractClass {
		@Override
		public void abstractMethod() {
		}
	}
}
