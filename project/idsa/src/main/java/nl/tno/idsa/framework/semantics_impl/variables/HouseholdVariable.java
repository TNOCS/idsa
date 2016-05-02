package nl.tno.idsa.framework.semantics_impl.variables;

import nl.tno.idsa.framework.semantics_impl.groups.Group;

/**
 * Created by kleina on 21-8-2015.
 */

// TODO Code documentation.
// TODO Does this have to be coded like a variable? It seems it does not use most of the functionalities of that class.

public class HouseholdVariable extends Variable<Group> {


    public HouseholdVariable() {

    }

    public HouseholdVariable(Group value) {
        setValue(value);
    }

    @Override
    public Variable<Group> getConstraintIntersection(Variable<Group> otherVariable) {
        return this;      // TODO Implement or throw exception.
    }

    @Override
    protected boolean checkValidity(Group value) {
        return true;      // TODO Implement or throw exception.
    }

    @Override
    public Variable<Group> deepClone() {
        return null;     // TODO Implement or throw exception.
        /*
        HashSet set = new HashSet(getValue().size());
        for (Object value : set){
            set.add(value);
        }
        return new HouseholdVariable(set);
        */
    }

    @Override
    public String toString() {
        return "HouseholdVariable{" +
                "set=" + getValue() +
                '}';
    }

    @Override
    public String toString(boolean mostSpecific) {
        return toString();
    }
}
