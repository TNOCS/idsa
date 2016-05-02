package nl.tno.idsa.library.relations.role_requires_action;

import nl.tno.idsa.framework.semantics_impl.relations.ActionEnablesRole;
import nl.tno.idsa.library.actions.SpawnAmbulance;
import nl.tno.idsa.library.roles.Ambulance;

/**
 * Created by jongsd on 7-8-15.
 */
// TODO Add ignore unused. Document the class.
public class SpawnAmbulanceCreatesAmbulance extends ActionEnablesRole {
    public SpawnAmbulanceCreatesAmbulance() {
        super(SpawnAmbulance.class, Ambulance.class, true);
    }
}
