package nl.tno.idsa.framework.semantics_impl.relations;

import nl.tno.idsa.framework.semantics_base.objects.ParameterId;
import nl.tno.idsa.framework.semantics_base.relations.EnablingRelation;
import nl.tno.idsa.framework.semantics_impl.actions.Action;
import nl.tno.idsa.framework.semantics_impl.locations.LocationFunction;
import nl.tno.idsa.framework.semantics_impl.variables.Variable;

import java.util.Map;

/**
 * Created by jongsd on 7-8-15.
 * Class "in between" for clarity.
 */

// TODO Document class.

public abstract class LocationEnablesAction extends EnablingRelation<LocationFunction, Action> {

    protected LocationEnablesAction(Class<? extends LocationFunction> locationFunctionClass, Class<? extends Action> actionClass) {
        super(locationFunctionClass, actionClass);
    }

    @Override
    protected void addOwnParameters(Map<ParameterId, Variable> parameterMap) {
        // super.addOwnParameters(parameterMap); // Is abstract.
    }
}
