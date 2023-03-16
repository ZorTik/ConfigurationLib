package me.zort.configurationlib.util;

import com.google.common.base.Defaults;
import lombok.experimental.UtilityClass;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

@UtilityClass
public class ReflectionHelper {

    @SuppressWarnings("unchecked")
    public static <T> T newInstance(Class<T> clazz) throws InvocationTargetException, InstantiationException, IllegalAccessException, NoSuchMethodException {
        Constructor<T> constructor;
        try {
            constructor = clazz.getDeclaredConstructor();
        } catch (NoSuchMethodException e) {
            Constructor<?>[] constructors = clazz.getDeclaredConstructors();

            if (constructors.length == 0)
                throw new NoSuchMethodException("No constructors found for class " + clazz.getName());

            constructor = (Constructor<T>) constructors[0];
        }

        constructor.setAccessible(true);
        Object[] params = new Object[0];
        if (constructor.getParameterCount() > 0) {
            params = new Object[constructor.getParameterCount()];
            for (int i = 0; i < constructor.getParameterCount(); i++) {
                Class<?> parameterType = constructor.getParameterTypes()[i];
                params[i] = Defaults.defaultValue(parameterType);
            }
        }
        return constructor.newInstance(params);
    }

}
