package nl.tno.idsa.library.relations.action_requires_role;

import nl.tno.idsa.framework.semantics_impl.relations.RoleEnablesAction;
import nl.tno.idsa.library.actions.MoveTo;
import nl.tno.idsa.library.roles.Civilian;

/**
 * Created by jongsd on 7-8-15.
 */
// TODO Add ignore unused. Document the class.
public class MoveToRequiresCivilian extends RoleEnablesAction {
    public MoveToRequiresCivilian() {
        super(Civilian.class, MoveTo.class, true);
    }
}
