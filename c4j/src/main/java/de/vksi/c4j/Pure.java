package de.vksi.c4j;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

/**
 * Denotes the annotated method as being pure, meaning the method does not have any side-effects and does not modify the
 * state of the own object instance, its fields, any parameters of the method or any static fields of any class.
 * <p>
 * Can only call other methods being annotated as @{@link Pure} for these, if they are references.
 * <p>
 * Exceptions can be declared for fields by using @{@link AllowPureAccess}.
 * <p>
 * Note that all methods of a contract-class are implicitly @{@link Pure}.
 */
@Documented
@Target({ ElementType.METHOD })
public @interface Pure {

}
