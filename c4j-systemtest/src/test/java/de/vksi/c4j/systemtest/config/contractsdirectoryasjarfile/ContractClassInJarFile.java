package de.vksi.c4j.systemtest.config.contractsdirectoryasjarfile;

import static de.vksi.c4j.Condition.preCondition;
import de.vksi.c4j.Contract;
import de.vksi.c4j.systemtest.config.contractsdirectoryasjarfile.ContractsDirectoryAsJarFileSystemTest.TargetClassUsingContractInJarFile;

@Contract
public class ContractClassInJarFile extends TargetClassUsingContractInJarFile {
	@Override
	public void method(int arg) {
		if (preCondition()) {
			assert arg > 0;
		}
	}
}
