package nl.tno.idsa.framework.semantics_base;

import org.reflections.Reflections;

import java.util.Set;

/**
 * Created by jongsd on 4-8-15.
 */

// TODO Document class.
// TODO Move to utils?

public class JavaSubclassFinder {

    private static final Reflections reflections = new Reflections("");

    public static <S> Set<Class<? extends S>> listSubclasses(Class<S> baseClass) {
        return reflections.getSubTypesOf(baseClass);
    }
}
