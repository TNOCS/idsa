package nl.tno.idsa.framework.semantics_impl.variables;

import java.util.Set;

/**
 * Created by kleina on 21-8-2015.
 */

// TODO Code documentation.    Or remove?

public class SetVariable extends Variable<Set> {


    public SetVariable() {

    }

    public SetVariable(Set value) {
        setValue(value);
    }

    @SuppressWarnings("unchecked")
    public boolean add(Object o) {
        return getValue().add(o);
    }

    public boolean remove(Object o) {
        return getValue().remove(o);
    }

    @Override
    public Variable<Set> getConstraintIntersection(Variable<Set> otherVariable) {
        return this;  // TODO Implement if needed.
    }

    @Override
    protected boolean checkValidity(Set value) {
        return true;  // TODO Implement if needed.
    }

    @Override
    public Variable<Set> deepClone() {
        return null;  // TODO Implement if needed.
        /*
        HashSet set = new HashSet(getValue().size());
        for (Object value : set){
            set.add(value);
        }
        return new HashSetVariable(set);
        */
    }

    @Override
    public String toString() {
        return "HashSetVariable{" +
                "set=" + getValue() +
                '}';
    }

    @Override
    public String toString(boolean mostSpecific) {
        return toString();
    }
}
