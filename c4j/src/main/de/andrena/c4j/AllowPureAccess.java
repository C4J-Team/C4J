package de.andrena.c4j;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

/**
 * Allows pure write-access on fields from methods being marked as @{@link Pure}.
 * <p>
 * This should be used with great care and only in exceptional cases, for example for caching a value being returned by
 * a getter.
 */
@Documented
@Target(ElementType.FIELD)
public @interface AllowPureAccess {

}
