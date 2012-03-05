package de.andrena.c4j.systemtest.unchanged;

import static de.andrena.c4j.Condition.post;
import static de.andrena.c4j.Condition.unchanged;

import java.util.HashSet;
import java.util.Set;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import de.andrena.c4j.Contract;
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

	@Test(expected = AssertionError.class)
	public void testFieldIsChanged() {
		target.fieldIsChanged();
	}

	@Test(expected = AssertionError.class)
	public void testMethodIsChanged() {
		target.methodIsChanged();
	}

	@Test(expected = AssertionError.class)
	public void testFieldIsReplaced() {
		target.fieldIsReplaced();
	}

	@Test(expected = AssertionError.class)
	public void testMethodIsReplaced() {
		target.methodIsReplaced();
	}

	@Contract(ContractClass.class)
	public static class TargetClass {
		protected Set<String> field = new HashSet<String>();

		@Pure
		public Set<String> method() {
			return field;
		}

		public void fieldRemainsUnchanged() {
		}

		public void methodRemainsUnchanged() {
		}

		public void fieldIsChanged() {
			field.add("abc");
		}

		public void methodIsChanged() {
			field.add("abc");
		}

		public void fieldIsReplaced() {
			field = new HashSet<String>();
		}

		public void methodIsReplaced() {
			field = new HashSet<String>();
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
	}
}
