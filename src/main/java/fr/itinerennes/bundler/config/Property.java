package fr.itinerennes.bundler.config;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotates a field to be injected with a value extracted from a propery file.
 * 
 * @see PropertyAnnotations
 * @author Jérémie Huchet
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Property {

    /**
     * @return the key of the value to inject
     */
    String value() default "";

    /**
     * @return an optional specific file from which the properties must be read
     */
    String file() default "";
}
