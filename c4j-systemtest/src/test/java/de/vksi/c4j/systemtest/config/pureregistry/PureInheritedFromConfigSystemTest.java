package de.vksi.c4j.systemtest.config.pureregistry;

import static de.vksi.c4j.Condition.ignored;
import static de.vksi.c4j.Condition.postCondition;

import org.junit.Rule;
import org.junit.Test;

import de.vksi.c4j.ContractReference;
import de.vksi.c4j.Target;
import de.vksi.c4j.systemtest.TransformerAwareRule;

public class PureInheritedFromConfigSystemTest {
	@Rule
	public TransformerAwareRule transformerAware = new TransformerAwareRule();

	@Test
	public void pureInheritedFromConfig() throws Exception {
		new TargetClass().equals(null);
	}

	@ContractReference(ContractClass.class)
	private static class TargetClass {
	}

	private static class ContractClass extends TargetClass {
		@Target
		private TargetClass target;

		@Override
		public boolean equals(Object obj) {
			if (postCondition()) {
				// equals @Pure-ness should be inherited from Object, defined in the Pure Registry
				assert target.equals(target) : "reflexive";
			}
			return (Boolean) ignored();
		}
	}

}
