package io.github.benas.easyproperties;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

class ReflectionUtils {

    public static List<Field> getAllFields(final Object object) {
        List<Field> allFields = new ArrayList<>();
        allFields.addAll(getDeclaredFields(object));
        allFields.addAll(getInheritedFields(object));
        return allFields;
    }

    private static List<Field> getDeclaredFields(final Object object) {
        return new ArrayList<>(Arrays.asList(object.getClass().getDeclaredFields()));
    }

    private static List<Field> getInheritedFields(final Object object) {
        List<Field> inheritedFields = new ArrayList<>();
        Class clazz = object.getClass();
        while (clazz.getSuperclass() != null) {
            Class superclass = clazz.getSuperclass();
            inheritedFields.addAll(Arrays.asList(superclass.getDeclaredFields()));
            clazz = superclass;
        }
        return inheritedFields;
    }
}
