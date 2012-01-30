package de.andrena.next.internal.transformer;

import javassist.CannotCompileException;
import javassist.CtClass;
import javassist.CtConstructor;
import javassist.CtField;
import javassist.CtNewConstructor;
import javassist.NotFoundException;
import de.andrena.next.internal.util.ContractRegistry.ContractInfo;

public class ConstructorTransformer extends AbstractContractClassTransformer {
	public static final String CONSTRUCTOR_REPLACEMENT_NAME = "constructor$";

	@Override
	public void transform(ContractInfo contractInfo, CtClass contractClass) throws Exception {
		if (contractClass.equals(contractInfo.getContractClass()) && !(contractInfo.getTargetClass().isInterface())) {
			replaceConstructors(contractClass);
			contractClass.addConstructor(CtNewConstructor.defaultConstructor(contractClass));
		}
	}

	private void replaceConstructors(CtClass contractClass) throws CannotCompileException, NotFoundException {
		for (CtConstructor constructor : contractClass.getConstructors()) {
			if (constructor.isClassInitializer()) {
				System.out.println("WARNING: REMOVING CLASS INITIALIZER!!!");
			}
			contractClass.addMethod(constructor.toMethod(CONSTRUCTOR_REPLACEMENT_NAME, contractClass));
		}
		if (contractClass.getSuperclass() != null) {
			CtClass oldSuperclass = contractClass.getSuperclass();
			contractClass.getClassFile().setSuperclass(null);
			for (CtField superclassField : oldSuperclass.getFields()) {
				contractClass.addField(new CtField(superclassField, contractClass));
			}
		}
		for (CtConstructor constructor : contractClass.getConstructors()) {
			contractClass.removeConstructor(constructor);
		}
	}

}
