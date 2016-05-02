package nl.tno.idsa.library.relations.role_requires_action;

import nl.tno.idsa.framework.semantics_impl.relations.ActionEnablesRole;
import nl.tno.idsa.library.actions.StreetTheft;
import nl.tno.idsa.library.roles.StreetThief;

/**
 * Created by jongsd on 7-8-15.
 */
// TODO Add ignore unused. Document the class.
// TODO Are all semantic classes final?
public class StreetTheftCreatesStreetThief extends ActionEnablesRole {
    public StreetTheftCreatesStreetThief() {
        super(StreetTheft.class, StreetThief.class, true);
    }
}
