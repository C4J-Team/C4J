package de.andrena.c4j.systemtest.pure;

import org.junit.Rule;
import org.junit.Test;

import de.andrena.c4j.systemtest.TransformerAwareRule;
import de.andrena.c4j.Pure;

public class PureInheritanceSystemTest {
	@Rule
	public TransformerAwareRule transformerAware = new TransformerAwareRule();

	@Test
	public void testPureFromSuperClass() {
		new PureSubClass().pureMethod();
	}

	@Test(expected = AssertionError.class)
	public void testPureFromSuperClassButUnpure() {
		new UnpureSubClass().pureMethod();
	}

	@Test
	public void testPureFromInterface() {
		new ClassHavingPureMethod().pureMethod();
	}

	@Test(expected = AssertionError.class)
	public void testPureFromInterfaceButUnpure() {
		new ClassHavingUnpureMethod().pureMethod();
	}

	@Test
	public void testPureFromIndirectInterface() {
		new ClassHavingIndirectPureMethod().pureMethod();
	}

	@Test(expected = AssertionError.class)
	public void testPureFromIndirectInterfaceButUnpure() {
		new ClassHavingIndirectUnpureMethod().pureMethod();
	}

	public static class PureSubClass extends SuperClass {
		@Override
		public void pureMethod() {
		}
	}

	public static class UnpureSubClass extends SuperClass {
		protected String field;

		@Override
		public void pureMethod() {
			field = "invalid";
		}
	}

	public static class SuperClass {
		@Pure
		public void pureMethod() {
		}
	}

	public static class ClassHavingPureMethod implements HasPureMethod {
		@Override
		public void pureMethod() {
		}
	}

	public static class ClassHavingUnpureMethod implements HasPureMethod {
		@Override
		public void pureMethod() {
			unpureMethod();
		}

		public void unpureMethod() {
		}
	}

	public interface HasPureMethod {
		@Pure
		void pureMethod();
	}

	public static class SuperClassWithInterface implements HasPureMethod {
		@Override
		public void pureMethod() {
		}
	}

	public static class ClassHavingIndirectPureMethod extends SuperClassWithInterface {
		@Override
		public void pureMethod() {
		}
	}

	public static class ClassHavingIndirectUnpureMethod extends SuperClassWithInterface {
		private ClassHavingUnpureMethod otherObj = new ClassHavingUnpureMethod();

		@Override
		public void pureMethod() {
			otherObj.unpureMethod();
		}
	}
}
