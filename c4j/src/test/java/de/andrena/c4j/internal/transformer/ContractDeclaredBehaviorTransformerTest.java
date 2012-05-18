package de.andrena.c4j.internal.transformer;

import static org.mockito.Mockito.mock;
import javassist.ClassPool;
import javassist.CtBehavior;
import javassist.CtClass;

import org.apache.log4j.Logger;
import org.junit.Test;

import de.andrena.c4j.internal.transformer.ContractDeclaredBehaviorTransformer;
import de.andrena.c4j.internal.util.ContractRegistry.ContractInfo;

public class ContractDeclaredBehaviorTransformerTest {

	private Logger loggerMock;

	@Test
	public void testTransform() throws Exception {
		loggerMock = mock(Logger.class);
		ContractDeclaredBehaviorTransformer transformer = new DummyContractDeclaredBehaviorTransformer();
		CtClass contractClass = ClassPool.getDefault().get(ContractClass.class.getName());
		transformer.transform(null, contractClass);
	}

	public class DummyContractDeclaredBehaviorTransformer extends ContractDeclaredBehaviorTransformer {
		@Override
		public void transform(ContractInfo contractInfo, CtBehavior contractBehavior) throws Exception {
			loggerMock.info(contractBehavior);
		}
	}

	public static class ContractClass {
		public ContractClass() {
		}

		public void methodOne() {
		}

		public void methodTwo() {
		}
	}
}
