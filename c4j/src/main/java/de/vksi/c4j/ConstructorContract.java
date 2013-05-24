package de.vksi.c4j;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

/**
 * Usable on a contract-method not overriding a target method. The signature of the contract method has to match the
 * signature of a constructor of the target class. The contract method is executed whenever the matching target
 * constructor is being called.
 */
@Documented
@Target(ElementType.METHOD)
public @interface ConstructorContract {

}
