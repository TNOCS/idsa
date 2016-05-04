package nl.tno.idsa.library.relations.role_requires_action;

import nl.tno.idsa.framework.semantics_impl.relations.ActionEnablesRole;
import nl.tno.idsa.library.actions.ParticipateInProcession;
import nl.tno.idsa.library.roles.Reveller;

/**
 * Created by jongsd on 7-8-15.
 */
// TODO Add ignore unused. Document the class.
public class ProcessionCreatesReveller extends ActionEnablesRole {
    public ProcessionCreatesReveller() {
        super(ParticipateInProcession.class, Reveller.class, true);
    }
}
