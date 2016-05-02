package nl.tno.idsa.library.relations.action_requires_role;

import nl.tno.idsa.framework.semantics_impl.relations.RoleEnablesAction;
import nl.tno.idsa.library.actions.MoveToTarget;
import nl.tno.idsa.library.roles.Civilian;

/**
 * Created by jongsd on 7-8-15.
 */
// TODO Add ignore unused. Document the class.
public class MoveToTargetRequiresCivilian extends RoleEnablesAction {
    public MoveToTargetRequiresCivilian() {
        super(Civilian.class, MoveToTarget.class, false);
    }
}
