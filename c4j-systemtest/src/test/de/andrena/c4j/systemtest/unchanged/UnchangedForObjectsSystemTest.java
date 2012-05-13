package de.andrena.c4j.systemtest.unchanged;

import static de.andrena.c4j.Condition.post;
import static de.andrena.c4j.Condition.unchanged;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import de.andrena.c4j.ContractReference;
import de.andrena.c4j.Pure;
import de.andrena.c4j.Target;
import de.andrena.c4j.systemtest.MutableString;
import de.andrena.c4j.systemtest.TransformerAwareRule;

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

	@Test(expected = AssertionError.class)
	public void testFieldIsChanged() {
		target.fieldIsChanged();
	}

	@Test(expected = AssertionError.class)
	public void testMethodIsChanged() {
		target.methodIsChanged();
	}

	@Test(expected = AssertionError.class)
	public void testParameterIsChanged() {
		target.parameterIsChanged(new MutableString());
	}

	@Test(expected = AssertionError.class)
	public void testParameter5IsChanged() {
		target.parameter5IsChanged(0, 0, 0, 0, new MutableString());
	}

	@Test(expected = AssertionError.class)
	public void testParameterArrayIsChanged() {
		target.parameterArrayIsChanged(new MutableString[] { new MutableString() });
	}

	@Test(expected = AssertionError.class)
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
	public static class TargetClass {
		protected MutableString field = new MutableString();

		@Pure
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

	public static class ContractClass extends TargetClass {
		@Target
		private TargetClass target;

		@Override
		public void fieldRemainsUnchanged() {
			if (post()) {
				assert unchanged(target.field);
			}
		}

		@Override
		public void methodRemainsUnchanged() {
			if (post()) {
				assert unchanged(target.method());
			}
		}

		@Override
		public void parameterRemainsUnchanged(MutableString param) {
			if (post()) {
				assert unchanged(param);
			}
		}

		@Override
		public void fieldIsChanged() {
			if (post()) {
				assert unchanged(target.field);
			}
		}

		@Override
		public void methodIsChanged() {
			if (post()) {
				assert unchanged(target.method());
			}
		}

		@Override
		public void parameterIsChanged(MutableString param) {
			if (post()) {
				assert unchanged(param);
			}
		}

		@Override
		public void parameter5IsChanged(int a, int b, int c, int d, MutableString param) {
			if (post()) {
				assert unchanged(param);
			}
		}

		@Override
		public void parameterArrayIsChanged(MutableString[] param) {
			if (post()) {
				assert unchanged(param);
			}
		}

		@Override
		public void parameterArrayIsReplaced(MutableString[] param) {
			if (post()) {
				assert unchanged(param);
			}
		}

		@Override
		public void fieldIsReplaced() {
			if (post()) {
				assert unchanged(target.field);
			}
		}

		@Override
		public void methodIsReplaced() {
			if (post()) {
				assert unchanged(target.method());
			}
		}

		@Override
		public void parameterIsReplaced(MutableString param) {
			if (post()) {
				assert unchanged(param);
			}
		}
	}

}
