package nl.tno.idsa.framework.semantics_base.enumerations;

import nl.tno.idsa.framework.semantics_base.JavaSubclassFinder;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Runtime enumerations create the opportunity to create options (like normal enums) that are not fixed in code, but
 * rather gathered from implementing classes. For example, all days of the week implement DayOfWeek, which implements
 * the IRuntimeEnumElement interface. We can then list the various days of the week by calling
 * the listOptions method of RuntimeEnum.getInstance(DayOfWeek.class). Outside this class, this is all type safe.
 */
public class RuntimeEnum<T extends IRuntimeEnumElement> {

    private static Map<Class, RuntimeEnum> instances = new HashMap<>();

    /**
     * Get the runtime enumeration for the given base class, e.g. DayOfWeek.
     */
    @SuppressWarnings("unchecked")
    public static <T extends IRuntimeEnumElement> RuntimeEnum<T> getInstance(Class<? extends T> baseClass) {
        RuntimeEnum runtimeEnum = instances.get(baseClass);
        if (runtimeEnum == null) {
            runtimeEnum = new RuntimeEnum<T>(baseClass);
            instances.put(baseClass, runtimeEnum);
        }
        return runtimeEnum;
    }

    private final Map<Integer, T> implementations;

    @SuppressWarnings("unchecked")
    private RuntimeEnum(Class<? extends T> baseClass) {
        Set subclasses = JavaSubclassFinder.listSubclasses(baseClass); // Why can't this be generically typed? ...
        implementations = new HashMap<Integer, T>();
        for (Object subclass : subclasses) {
            try {
                T instance = ((Class<? extends T>) subclass).newInstance();
                implementations.put(instance.getIndex(), instance);
            } catch (Exception e) {
                // Yeah.
            }
        }
    }

    /**
     * Get the runtime enumeration implementations of the base class.
     */
    @SuppressWarnings("unchecked")
    public Collection<T> listOptions() {
        return implementations.values();
    }

    /**
     * Get the runtime enumeration implementation with the given index of the base class.
     */
    @SuppressWarnings("unchecked")
    public T getOption(int index) {
        return implementations.get(index);
    }
}
