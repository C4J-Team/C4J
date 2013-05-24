package de.vksi.c4j.systemtest.pure;

import org.junit.Rule;
import org.junit.Test;

import de.vksi.c4j.Pure;
import de.vksi.c4j.systemtest.TransformerAwareRule;

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

	private static class PureSubClass extends SuperClass {
		@Override
		public void pureMethod() {
		}
	}

	@SuppressWarnings("unused")
	private static class UnpureSubClass extends SuperClass {
		protected String field;

		@Override
		public void pureMethod() {
			field = "invalid";
		}
	}

	@SuppressWarnings("unused")
	private static class SuperClass {
		@Pure
		public void pureMethod() {
		}
	}

	private static class ClassHavingPureMethod implements HasPureMethod {
		@Override
		public void pureMethod() {
		}
	}

	private static class ClassHavingUnpureMethod implements HasPureMethod {
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

	private static class SuperClassWithInterface implements HasPureMethod {
		@Override
		public void pureMethod() {
		}
	}

	private static class ClassHavingIndirectPureMethod extends SuperClassWithInterface {
		@Override
		public void pureMethod() {
		}
	}

	private static class ClassHavingIndirectUnpureMethod extends SuperClassWithInterface {
		private ClassHavingUnpureMethod otherObj = new ClassHavingUnpureMethod();

		@Override
		public void pureMethod() {
			otherObj.unpureMethod();
		}
	}
}
