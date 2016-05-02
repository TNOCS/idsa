package nl.tno.idsa.framework.semantics_impl.variables;

/**
 * Created by jongsd on 13-8-15.
 */

// TODO Code documentation.

public class BooleanVariable extends Variable<Boolean> {

    public BooleanVariable() {

    }

    public BooleanVariable(boolean value) {
        setValue(value);
    }

    @Override
    public Variable<Boolean> getConstraintIntersection(Variable<Boolean> otherVariable) {
        return this; // TODO Not implemented.
    }

    @Override
    protected boolean checkValidity(Boolean value) {
        return true;
    }

    @Override
    public Variable<Boolean> deepClone() {
        return new BooleanVariable(getValue());
    }

    @Override
    public String toString() {
        if (getBinder() != null) {
            Variable<Boolean> mostSpecificVariable = getBinder().getMostSpecificVariable(this);
            if (mostSpecificVariable != this) {
                return mostSpecificVariable.toString(false);
            }
        }
        return toString(false);
    }

    @Override
    public String toString(boolean mostSpecific) {
        if (mostSpecific) {
            return toString();
        }
        return "B" + getIdString() + "[Value=" + getValue() + "]";
    }
}
