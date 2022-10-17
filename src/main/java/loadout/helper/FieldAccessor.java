package loadout.helper;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.security.AccessController;
import java.security.PrivilegedAction;

public class FieldAccessor {
    public static <T> void setPrivateField(Class<T> clazz,String fieldName, Object newValue) throws NoSuchFieldException {
        Field f = clazz.getDeclaredField(fieldName);
        f.setAccessible(true);
        Field modifiersField = Field.class.getDeclaredField("modifiers");
        f.getModifiers();
    }
    public static <T> void setFinalStatic(Field field, Object newValue) throws Exception {
        field.setAccessible(true);
        Field modifiersField = Field.class.getDeclaredField("modifiers");

        // wrapping setAccessible
        AccessController.doPrivileged(new PrivilegedAction() {
            @Override
            public Object run() {
                modifiersField.setAccessible(true);
                return null;
            }
        });

        modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);
        field.set(null, newValue);
    }
}
