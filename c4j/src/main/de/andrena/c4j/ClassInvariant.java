package de.andrena.c4j;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

/**
 * Usable on a contract-method not overriding a target method. Code in this method defines a class-invariant and is
 * being executed after the execution of any method in the target class.
 * <p>
 * Note that by default, a class-invariant is <em>not</em> being executed after pure methods. This behavior can be
 * configured using {@link Configuration#getPureBehaviors()}.
 */
@Documented
@Target(ElementType.METHOD)
public @interface ClassInvariant {

}
