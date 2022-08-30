package me.zort.configurationlib.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation is used for fields in mapped objects.
 * Determines the name of the node (section/value) in configuration
 * if field's name is different.
 *
 * @author ZorTik
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface NodeName {

    String value();

}
