package nl.tno.idsa.framework.semantics_impl.variables;

/**
 * Created by jongsd on 13-8-15.
 */

// TODO Document code.

public class IntegerVariable extends Variable<Integer> {

    public IntegerVariable() {

    }

    public IntegerVariable(int value) {
        setValue(value);
    }

    @Override
    public Variable<Integer> getConstraintIntersection(Variable<Integer> otherVariable) {
        return this; // TODO The other variable may have a different domain -- take the smallest interval in both.
    }

    @Override
    protected boolean checkValidity(Integer value) {
        return true;
    }

    @Override
    public Variable<Integer> deepClone() {
        return new IntegerVariable(getValue());
    }

    @Override
    public String toString() {
        if (getBinder() != null) {
            Variable<Integer> mostSpecificVariable = getBinder().getMostSpecificVariable(this);
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
        return "I" + getIdString() + "[Value=" + getValue() + "]";
    }
}
