package nl.tno.idsa.framework.semantics_impl.locations;

import nl.tno.idsa.framework.semantics_base.objects.ParameterId;
import nl.tno.idsa.framework.semantics_base.objects.SemanticObject;
import nl.tno.idsa.framework.semantics_impl.variables.IntegerVariable;
import nl.tno.idsa.framework.semantics_impl.variables.Variable;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Implementations of this class should be final to reflect that they do not use Java object hierarchy for parent-
 * child relations, but rather their own mechanism that supports multiple inheritance et cetera.
 */

// TODO Document class. Remark about finality above should apply to any semantic object implementation. Everything ...
// that is not final should be abstract, also. Also, for a school, we add function School as well as Workplace in the
// data model parser, so this should be explained as well.

public abstract class LocationFunction extends SemanticObject<LocationFunction> {

    public static enum Parameters implements ParameterId {
        CAPACITY, // In number of people present
        OCCUPANCY // In number of people present
    }

    HashSet<Class<? extends LocationFunction>> superclasses;

    public LocationFunction() {
        super(); // Creates and sets all parameters.
        Class<? extends LocationFunction>[] superclassArray = getSuperclassArray();
        if (superclassArray != null) {
            superclasses = new HashSet<>();
            Collections.addAll(superclasses, superclassArray);
        }
    }

    @SuppressWarnings("unchecked")
    public LocationFunction(int capacity) {
        this();  // Creates and sets all parameters.
        getParameters().get(Parameters.CAPACITY).setValue(capacity); // Override the capacity, which has already been set.
    }

    @Override // This happens in the constructor before anything else (in this class).
    protected void addOwnParameters(Map<ParameterId, Variable> parameterMap) {
        // super.addOwnParameters(parameterMap); // Is abstract.
        parameterMap.put(Parameters.CAPACITY, new IntegerVariable(Integer.MAX_VALUE));
        parameterMap.put(Parameters.OCCUPANCY, new IntegerVariable(0));
    }

    public Set<Class<? extends LocationFunction>> getSemanticSuperclasses() {
        return superclasses;
    }

    /**
     * Subclasses should override this if needed.
     */
    protected Class<? extends LocationFunction>[] getSuperclassArray() {
        return null;
    }

    public final int getCapacity() {
        return (int) getParameters().get(Parameters.CAPACITY).getValue();
    }

    public final int getOccupancy() {
        return (int) getParameters().get(Parameters.OCCUPANCY).getValue();
    }

    public final boolean canFit(int numAgents) {
        return getOccupancy() + numAgents <= getCapacity();
    }

    @SuppressWarnings("unchecked")
    public final void increaseOccupancy(int numAgents) {
        getParameters().get(Parameters.OCCUPANCY).setValue(getOccupancy() + numAgents);
    }

    @SuppressWarnings("unchecked")
    public final void decreaseOccupancy(int numAgents) {
        getParameters().get(Parameters.OCCUPANCY).setValue(getOccupancy() - numAgents);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName();
    }
}
