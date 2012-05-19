package de.andrena.c4j.systemtest.config.contractsdirectoryasjarfile;

import static de.andrena.c4j.Condition.pre;
import de.andrena.c4j.Contract;
import de.andrena.c4j.systemtest.config.contractsdirectoryasjarfile.ContractsDirectoryAsJarFileSystemTest.TargetClassUsingContractInJarFile;

@Contract
public class ContractClassInJarFile extends TargetClassUsingContractInJarFile {
	@Override
	public void method(int arg) {
		if (pre()) {
			assert arg > 0;
		}
	}
}
