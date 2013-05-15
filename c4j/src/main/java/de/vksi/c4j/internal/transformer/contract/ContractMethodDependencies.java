package de.vksi.c4j.internal.transformer.contract;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javassist.CtMethod;
import de.vksi.c4j.error.UsageError;
import de.vksi.c4j.internal.compiler.EmptyExp;
import de.vksi.c4j.internal.compiler.StandaloneExp;
import de.vksi.c4j.internal.transformer.editor.InitializationGatheringEditor;
import de.vksi.c4j.internal.transformer.editor.StoreDependency;

public class ContractMethodDependencies {

	private List<StoreDependency> storeDependencies = new ArrayList<StoreDependency>();
	private boolean containsUnchanged = false;
	private StandaloneExp preConditionExp = new EmptyExp();
	private UsageError thrownException;
	private Set<CtMethod> mergedMethods = new HashSet<CtMethod>();

	public ContractMethodDependencies(Map<CtMethod, InitializationGatheringEditor> gatherMap, CtMethod contractMethod) {
		mergeMethodCalls(gatherMap, contractMethod);
	}

	private void mergeMethodCalls(Map<CtMethod, InitializationGatheringEditor> gatherMap, CtMethod contractMethod) {
		InitializationGatheringEditor initializationEditor = gatherMap.get(contractMethod);
		for (Map.Entry<CtMethod, InitializationGatheringEditor> dependency : getRelevantDependentEditors(
				contractMethod, initializationEditor, gatherMap).entrySet()) {
			mergeMethod(gatherMap, dependency.getKey(), dependency.getValue());
		}
	}

	private void mergeMethod(Map<CtMethod, InitializationGatheringEditor> gatherMap, CtMethod method,
			InitializationGatheringEditor initializationEditor) {
		if (mergedMethods.contains(method)) {
			return;
		}
		mergedMethods.add(method);
		mergeInitializationEditor(initializationEditor);
		mergeMethodCalls(gatherMap, method);
	}

	private void mergeInitializationEditor(InitializationGatheringEditor initializationEditor) {
		if (initializationEditor.containsUnchanged()) {
			containsUnchanged = true;
		}
		if (initializationEditor.getThrownException() != null) {
			thrownException = initializationEditor.getThrownException();
		}
		storeDependencies.addAll(initializationEditor.getStoreDependencies());
		preConditionExp = preConditionExp.append(initializationEditor.getPreConditionExp());
	}

	private Map<CtMethod, InitializationGatheringEditor> getRelevantDependentEditors(CtMethod rootMethod,
			InitializationGatheringEditor rootEditor, Map<CtMethod, InitializationGatheringEditor> gatherMap) {
		Map<CtMethod, InitializationGatheringEditor> relevantEditors = new HashMap<CtMethod, InitializationGatheringEditor>();
		relevantEditors.put(rootMethod, rootEditor);
		for (CtMethod dependencyMethod : rootEditor.getContractMethodCalls()) {
			relevantEditors.put(dependencyMethod, gatherMap.get(dependencyMethod));
		}
		return relevantEditors;
	}

	public boolean hasPreDependencies() {
		return hasStoreDependencies() || !preConditionExp.isEmpty();
	}

	public UsageError getThrownException() {
		return thrownException;
	}

	public StandaloneExp getPreConditionExp() {
		return preConditionExp;
	}

	public boolean hasStoreDependencies() {
		return !storeDependencies.isEmpty();
	}

	public List<StoreDependency> getStoreDependencies() {
		return storeDependencies;
	}

	public boolean containsUnchanged() {
		return containsUnchanged;
	}

}
