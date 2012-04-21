package de.andrena.c4j;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

/**
 * Declares a contract for a class (and its subclasses) or interface (and its implementing classes), refering to the
 * class holding the contract. The class (or interface) declaring this annotation is called the target-class of the
 * contract, the class holding the contract is called the contract-class.
 * <p>
 * As alternatives to this annotation, the contract relationship can inversely be defined using the @{@link Contract}
 * annotation, or an external contract can be defined using {@link Configuration#getExternalContracts()}.
 */
@Documented
@Target(ElementType.TYPE)
public @interface ContractReference {
	Class<?> value();
}
