package de.vksi.c4j.systemtest.contractclassmagic;

import org.apache.log4j.Level;
import org.junit.Rule;
import org.junit.Test;

import de.vksi.c4j.ContractReference;
import de.vksi.c4j.Target;
import de.vksi.c4j.internal.classfile.ClassAnalyzer;
import de.vksi.c4j.systemtest.TransformerAwareRule;

public class TargetReferenceSystemTest {
	@Rule
	public TransformerAwareRule transformerAware = new TransformerAwareRule();
	private ClassAnalyzer reflectionHelper = new ClassAnalyzer();

	@Test
	public void testTargetHasWeakerType() {
		new TargetClassWithWeakerTarget();
		transformerAware.expectGlobalLog(Level.WARN, "Target reference "
				+ reflectionHelper.getSimplerName(ContractClassWithWeakerTarget.class)
				+ ".targetWeakerType has weaker type than the target type would allow.");
	}

	@Test
	public void testTargetHasIncompatibleType() {
		new TargetClassWithIncompatibleTarget();
		transformerAware.expectGlobalLog(Level.ERROR, "Target reference "
				+ reflectionHelper.getSimplerName(ContractClassWithIncompatibleTarget.class)
				+ ".targetIncompatibleType has incompatible type.");
	}

	@Test
	public void testTargetIsMissingAnnotation() {
		new TargetClassWithTargetMissingAnnotation();
		transformerAware.expectGlobalLog(Level.WARN, "Field "
				+ reflectionHelper.getSimplerName(ContractClassWithTargetMissingAnnotation.class)
				+ ".target is possibly missing annotation @Target.");
	}

	@Test
	public void testMultipleTargets() {
		new TargetClassWithMultipleTargets();
		transformerAware.expectGlobalLog(Level.ERROR, "Contract "
				+ reflectionHelper.getSimplerName(ContractClassWithMultipleTargets.class)
				+ " has multiple fields annotated with @Target, only the first one is being set.");
	}

	@ContractReference(ContractClassWithWeakerTarget.class)
	private static class TargetClassWithWeakerTarget {
	}

	private static class ContractClassWithWeakerTarget extends TargetClassWithWeakerTarget {
		@Target
		protected Object targetWeakerType;
	}

	@ContractReference(ContractClassWithIncompatibleTarget.class)
	private static class TargetClassWithIncompatibleTarget {
	}

	private static class ContractClassWithIncompatibleTarget extends TargetClassWithIncompatibleTarget {
		@Target
		protected ContractClassWithIncompatibleTarget targetIncompatibleType;
	}

	@ContractReference(ContractClassWithTargetMissingAnnotation.class)
	private static class TargetClassWithTargetMissingAnnotation {
	}

	private static class ContractClassWithTargetMissingAnnotation extends TargetClassWithTargetMissingAnnotation {
		protected TargetClassWithTargetMissingAnnotation target;
	}

	@ContractReference(ContractClassWithMultipleTargets.class)
	private static class TargetClassWithMultipleTargets {
	}

	private static class ContractClassWithMultipleTargets extends TargetClassWithMultipleTargets {
		@Target
		protected TargetClassWithMultipleTargets target1;
		@Target
		protected TargetClassWithMultipleTargets target2;
	}
}
