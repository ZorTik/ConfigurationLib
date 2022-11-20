package me.zort.configurationlib.annotation;

import org.jetbrains.annotations.Nullable;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;

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

    final class Parser {

        @Nullable
        public static String parse(Object from) {
            for (Field field : from.getClass().getDeclaredFields()) {
                if(field.isAnnotationPresent(ThisNodeId.class)) {
                    field.setAccessible(true);
                    try {
                        return (String) field.get(from);
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                }
            }
            return null;
        }

    }

}
