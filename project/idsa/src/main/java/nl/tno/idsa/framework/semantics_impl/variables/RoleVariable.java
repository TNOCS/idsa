package nl.tno.idsa.framework.semantics_impl.variables;

import nl.tno.idsa.framework.semantics_base.SemanticLibrary;
import nl.tno.idsa.framework.semantics_impl.roles.Role;

/**
 * Created by jongsd on 13-8-15.
 */

// TODO Document code.

public class RoleVariable extends Variable<Class<? extends Role>> {

    public RoleVariable() {
    }

    public RoleVariable(Class<? extends Role> value) {
        setValue(value);
    }

    @Override
    public Variable<Class<? extends Role>> getConstraintIntersection(Variable<Class<? extends Role>> otherVariable) {
        if (SemanticLibrary.getInstance().isSemanticSubclass(getValue(), otherVariable.getValue())) {
            return otherVariable;
        } else if (SemanticLibrary.getInstance().isSemanticSuperclass(getValue(), otherVariable.getValue())) {
            return this;
        } else {
            return null;
        }
    }

    @Override
    protected boolean checkValidity(Class<? extends Role> value) {
        return true;
    }

    @Override
    public Variable<Class<? extends Role>> deepClone() {
        return new RoleVariable(getValue());
    }

    @Override
    public String toString() {
        if (getBinder() != null) {
            Variable<Class<? extends Role>> mostSpecificVariable = getBinder().getMostSpecificVariable(this);
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
        String simpleName = getValue() != null ? getValue().getSimpleName() : "?";
        return "R" + getIdString() + "[" + simpleName + "]";
    }
}
