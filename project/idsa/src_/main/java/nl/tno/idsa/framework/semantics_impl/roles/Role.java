package nl.tno.idsa.framework.semantics_impl.roles;

import nl.tno.idsa.framework.agents.Agent;
import nl.tno.idsa.framework.semantics_base.objects.ParameterId;
import nl.tno.idsa.framework.semantics_base.objects.SemanticSingleton;
import nl.tno.idsa.framework.semantics_impl.variables.IntegerVariable;
import nl.tno.idsa.framework.semantics_impl.variables.Variable;

import java.awt.*;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Semantic object denoting roles of agents (e.g. in plans). This object is not intended to be instantiated; roles
 * are static, unchangeable concepts.
 */

// TODO How can/Should we enforce that no-one except the semantic library instantiates a singleton semantic object? ...
// (Or in fact any object, with singleton being a yes/no attribute of a semantic class.)
// This is relatively easy at runtime (throw some exception), but someone can still write code that calls the
// constructor directly.
// Probably a good solution would be passing the semantic library in the constructor (which is a singleton itself),
// and for singletons, asking the semantic library whether there already is an instance and throwing an exception.


public abstract class Role extends SemanticSingleton<Role> {

    private HashSet<Class<? extends Role>> superclasses;

    /**
     * Each role can define a typical color, e.g. to distinguish agents
     * that have this role on a graphical map.
     */
    public static enum Parameters implements ParameterId {
        COLOR
    }

    /**
     * Construct a role object. Because a role is a singleton, only the semantic library should do this.
     */
    public Role() {
        // Convenience for subclasses of role: they define semantic superclasses in an array instead of a set.
        Class<? extends Role>[] semanticSuperclassArray = createSemanticSuperclassArray();
        if (semanticSuperclassArray != null) {
            superclasses = new HashSet<>();
            Collections.addAll(superclasses, semanticSuperclassArray);
        }
    }

    /**
     * Return the semantic superclasses. (For example, a policeman is also a first responder.)
     */
    public final Set<Class<? extends Role>> getSemanticSuperclasses() {
        return superclasses;
    }

    /**
     * Convenience method for implementations of the role class to deliver superclasses. Return an empty array or even
     * null if there are no superclasses.
     */
    protected abstract Class<? extends Role>[] createSemanticSuperclassArray();

    /**
     * If this role can be identified by a typical color, specify it here. This color is inherited unless semantic
     * subclasses override it.
     */
    protected Color getTypicalColor() {
        return null;
    }

    /**
     * Returns whether the agent is suitable for the role.
     */
    public boolean isAgentSuitable(Agent agent) {
        // TODO Implement (in subclasses object) that some agents are just not suitable for some roles, e.g. a 3-year old shoplifter.
        return true;
    }

    /**
     * Add parameters, in this case the typical color parameter.
     */
    @Override
    protected final void addOwnParameters(Map<ParameterId, Variable> parameterMap) {
        // super.addOwnParameters(parameterMap); // Is abstract.
        if (getTypicalColor() != null) {
            parameterMap.put(Parameters.COLOR, new IntegerVariable(getTypicalColor().getRGB()));
        }
    }
}
