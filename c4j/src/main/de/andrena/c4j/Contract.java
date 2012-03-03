package de.andrena.c4j;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

/**
 * Declares a contract for a class (and its subclasses) or interface (and its implementing classes). The class (or
 * interface) declaring this annotation is called the target-class of the contract, the class holding the contract is
 * called the contract-class.
 * <p>
 * As an alternative to this annotation, an external contract can be defined using
 * {@link Configuration#getExternalContracts()}.
 */
@Documented
@Target(ElementType.TYPE)
public @interface Contract {
	Class<?> value();
}
