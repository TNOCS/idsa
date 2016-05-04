package nl.tno.idsa.library.relations.role_requires_action;

import nl.tno.idsa.framework.semantics_impl.relations.ActionEnablesRole;
import nl.tno.idsa.library.actions.GetSick;
import nl.tno.idsa.library.roles.MedicalVictim;

/**
 * Created by jongsd on 7-8-15.
 */
// TODO Add ignore unused. Document the class.
public class GetSickCreatesMedicalVictim extends ActionEnablesRole {
    public GetSickCreatesMedicalVictim() {
        super(GetSick.class, MedicalVictim.class, true);
    }
}
