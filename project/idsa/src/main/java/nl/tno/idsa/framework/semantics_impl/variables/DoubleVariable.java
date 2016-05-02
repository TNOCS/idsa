package nl.tno.idsa.framework.semantics_impl.variables;

/**
 * Created by jongsd on 13-8-15.
 */

// TODO Code documentation.

public class DoubleVariable extends Variable<Double> {

    public DoubleVariable() {

    }

    public DoubleVariable(double value) {
        setValue(value);
    }

    @Override
    public Variable<Double> getConstraintIntersection(Variable<Double> otherVariable) {
        return this; // TODO The other variable may have a different domain -- take the smallest interval in both.
    }

    @Override
    protected boolean checkValidity(Double value) {
        return true;
    }

    @Override
    public Variable<Double> deepClone() {
        return new DoubleVariable(getValue());
    }

    @Override
    public String toString() {
        if (getBinder() != null) {
            Variable<Double> mostSpecificVariable = getBinder().getMostSpecificVariable(this);
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
        return "D" + getIdString() + "[Value=" + getValue() + "]";
    }
}
