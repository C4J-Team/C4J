package de.andrena.next.internal.transformer;

import javassist.CtClass;
import javassist.CtConstructor;
import javassist.CtNewConstructor;
import de.andrena.next.internal.util.ContractRegistry.ContractInfo;

public class ConstructorTransformer extends AbstractContractClassTransformer {
	public static final String CONSTRUCTOR_REPLACEMENT_NAME = "SOIFDJSDOIFSDFH$";

	@Override
	public void transform(ContractInfo contractInfo, CtClass contractClass) throws Exception {
		if (contractClass.equals(contractInfo.getContractClass()) && !(contractInfo.getTargetClass().isInterface())) {
			for (CtConstructor constructor : contractClass.getConstructors()) {
				if (constructor.isClassInitializer()) {
					System.out.println("WARNING: REMOVING CLASS INITIALIZER!!!");
				}
				contractClass.addMethod(constructor.toMethod(CONSTRUCTOR_REPLACEMENT_NAME, contractClass));
			}
			contractClass.getClassFile().setSuperclass(null);
			for (CtConstructor constructor : contractClass.getConstructors()) {
				contractClass.removeConstructor(constructor);
			}
			contractClass.addConstructor(CtNewConstructor.defaultConstructor(contractClass));
		}
	}

}
