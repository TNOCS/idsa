package nl.tno.idsa.framework.semantics_base.objects;

import nl.tno.idsa.framework.semantics_base.SemanticLibrary;
import nl.tno.idsa.framework.semantics_impl.variables.Variable;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Semantic base class. The class has parameters (inherited from the parametrized object class). Also, it has a
 * potentially many-to-many sub/superclass relation. It is not (always) needed to instantiate a semantic object.
 * The semantic library keeps one instantiation, and supports all kinds of queries on class level rather than
 * instance level. This facilitates semantic concepts that are basically static and unchangeable.
 */
public abstract class SemanticObject<T extends ParametrizedObject> extends ParametrizedObject {

    // TODO How to enforce that we cannot instantiate this class outside the semantic library?

    // TODO Rememeber: you cannot access the semantic library in the constructor of a semantic object ...
    // since the library calls this constructor before existing itself :)

    /**
     * Create the object and all parameters.
     */
    protected SemanticObject() {
        super();
    }

    /**
     * Return the semantic superclasses of this semantic object.
     */
    public abstract Set<Class<? extends T>> getSemanticSuperclasses();

    /**
     * Create the parameters for this semantic object. This includes (1) all superclass parameters and (2) own
     * parameters, in that order. This method is called by the constructor of ParametrizedObject.
     */
//    @SuppressWarnings("unchecked")
    protected final Map<ParameterId, Variable> createParameters() {
        Map<ParameterId, Variable> result = new HashMap<>();
        addOwnParameters(result);
        return result;
    }

    /**
     * Implementations inheriting from a non-abstract semantic class should always include a call
     * to super.addOwnParameters. Moreover, if the semantic object defines any parameters itself,
     * these parameters can be defined and added to the parameter map here. This happens at the
     * start of construction of the semantic object. Default values are possible.
     */
    protected abstract void addOwnParameters(Map<ParameterId, Variable> parameterMap);

    /**
     * Create a deep clone of this object, i.e. all parameters are newly created so we can independently set values.
     */
    public SemanticObject deepClone() {
        try {
            SemanticObject newInstance = SemanticLibrary.getInstance().createSemanticInstance(getClass());
            Map<ParameterId, Variable> result = null;
            if (getParameters() != null) {
                result = new HashMap<>();
                for (ParameterId key : getParameters().keySet()) {
                    Variable variable = getParameters().get(key);
                    if (variable != null) {
                        result.put(key, variable.deepClone());
                    } else {
                        result.put(key, null);
                    }
                }
            }
            newInstance.getParameters().clear();
            if (result != null) {
                newInstance.getParameters().putAll(result);
            }
            return newInstance;
        } catch (Exception e) {
            throw new IllegalArgumentException("Cannot clone objects of class " + getClass() + ". Perhaps the class is missing a default constructor?");
        }
    }
}
