package nl.tno.idsa.framework.semantics_impl.variables;


// TODO Code documentation.

public class ClassVariable<C> extends Variable<Class<? extends C>> {

    public ClassVariable() {
    }

    public ClassVariable(Class<? extends C> value) {
        setValue(value);
    }

    @Override
    public Variable<Class<? extends C>> getConstraintIntersection(Variable<Class<? extends C>> otherVariable) {
        throw new RuntimeException("Not implemented"); // TODO Implement if needed.
    }

    @Override
    protected boolean checkValidity(Class<? extends C> value) {
        return true;
    }

    @Override
    public Variable<Class<? extends C>> deepClone() {
        ClassVariable<C> newVariable = new ClassVariable<>();
        newVariable.setValue(getValue());
        return newVariable;
    }

    @Override
    public String toString() {
        if (getBinder() != null) {
            Variable<Class<? extends C>> mostSpecificVariable = getBinder().getMostSpecificVariable(this);
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
        String valueStr = null;
        if (getValue() != null) {
            valueStr = getValue().getSimpleName();
        }
        return "C" + getIdString() + "[Value=" + valueStr + "]";
    }
}
