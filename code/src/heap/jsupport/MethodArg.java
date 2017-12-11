package heap.jsupport;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * An annotation to mark fields standing for method arguments.
 * 
 * @author romanm
 */
@Retention(RUNTIME)
@Target(FIELD)
public @interface MethodArg {
	/**
	 * Indicates whether this argument is of interest when the method exits.
	 */
	boolean out();

	/**
	 * Indicates that the argument may not be written to.
	 */
	boolean readonly() default false;
}