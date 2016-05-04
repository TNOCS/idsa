package nl.tno.idsa.library.relations.role_has_trigger;

import nl.tno.idsa.framework.behavior.triggers.MovingAreaTrigger;
import nl.tno.idsa.framework.semantics_base.objects.ParameterId;
import nl.tno.idsa.framework.semantics_impl.relations.RoleEnablesTrigger;
import nl.tno.idsa.framework.semantics_impl.variables.Variable;
import nl.tno.idsa.library.roles.Panicker;
import nl.tno.idsa.library.triggers.MovingDeterringTrigger;

import java.util.Map;


// TODO Add ignore unused. Document the class.
// TODO Pretty lame implementation.

public class PanickerDeters extends RoleEnablesTrigger {

    public PanickerDeters() {
        super(Panicker.class, MovingDeterringTrigger.class);
    }

    @Override
    protected void addOwnParameters(Map<ParameterId, Variable> parameterMap) {
        // super.addOwnParameters(parameterMap); // Is abstract.
    }

    @Override
    public MovingAreaTrigger createTrigger() {
        return new MovingDeterringTrigger(20, 0.8); // Creates a lot of panic close by.
    }
}
