package nl.tno.idsa.library.locations;

import nl.tno.idsa.framework.semantics_base.objects.ParameterId;
import nl.tno.idsa.framework.semantics_impl.locations.LocationFunction;
import nl.tno.idsa.framework.semantics_impl.variables.IntegerVariable;
import nl.tno.idsa.framework.semantics_impl.variables.Variable;

import java.util.Map;

public class SportsField extends LocationFunction {

    @Override
    @SuppressWarnings("unchecked")
    protected Class<? extends LocationFunction>[] getSuperclassArray() {
        return new Class[]{Public.class, Outside.class};
    }

    public SportsField() {
    }

    public SportsField(int capacity) {
        super(capacity);
    }

    @Override
    protected void addOwnParameters(Map<ParameterId, Variable> parameterMap) {
        super.addOwnParameters(parameterMap);
        parameterMap.put(LocationFunction.Parameters.CAPACITY, new IntegerVariable(Integer.MAX_VALUE)); // TODO Soccer team only apparently.
    }
}
