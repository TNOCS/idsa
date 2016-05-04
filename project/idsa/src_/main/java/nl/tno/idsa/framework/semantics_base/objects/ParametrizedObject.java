package nl.tno.idsa.framework.semantics_base.objects;

import nl.tno.idsa.framework.semantics_impl.variables.Variable;

import java.util.Map;

/**
 * Base class for objects that have parameters (i.e. other than fields).
 */

// TODO Document class.

public abstract class ParametrizedObject {
    private Map<ParameterId, Variable> parameters;

    /**
     * Construct the object and the parameters.
     */
    protected ParametrizedObject() {
        parameters = createParameters();
    }

    /**
     * Return the parameters of this object.
     */
    public Map<ParameterId, Variable> getParameters() {
        return parameters;
    }

    /**
     * Create the parameters. It is possible to give them a default value in this method. This is the first thing
     * a parametrized object does when being constructed.
     */
    protected abstract Map<ParameterId, Variable> createParameters();
}
