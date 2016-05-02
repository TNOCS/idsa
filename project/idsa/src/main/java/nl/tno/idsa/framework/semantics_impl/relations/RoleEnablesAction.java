package nl.tno.idsa.framework.semantics_impl.relations;

import nl.tno.idsa.framework.semantics_base.objects.ParameterId;
import nl.tno.idsa.framework.semantics_base.relations.EnablingRelation;
import nl.tno.idsa.framework.semantics_impl.actions.Action;
import nl.tno.idsa.framework.semantics_impl.roles.Role;
import nl.tno.idsa.framework.semantics_impl.variables.BooleanVariable;
import nl.tno.idsa.framework.semantics_impl.variables.Variable;

import java.util.Map;

/**
 * Created by jongsd on 7-8-15.
 * Class "in between" for clarity.
 */

// TODO Document class.

public abstract class RoleEnablesAction extends EnablingRelation<Role, Action> {

    public static enum Parameters implements ParameterId {ROLE_REQUIRED_FROM_ACTOR}

    @SuppressWarnings("unchecked")
    protected RoleEnablesAction(Class<? extends Role> roleClass, Class<? extends Action> actionClass, boolean isRoleRequiredFromActor) {
        super(roleClass, actionClass);
        getParameters().get(Parameters.ROLE_REQUIRED_FROM_ACTOR).setValue(isRoleRequiredFromActor);
    }

    public boolean isRoleRequiredFromActor() {
        return (boolean) getParameters().get(Parameters.ROLE_REQUIRED_FROM_ACTOR).getValue();
    }

    @Override
    protected void addOwnParameters(Map<ParameterId, Variable> parameterMap) {
        // super.addOwnParameters(parameterMap); // Is abstract.
        parameterMap.put(Parameters.ROLE_REQUIRED_FROM_ACTOR, new BooleanVariable(true));
    }
}
