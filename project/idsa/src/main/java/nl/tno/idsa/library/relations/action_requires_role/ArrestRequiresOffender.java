package nl.tno.idsa.library.relations.action_requires_role;

import nl.tno.idsa.framework.semantics_impl.relations.RoleEnablesAction;
import nl.tno.idsa.library.actions.Arrest;
import nl.tno.idsa.library.roles.Offender;

/**
 * Created by jongsd on 7-8-15.
 */
// TODO Add ignore unused. Document the class.
public class ArrestRequiresOffender extends RoleEnablesAction {
    public ArrestRequiresOffender() {
        super(Offender.class, Arrest.class, false);
    }
}
