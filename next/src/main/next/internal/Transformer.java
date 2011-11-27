package next.internal;

import java.lang.instrument.ClassFileTransformer;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.List;

import javassist.ByteArrayClassPath;
import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.LoaderClassPath;
import javassist.NotFoundException;
import next.Contract;
import next.internal.compiler.ArrayExp;
import next.internal.compiler.NestedExp;
import next.internal.compiler.StaticCallExp;
import next.internal.compiler.ValueExp;
import next.internal.util.BackdoorAnnotationLoader;
import next.internal.util.ObjectConverter;

import org.apache.log4j.Logger;

public class Transformer implements ClassFileTransformer {

	private Logger logger = Logger.getLogger(Transformer.class);
	private ClassPool pool = ClassPool.getDefault();

	@Override
	public byte[] transform(ClassLoader loader, String classNameWithSlashes, Class<?> classBeingRedefined,
			ProtectionDomain protectionDomain, byte[] classfileBuffer) {
		String className = classNameWithSlashes.replace('/', '.');
		logger.debug("transformation started for class " + className);
		try {
			if (loader != null) {
				pool.insertClassPath(new LoaderClassPath(loader));
			}
			if (classfileBuffer != null) {
				pool.insertClassPath(new ByteArrayClassPath(className, classfileBuffer));
			}

			CtClass targetClass = pool.get(className);
			if (targetClass.isInterface()) {
				logger.debug("transformation aborted, as class is an interface");
				return null;
			}
			if (targetClass.hasAnnotation(Contract.class)) {
				logger.info("transforming class " + className);
				String contractClassString = new BackdoorAnnotationLoader(targetClass).getClassValue(Contract.class,
						"value");
				transformClass(targetClass, pool.get(contractClassString));
				return targetClass.toBytecode();
			} else if (targetClass.getSuperclass() != null && targetClass.getSuperclass().hasAnnotation(Contract.class)) {
				logger.info("transforming contract " + className);
				transformContractClass(targetClass);
				targetClass.writeFile();
				return targetClass.toBytecode();
			}
		} catch (NotFoundException e) {
			logger.warn("transformation failed for class '" + className + "': could not load class", e);
		} catch (TransformationException e) {
			logger.warn(e.getMessage(), e);
		} catch (Throwable e) {
			e.printStackTrace();
			logger.warn("transformation failed for class '" + className + "': unknown error", e);
		}
		return null;
	}

	private void transformContractClass(CtClass contractClass) throws CannotCompileException {
		for (CtMethod contractMethod : contractClass.getDeclaredMethods()) {
			contractMethod.instrument(new ContractMethodExpressionEditor());
		}

	}

	private void transformClass(CtClass targetClass, CtClass contractClass) throws TransformationException,
			CannotCompileException, NotFoundException {
		for (CtMethod contractMethod : contractClass.getDeclaredMethods()) {
			for (CtMethod targetMethod : targetClass.getDeclaredMethods()) {
				if (contractMethod.equals(targetMethod)) {
					List<NestedExp> paramTypes = new ArrayList<NestedExp>();
					List<NestedExp> args = new ArrayList<NestedExp>();
					int i = 0;
					for (CtClass paramClass : contractMethod.getParameterTypes()) {
						paramTypes.add(new ValueExp(paramClass));
						args.add(new StaticCallExp(ObjectConverter.toObject, NestedExp.arg(i + 1)));
						i++;
					}
					ArrayExp paramTypesArray = new ArrayExp(Class.class, paramTypes);
					ArrayExp argsArray = new ArrayExp(Object.class, args);
					StaticCallExp callBefore = new StaticCallExp(Evaluator.before, new ValueExp(contractClass),
							new ValueExp(contractMethod.getName()), paramTypesArray, argsArray);
					NestedExp returnValue = new StaticCallExp(ObjectConverter.toObject, NestedExp.RETURN_VALUE);
					if (contractMethod.getReturnType().equals(CtClass.voidType)) {
						returnValue = NestedExp.NULL;
					}
					StaticCallExp callAfter = new StaticCallExp(Evaluator.after, new ValueExp(contractClass),
							new ValueExp(contractMethod.getName()), paramTypesArray, argsArray, returnValue);
					logger.info("callstring: " + callBefore);
					callBefore.toStandalone().insertBefore(targetMethod);
					callAfter.toStandalone().insertAfter(targetMethod);
				}
			}
		}
	}
}
