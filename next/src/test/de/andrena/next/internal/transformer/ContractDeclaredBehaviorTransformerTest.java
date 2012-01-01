package de.andrena.next.internal.transformer;

import javassist.ClassPool;
import javassist.CtBehavior;
import javassist.CtClass;

import org.apache.log4j.Logger;
import org.junit.Test;

import de.andrena.next.internal.ContractRegistry.ContractInfo;
import static org.mockito.Mockito.mock;

public class ContractDeclaredBehaviorTransformerTest {

	private Logger loggerMock;

	@Test
	public void testTransform() throws Exception {
		loggerMock = mock(Logger.class);
		ContractDeclaredBehaviorTransformer transformer = new DummyContractDeclaredBehaviorTransformer();
		CtClass contractClass = ClassPool.getDefault().get(ContractClass.class.getName());
		for (CtBehavior beh : contractClass.getDeclaredBehaviors()) {
			System.out.println(beh.getLongName());
		}
		transformer.transform(null, contractClass);
	}

	public class DummyContractDeclaredBehaviorTransformer extends ContractDeclaredBehaviorTransformer {
		@Override
		public void transform(ContractInfo contractInfo, CtBehavior contractBehavior) throws Exception {
			System.out.println(contractBehavior.getName());
			loggerMock.info(contractBehavior);
		}
	}

	public static class ContractClass {
		{
			System.out.println("initializer called");
		}

		public ContractClass() {
		}

		public void methodOne() {
		}

		public void methodTwo() {
		}
	}
}
