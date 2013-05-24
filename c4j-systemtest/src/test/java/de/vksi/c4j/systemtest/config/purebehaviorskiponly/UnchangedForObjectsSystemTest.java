package de.vksi.c4j.systemtest.config.purebehaviorskiponly;

import static de.vksi.c4j.Condition.postCondition;
import static de.vksi.c4j.Condition.unchanged;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import de.vksi.c4j.ContractReference;
import de.vksi.c4j.Target;
import de.vksi.c4j.systemtest.TransformerAwareRule;

public class UnchangedForObjectsSystemTest {
	@Rule
	public TransformerAwareRule transformerAware = new TransformerAwareRule();

	private TargetClass target;

	@Before
	public void before() {
		target = new TargetClass();
	}

	@Test
	public void testFieldRemainsUnchanged() {
		target.fieldRemainsUnchanged();
	}

	@Test
	public void testMethodRemainsUnchanged() {
		target.methodRemainsUnchanged();
	}

	@Test
	public void testParameterRemainsUnchanged() {
		target.parameterRemainsUnchanged(new MutableString());
	}

	@Test
	public void testFieldIsChanged() {
		target.fieldIsChanged();
	}

	@Test
	public void testMethodIsChanged() {
		target.methodIsChanged();
	}

	@Test
	public void testParameterIsChanged() {
		target.parameterIsChanged(new MutableString());
	}

	@Test
	public void testParameter5IsChanged() {
		target.parameter5IsChanged(0, 0, 0, 0, new MutableString());
	}

	@Test
	public void testParameterArrayIsChanged() {
		target.parameterArrayIsChanged(new MutableString[] { new MutableString() });
	}

	@Test
	public void testParameterArrayIsReplaced() {
		target.parameterArrayIsReplaced(new MutableString[] { new MutableString("original") });
	}

	@Test(expected = AssertionError.class)
	public void testFieldIsReplaced() {
		target.fieldIsReplaced();
	}

	@Test(expected = AssertionError.class)
	public void testMethodIsReplaced() {
		target.methodIsReplaced();
	}

	@Test(expected = AssertionError.class)
	public void testParameterIsReplaced() {
		target.parameterIsReplaced(new MutableString());
	}

	@ContractReference(ContractClass.class)
	private static class TargetClass {
		protected MutableString field = new MutableString();

		public MutableString method() {
			return field;
		}

		public void fieldRemainsUnchanged() {
		}

		public void methodRemainsUnchanged() {
		}

		public void parameterRemainsUnchanged(MutableString param) {
		}

		public void fieldIsChanged() {
			field.setValue("abc");
		}

		public void methodIsChanged() {
			field.setValue("abc");
		}

		public void parameterIsChanged(MutableString param) {
			param.setValue("abc");
		}

		public void parameter5IsChanged(int a, int b, int c, int d, MutableString param) {
			param.setValue("abc");
		}

		public void parameterArrayIsChanged(MutableString[] param) {
			param[0].setValue("abc");
		}

		public void parameterArrayIsReplaced(MutableString[] param) {
			param[0] = new MutableString("replacement");
		}

		public void fieldIsReplaced() {
			field = new MutableString();
		}

		public void methodIsReplaced() {
			field = new MutableString();
		}

		public void parameterIsReplaced(MutableString param) {
			param = new MutableString();
		}
	}

	private static class ContractClass extends TargetClass {
		@Target
		private TargetClass target;

		@Override
		public void fieldRemainsUnchanged() {
			if (postCondition()) {
				assert unchanged(target.field);
			}
		}

		@Override
		public void methodRemainsUnchanged() {
			if (postCondition()) {
				assert unchanged(target.method());
			}
		}

		@Override
		public void parameterRemainsUnchanged(MutableString param) {
			if (postCondition()) {
				assert unchanged(param);
			}
		}

		@Override
		public void fieldIsChanged() {
			if (postCondition()) {
				assert unchanged(target.field);
			}
		}

		@Override
		public void methodIsChanged() {
			if (postCondition()) {
				assert unchanged(target.method());
			}
		}

		@Override
		public void parameterIsChanged(MutableString param) {
			if (postCondition()) {
				assert unchanged(param);
			}
		}

		@Override
		public void parameter5IsChanged(int a, int b, int c, int d, MutableString param) {
			if (postCondition()) {
				assert unchanged(param);
			}
		}

		@Override
		public void parameterArrayIsChanged(MutableString[] param) {
			if (postCondition()) {
				assert unchanged(param);
			}
		}

		@Override
		public void parameterArrayIsReplaced(MutableString[] param) {
			if (postCondition()) {
				assert unchanged(param);
			}
		}

		@Override
		public void fieldIsReplaced() {
			if (postCondition()) {
				assert unchanged(target.field);
			}
		}

		@Override
		public void methodIsReplaced() {
			if (postCondition()) {
				assert unchanged(target.method());
			}
		}

		@Override
		public void parameterIsReplaced(MutableString param) {
			if (postCondition()) {
				assert unchanged(param);
			}
		}
	}

}
