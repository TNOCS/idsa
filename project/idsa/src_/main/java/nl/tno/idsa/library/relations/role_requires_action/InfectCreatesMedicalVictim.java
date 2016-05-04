package nl.tno.idsa.library.relations.role_requires_action;

import nl.tno.idsa.framework.semantics_impl.relations.ActionEnablesRole;
import nl.tno.idsa.library.actions.Infect;
import nl.tno.idsa.library.roles.MedicalVictim;

/**
 * Created by jongsd on 7-8-15.
 */
// TODO Add ignore unused. Document the class. Explain that separate relations create extensibility.
public class InfectCreatesMedicalVictim extends ActionEnablesRole {
    public InfectCreatesMedicalVictim() {
        super(Infect.class, MedicalVictim.class, false); // The target is infected.
    }
}
