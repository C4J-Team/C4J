package de.andrena.next;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

/**
 * Usable on a contract-method not overriding a target method. Code in this method is being executed within the
 * initialization of the contract instance.
 * <p>
 * Note that code within constructors is <em>not</em> being executed within the initialization of the contract instance.
 * Instead, pre- and post-conditions for constructors can be defined within constructors of a contract-class.
 */
@Documented
@Target(ElementType.METHOD)
public @interface InitializeContract {

}
