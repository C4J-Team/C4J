package de.andrena.c4j.systemtest.unchanged;

import static de.andrena.c4j.Condition.post;
import static de.andrena.c4j.Condition.unchanged;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import de.andrena.c4j.ContractReference;
import de.andrena.c4j.Pure;
import de.andrena.c4j.Target;
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
		target.parameterRemainsUnchanged(new SetLike());
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
		target.parameterIsChanged(new SetLike());
	}

	@Test(expected = AssertionError.class)
	public void testParameter5IsChanged() {
		target.parameter5IsChanged(0, 0, 0, 0, new SetLike());
	}

	@Test(expected = AssertionError.class)
	public void testParameterArrayIsChanged() {
		target.parameterArrayIsChanged(new SetLike[] { new SetLike() });
	}

	// failing, see https://github.com/C4J-Team/C4J/issues/1
	@Test(expected = AssertionError.class)
	public void testParameterArrayIsReplaced() {
		target.parameterArrayIsReplaced(new SetLike[] { new SetLike() });
	}

	@Test(expected = AssertionError.class)
	public void testFieldIsReplaced() {
		target.fieldIsReplaced();
	}

	@Test(expected = AssertionError.class)
	public void testMethodIsReplaced() {
		target.methodIsReplaced();
	}

	@Test
	public void testParameterIsReplaced() {
		target.parameterIsReplaced(new SetLike());
	}

	@ContractReference(ContractClass.class)
	public static class TargetClass {
		protected SetLike field = new SetLike();

		@Pure
		public SetLike method() {
			return field;
		}

		public void fieldRemainsUnchanged() {
		}

		public void methodRemainsUnchanged() {
		}

		public void parameterRemainsUnchanged(SetLike param) {
		}

		public void fieldIsChanged() {
			field.setValue("abc");
		}

		public void methodIsChanged() {
			field.setValue("abc");
		}

		public void parameterIsChanged(SetLike param) {
			param.setValue("abc");
		}

		public void parameter5IsChanged(int a, int b, int c, int d, SetLike param) {
			param.setValue("abc");
		}

		public void parameterArrayIsChanged(SetLike[] param) {
			param[0].setValue("abc");
		}

		public void parameterArrayIsReplaced(SetLike[] param) {
			param[0] = new SetLike();
		}

		public void fieldIsReplaced() {
			field = new SetLike();
		}

		public void methodIsReplaced() {
			field = new SetLike();
		}

		public void parameterIsReplaced(SetLike param) {
			param = new SetLike();
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
		public void parameterRemainsUnchanged(SetLike param) {
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
		public void parameterIsChanged(SetLike param) {
			if (post()) {
				assert unchanged(param);
			}
		}

		@Override
		public void parameter5IsChanged(int a, int b, int c, int d, SetLike param) {
			if (post()) {
				assert unchanged(param);
			}
		}

		@Override
		public void parameterArrayIsChanged(SetLike[] param) {
			if (post()) {
				assert unchanged((Object) param);
			}
		}

		@Override
		public void parameterArrayIsReplaced(SetLike[] param) {
			if (post()) {
				assert unchanged((Object) param);
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
		public void parameterIsReplaced(SetLike param) {
			if (post()) {
				assert unchanged(param);
			}
		}
	}

	public static class SetLike {
		private String value = "";

		public void setValue(String value) {
			this.value = value;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((value == null) ? 0 : value.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			SetLike other = (SetLike) obj;
			if (value == null) {
				if (other.value != null)
					return false;
			} else if (!value.equals(other.value))
				return false;
			return true;
		}

	}

}
