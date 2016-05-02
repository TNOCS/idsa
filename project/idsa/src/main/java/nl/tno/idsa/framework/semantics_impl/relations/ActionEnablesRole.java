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
 */

// TODO Document class.

public abstract class ActionEnablesRole extends EnablingRelation<Action, Role> {

    public static enum Parameters implements ParameterId {ACTION_ENABLES_ACTOR_ROLE}

    @SuppressWarnings("unchecked")
    protected ActionEnablesRole(Class<? extends Action> actionClass, Class<? extends Role> roleClass, boolean doesActionGiveActorTheRole) {
        super(actionClass, roleClass);
        getParameters().get(Parameters.ACTION_ENABLES_ACTOR_ROLE).setValue(doesActionGiveActorTheRole);
    }

    public boolean doesActionGiveActorTheRole() {
        return (boolean) getParameters().get(Parameters.ACTION_ENABLES_ACTOR_ROLE).getValue();
    }

    @Override
    protected void addOwnParameters(Map<ParameterId, Variable> parameterMap) {
        // super.addOwnParameters(parameterMap); // Is abstract.
        parameterMap.put(Parameters.ACTION_ENABLES_ACTOR_ROLE, new BooleanVariable(true)); // Sensible default.
    }
}
