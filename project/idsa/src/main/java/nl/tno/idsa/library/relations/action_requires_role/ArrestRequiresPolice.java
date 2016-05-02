package nl.tno.idsa.library.relations.action_requires_role;

import nl.tno.idsa.framework.semantics_impl.relations.RoleEnablesAction;
import nl.tno.idsa.library.actions.Arrest;
import nl.tno.idsa.library.roles.Police;

/**
 * Created by jongsd on 7-8-15.
 */
// TODO Add ignore unused. Document the class.
public class ArrestRequiresPolice extends RoleEnablesAction {
    public ArrestRequiresPolice() {
        super(Police.class, Arrest.class, true);
    }
}
