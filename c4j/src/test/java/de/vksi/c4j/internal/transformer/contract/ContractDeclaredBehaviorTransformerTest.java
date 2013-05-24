package de.vksi.c4j.internal.transformer.contract;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;

import java.util.HashSet;
import java.util.Set;

import javassist.ClassPool;
import javassist.CtBehavior;
import javassist.CtClass;

import org.junit.Test;

import de.vksi.c4j.internal.contracts.ContractInfo;

public class ContractDeclaredBehaviorTransformerTest {

	@Test
	public void testTransform() throws Exception {
		DummyContractDeclaredBehaviorTransformer transformer = new DummyContractDeclaredBehaviorTransformer();
		CtClass contractClass = ClassPool.getDefault().get(ContractClass.class.getName());
		transformer.transform(null, contractClass);
		assertThat(transformer.getCalledBehaviors(), containsInAnyOrder(contractClass
				.getDeclaredConstructor(new CtClass[0]), contractClass.getDeclaredMethod("methodOne"), contractClass
				.getDeclaredMethod("methodTwo")));
	}

	public class DummyContractDeclaredBehaviorTransformer extends ContractDeclaredBehaviorTransformer {
		private Set<CtBehavior> calledBehaviors = new HashSet<CtBehavior>();

		@Override
		public void transform(ContractInfo contractInfo, CtBehavior contractBehavior) throws Exception {
			calledBehaviors.add(contractBehavior);
		}

		public Set<CtBehavior> getCalledBehaviors() {
			return calledBehaviors;
		}
	}

	@SuppressWarnings("unused")
	private static class ContractClass {
		public ContractClass() {
		}

		public void methodOne() {
		}

		public void methodTwo() {
		}
	}
}
