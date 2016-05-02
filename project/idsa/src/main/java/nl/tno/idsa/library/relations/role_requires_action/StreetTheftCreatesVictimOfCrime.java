package nl.tno.idsa.library.relations.role_requires_action;

import nl.tno.idsa.framework.semantics_impl.relations.ActionEnablesRole;
import nl.tno.idsa.library.actions.StreetTheft;
import nl.tno.idsa.library.roles.VictimOfCrime;

/**
 * Created by jongsd on 7-8-15.
 */
// TODO Add ignore unused. Document the class.
public class StreetTheftCreatesVictimOfCrime extends ActionEnablesRole {
    public StreetTheftCreatesVictimOfCrime() {
        super(StreetTheft.class, VictimOfCrime.class, false);
    }
}
