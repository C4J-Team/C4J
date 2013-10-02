package de.vksi.c4j.internal.transformer.contract;

import static de.vksi.c4j.internal.transformer.util.TransformationHelper.setClassIndex;
import static de.vksi.c4j.internal.transformer.util.TransformationHelper.setMethodIndex;

import java.util.List;

import javassist.CtClass;
import javassist.bytecode.BadBytecode;
import javassist.bytecode.CodeAttribute;
import javassist.bytecode.CodeIterator;
import javassist.bytecode.ConstPool;
import javassist.bytecode.Opcode;
import de.vksi.c4j.internal.compiler.StaticCall;
import de.vksi.c4j.internal.runtime.OldCache;
import de.vksi.c4j.internal.runtime.PureEvaluator;
import de.vksi.c4j.internal.transformer.editor.StoreDependency;

public class OldStoreCallWriter {

	private static final int GOTO_LENGTH = 3;
	private byte[] oldStoreBytes;
	private byte[] oldStoreExceptionBytes;
	private byte[] contractClassBytes;
	private byte[] registerUnchangeableBytes;

	public OldStoreCallWriter(ConstPool constPool, CtClass contractClass) {
		oldStoreBytes = getOldStoreBytes(constPool, OldCache.oldStore);
		oldStoreExceptionBytes = getOldStoreBytes(constPool, OldCache.oldStoreException);
		contractClassBytes = getContractClassBytes(constPool, contractClass);
		registerUnchangeableBytes = getRegisterUnchangeableBytes(constPool);
	}

	public int insertOldStoreCalls(CodeAttribute attribute, List<StoreDependency> storeDependencies) throws BadBytecode {
		CodeIterator iterator = attribute.iterator();
		int ifBlockLength = 0;
		for (StoreDependency storeDependency : storeDependencies) {
			ifBlockLength += insertStoreDependency(attribute, iterator, storeDependency);
		}
		return ifBlockLength;
	}

	private int insertStoreDependency(CodeAttribute attribute, CodeIterator iterator, StoreDependency storeDependency)
			throws BadBytecode {
		int startIndex = iterator.insert(storeDependency.getDependency());
		if (storeDependency.isUnchangeable()) {
			iterator.insert(registerUnchangeableBytes);
		}
		byte[] iloadBytes = getIloadBytes(storeDependency.getIndex());
		insertParamsAndMethodCall(iterator, iloadBytes, oldStoreBytes);
		int gotoIndex = insertStoreExceptionWithGoto(iterator, iloadBytes);
		int tryLength = gotoIndex - startIndex;
		int endIndex = startIndex + tryLength;
		attribute.getExceptionTable().add(startIndex, endIndex, endIndex + GOTO_LENGTH, 0);
		return tryLength + GOTO_LENGTH + contractClassBytes.length + iloadBytes.length + oldStoreExceptionBytes.length;
	}

	private int insertStoreExceptionWithGoto(CodeIterator iterator, byte[] iloadBytes) throws BadBytecode {
		byte[] gotoBytes = new byte[GOTO_LENGTH];
		gotoBytes[0] = (byte) Opcode.GOTO;
		int gotoTarget = gotoBytes.length + contractClassBytes.length + iloadBytes.length + oldStoreBytes.length;
		int gotoIndex = iterator.insert(gotoBytes);
		insertParamsAndMethodCall(iterator, iloadBytes, oldStoreExceptionBytes);
		// Goto target address must be adjusted afterwards, as insertions above manipulate this address
		iterator.write(new byte[] { (byte) (gotoTarget >> 8), (byte) gotoTarget }, gotoIndex + 1);
		return gotoIndex;
	}

	private void insertParamsAndMethodCall(CodeIterator iterator, byte[] iloadBytes, byte[] methodCallBytes)
			throws BadBytecode {
		iterator.insert(contractClassBytes);
		iterator.insert(iloadBytes);
		iterator.insert(methodCallBytes);
	}

	private byte[] getContractClassBytes(ConstPool constPool, CtClass contractClass) {
		byte[] contractClassBytes = new byte[3];
		contractClassBytes[0] = (byte) Opcode.LDC_W;
		setClassIndex(constPool, contractClassBytes, 1, contractClass);
		return contractClassBytes;
	}

	private byte[] getOldStoreBytes(ConstPool constPool, StaticCall oldStoreCall) {
		byte[] oldStoreBytes = new byte[3];
		oldStoreBytes[0] = (byte) Opcode.INVOKESTATIC;
		setMethodIndex(constPool, oldStoreBytes, 1, oldStoreCall, OldCache.oldStoreDescriptor);
		return oldStoreBytes;
	}

	private byte[] getIloadBytes(int i) {
		byte[] iloadBytes = new byte[2];
		iloadBytes[0] = Opcode.BIPUSH;
		iloadBytes[1] = (byte) i;
		return iloadBytes;
	}

	private byte[] getRegisterUnchangeableBytes(ConstPool constPool) {
		byte[] registerUnchangeableBytes = new byte[4];
		registerUnchangeableBytes[0] = (byte) Opcode.DUP;
		registerUnchangeableBytes[1] = (byte) Opcode.INVOKESTATIC;
		setMethodIndex(constPool, registerUnchangeableBytes, 2, PureEvaluator.registerUnchangeable,
				PureEvaluator.registerUnchangeableDescriptor);
		return registerUnchangeableBytes;
	}
}
