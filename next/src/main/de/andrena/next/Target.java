package de.andrena.next;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;

/**
 * Usable within a contract-class, a field being annotated with @{link Target} will hold the instance of the
 * corresponding target instance at runtime.
 * <p>
 * The field being annotated should always have the type of the target class.
 */
@Documented
@java.lang.annotation.Target(ElementType.FIELD)
public @interface Target {

}
