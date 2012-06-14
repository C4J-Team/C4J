package de.vksi.c4j;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

/**
 * Usable on contract-methods. Denotes the target method of a contract method as being @{@link Pure}, meaning the target
 * method does not have any side-effects.
 * <p>
 * Especially useful when using external contracts, if the target method cannot be modified.
 */
@Documented
@Target({ ElementType.METHOD })
public @interface PureTarget {

}
