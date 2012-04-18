package javassist.expr;

import javassist.CannotCompileException;
import javassist.CtClass;
import javassist.CtPrimitiveType;
import javassist.NotFoundException;
import javassist.bytecode.BadBytecode;
import javassist.bytecode.Bytecode;
import javassist.bytecode.CodeAttribute;
import javassist.bytecode.CodeIterator;
import javassist.bytecode.ConstPool;
import javassist.bytecode.LocalVariableAttribute;
import javassist.bytecode.MethodInfo;
import javassist.bytecode.Opcode;
import javassist.compiler.CompileError;
import javassist.compiler.Javac;
import javassist.compiler.JvstCodeGen;
import javassist.compiler.JvstTypeChecker;
import javassist.compiler.ProceedHandler;
import javassist.compiler.ast.ASTList;
import de.andrena.c4j.internal.RootTransformer;

public class ArrayWriteAccess extends Expr {

	public ArrayWriteAccess(int pos, CodeIterator i, CtClass declaring, MethodInfo m) {
		super(pos, i, declaring, m);
	}

	/**
	 * More or less copied from FieldAccess.
	 */
	@Override
	public void replace(String statement) throws CannotCompileException {
		thisClass.getClassFile(); // to call checkModify().
		ConstPool constPool = getConstPool();
		int pos = currentPos;

		Javac jc = new Javac(thisClass);
		CodeAttribute ca = iterator.get();
		try {
			CtClass[] params = new CtClass[2];
			params[0] = CtClass.intType;
			CtClass objectType = RootTransformer.INSTANCE.getPool().get(Object.class.getName());
			params[1] = objectType;

			int paramVar = ca.getMaxLocals();
			jc.recordParams(Object.class.getName(), params, true, paramVar, withinStatic());
			CtClass retType = CtClass.voidType;
			boolean included = checkResultValue(retType, statement);
			int retVar = jc.recordReturnType(retType, included);

			jc.recordProceed(new ProceedForArrayWrite(objectType, paramVar));
			Bytecode bytecode = jc.getBytecode();
			storeStack(params, false, paramVar, bytecode);
			jc.recordLocalVariables(ca, pos);
			LocalVariableAttribute va = (LocalVariableAttribute)
					ca.getAttribute(LocalVariableAttribute.tag);

			jc.compileStmnt(statement);

			replace0(pos, bytecode, 1); // 1 = opcode size
		} catch (CompileError e) {
			throw new CannotCompileException(e);
		} catch (NotFoundException e) {
			throw new CannotCompileException(e);
		} catch (BadBytecode e) {
			throw new CannotCompileException("broken method");
		}
	}

	static class ProceedForArrayWrite implements ProceedHandler {
		CtClass fieldType;
		int targetVar;

		ProceedForArrayWrite(CtClass type, int var) {
			fieldType = type;
			targetVar = var;
		}

		@Override
		public void doit(JvstCodeGen gen, Bytecode bytecode, ASTList args)
				throws CompileError {
			if (gen.getMethodArgsLength(args) != 2)
				throw new CompileError(Javac.proceedName
						+ "() can only take exactly 2 arguments for array writing");

			int stack;
			stack = -1;
			bytecode.addAload(targetVar);

			gen.atMethodArgs(args, new int[2], new int[2], new String[2]);
			stack -= ((CtPrimitiveType) CtClass.intType).getDataSize();
			--stack;

			bytecode.add(Opcode.AASTORE);
			bytecode.growStack(stack);
			gen.setType(CtClass.voidType);
			gen.addNullIfVoid();
		}

		@Override
		public void setReturnType(JvstTypeChecker c, ASTList args)
				throws CompileError {
			c.atMethodArgs(args, new int[2], new int[2], new String[2]);
			c.setType(CtClass.voidType);
			c.addNullIfVoid();
		}
	}

}
