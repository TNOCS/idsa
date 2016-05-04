package nl.tno.idsa.library.relations.role_requires_action;

import nl.tno.idsa.framework.semantics_impl.relations.ActionEnablesRole;
import nl.tno.idsa.library.actions.SpawnPolice;
import nl.tno.idsa.library.roles.Police;

/**
 * Created by jongsd on 7-8-15.
 */
// TODO Add ignore unused. Document the class.
public class SpawnPoliceCreatesPolice extends ActionEnablesRole {
    public SpawnPoliceCreatesPolice() {
        super(SpawnPolice.class, Police.class, true);
    }
}
