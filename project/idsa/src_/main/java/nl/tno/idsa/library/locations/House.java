package nl.tno.idsa.library.locations;

import nl.tno.idsa.framework.semantics_base.objects.ParameterId;
import nl.tno.idsa.framework.semantics_impl.groups.Group;
import nl.tno.idsa.framework.semantics_impl.locations.LocationFunction;
import nl.tno.idsa.framework.semantics_impl.variables.HouseholdVariable;
import nl.tno.idsa.framework.semantics_impl.variables.Variable;

import java.util.Map;

public final class House extends LocationFunction {

    @Override
    @SuppressWarnings("unchecked")
    protected Class<? extends LocationFunction>[] getSuperclassArray() {
        return new Class[]{Inside.class, Private.class};
    }

    public static enum Parameters implements ParameterId {RESIDENTS}

    public House() {
    }

    @SuppressWarnings("unchecked")
    public House(int capacity) {
        super(capacity);
    }

    @Override
    protected void addOwnParameters(Map<ParameterId, Variable> parameterMap) {
        super.addOwnParameters(parameterMap);
        parameterMap.put(Parameters.RESIDENTS, new HouseholdVariable());
    }

    // Convenience getter
    public Group getHousehold() {
        Group result = null;
        Variable hhv = getParameters().get(Parameters.RESIDENTS);
        if (hhv != null && hhv.getValue() != null) {
            result = (Group) hhv.getValue();
        }
        return result;
    }

    // Convenience setter
    @SuppressWarnings("unchecked")
    public void setHousehold(Group household) {
        Variable hhv = getParameters().get(Parameters.RESIDENTS);
        if (hhv != null) {
            hhv.setValue(household); // Unchecked.
        } else {
            getParameters().put(Parameters.RESIDENTS, new HouseholdVariable(household));
        }
    }
}
