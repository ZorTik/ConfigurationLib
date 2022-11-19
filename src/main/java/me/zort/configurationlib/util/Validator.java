package me.zort.configurationlib.util;

import java.util.ArrayList;
import java.util.List;

public final class Validator {

    public static void requireAnyType(Class<?> toBeChecked, Class<?>... types) {
        List<String> checkedTypes = new ArrayList<>();
        boolean passedCheck = false;
        for (Class<?> type : types) {
            checkedTypes.add(type.getName());
            if (type.isAssignableFrom(toBeChecked)) {
                passedCheck = true;
                break;
            }
        }
        if(!passedCheck) {
            throw new IllegalArgumentException(String.format("The given type is not assignable from any of the given types. (%s)", String.join(", ", checkedTypes)));
        }
    }

}
