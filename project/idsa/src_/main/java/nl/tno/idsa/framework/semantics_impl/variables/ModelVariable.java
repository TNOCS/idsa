package nl.tno.idsa.framework.semantics_impl.variables;

import nl.tno.idsa.framework.behavior.models.Model;
import nl.tno.idsa.framework.semantics_base.JavaSubclassFinder;

import java.util.Set;

/**
 * Created by jongsd on 13-8-15.
 */

// TODO Code documentation
// TODO Is this a variable in the same vein as group, boolean, location, et cetera?

public class ModelVariable extends Variable<Class<? extends Model>> {

    public ModelVariable() {

    }

    public ModelVariable(Class<? extends Model> value) {
        setValue(value);
    }

    @Override
    public Set<Class<? extends Model>> getDomain() {
        return JavaSubclassFinder.listSubclasses(Model.class);
    }

    @Override
    public void setDomain(Set domain) {
        // Ignore.
    }

    @Override
    public Variable<Class<? extends Model>> getConstraintIntersection(Variable<Class<? extends Model>> otherVariable) {
        return null;
    }

    @Override
    protected boolean checkValidity(Class<? extends Model> value) {
        return true;
    }

    @Override
    public Variable<Class<? extends Model>> deepClone() {
        return new ModelVariable(getValue());
    }

    @Override
    public String toString(boolean mostSpecific) {
        return toString();
    }

    @Override
    public String toString() {
        return "M" + getIdString() + "[Value=" + getValue().getSimpleName() + "]";
    }
}
