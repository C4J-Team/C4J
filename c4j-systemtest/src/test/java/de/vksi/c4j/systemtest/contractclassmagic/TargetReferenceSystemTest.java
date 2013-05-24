package de.vksi.c4j.systemtest.contractclassmagic;

import static de.vksi.c4j.internal.classfile.ClassAnalyzer.getSimplerName;

import org.apache.log4j.Level;
import org.junit.Rule;
import org.junit.Test;

import de.vksi.c4j.ContractReference;
import de.vksi.c4j.Target;
import de.vksi.c4j.systemtest.TransformerAwareRule;

public class TargetReferenceSystemTest {
	@Rule
	public TransformerAwareRule transformerAware = new TransformerAwareRule();

	@Test
	public void testTargetHasWeakerType() {
		new TargetClassWithWeakerTarget();
		transformerAware.expectGlobalLog(Level.WARN, "Target reference "
				+ getSimplerName(ContractClassWithWeakerTarget.class)
				+ ".targetWeakerType has weaker type than the target type would allow.");
	}

	@Test
	public void testTargetHasIncompatibleType() {
		new TargetClassWithIncompatibleTarget();
		transformerAware.expectGlobalLog(Level.ERROR, "Target reference "
				+ getSimplerName(ContractClassWithIncompatibleTarget.class)
				+ ".targetIncompatibleType has incompatible type.");
	}

	@Test
	public void testTargetIsMissingAnnotation() {
		new TargetClassWithTargetMissingAnnotation();
		transformerAware.expectGlobalLog(Level.WARN, "Field "
				+ getSimplerName(ContractClassWithTargetMissingAnnotation.class)
				+ ".target is possibly missing annotation @Target.");
	}

	@Test
	public void testMultipleTargets() {
		new TargetClassWithMultipleTargets();
		transformerAware.expectGlobalLog(Level.ERROR, "Contract "
				+ getSimplerName(ContractClassWithMultipleTargets.class)
				+ " has multiple fields annotated with @Target, only the first one is being set.");
	}

	@ContractReference(ContractClassWithWeakerTarget.class)
	private static class TargetClassWithWeakerTarget {
	}

	@SuppressWarnings("unused")
	private static class ContractClassWithWeakerTarget extends TargetClassWithWeakerTarget {
		@Target
		protected Object targetWeakerType;
	}

	@ContractReference(ContractClassWithIncompatibleTarget.class)
	private static class TargetClassWithIncompatibleTarget {
	}

	@SuppressWarnings("unused")
	private static class ContractClassWithIncompatibleTarget extends TargetClassWithIncompatibleTarget {
		@Target
		protected ContractClassWithIncompatibleTarget targetIncompatibleType;
	}

	@ContractReference(ContractClassWithTargetMissingAnnotation.class)
	private static class TargetClassWithTargetMissingAnnotation {
	}

	@SuppressWarnings("unused")
	private static class ContractClassWithTargetMissingAnnotation extends TargetClassWithTargetMissingAnnotation {
		protected TargetClassWithTargetMissingAnnotation target;
	}

	@ContractReference(ContractClassWithMultipleTargets.class)
	private static class TargetClassWithMultipleTargets {
	}

	@SuppressWarnings("unused")
	private static class ContractClassWithMultipleTargets extends TargetClassWithMultipleTargets {
		@Target
		protected TargetClassWithMultipleTargets target1;
		@Target
		protected TargetClassWithMultipleTargets target2;
	}
}
