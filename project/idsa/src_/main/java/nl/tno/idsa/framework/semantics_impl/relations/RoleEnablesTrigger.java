package nl.tno.idsa.framework.semantics_impl.relations;

import nl.tno.idsa.framework.behavior.triggers.AreaTrigger;
import nl.tno.idsa.framework.behavior.triggers.MovingAreaTrigger;
import nl.tno.idsa.framework.semantics_base.relations.EnablingRelation;
import nl.tno.idsa.framework.semantics_impl.roles.Role;

/**
 * Created by jongsd on 17-9-15.
 */

// TODO Document class. Why is it abstract? Is it even used?

public abstract class RoleEnablesTrigger extends EnablingRelation<Role, AreaTrigger> {
    protected RoleEnablesTrigger(Class<? extends Role> enabler, Class<? extends AreaTrigger> enabled) {
        super(enabler, enabled);
    }

    public abstract MovingAreaTrigger createTrigger();
}
