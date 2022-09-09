package me.zort.configurationlib.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation may be put on fields in mapping
 * classes where we want to set field value to the
 * id (name) of current node.
 * It's useful because normally, the id of current node
 * can't be retrieved from node itself.
 * <p>
 * This annotation can be placed only on fields whose
 * type is String!
 *
 * @author ZorTik
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface ThisNodeId {
}
