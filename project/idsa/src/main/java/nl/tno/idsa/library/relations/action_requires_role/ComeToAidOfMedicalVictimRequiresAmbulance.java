package nl.tno.idsa.library.relations.action_requires_role;

import nl.tno.idsa.framework.semantics_impl.relations.RoleEnablesAction;
import nl.tno.idsa.library.actions.ComeToAidOfMedicalVictim;
import nl.tno.idsa.library.roles.Ambulance;

/**
 * Created by jongsd on 7-8-15.
 */
// TODO Add ignore unused. Document the class.
public class ComeToAidOfMedicalVictimRequiresAmbulance extends RoleEnablesAction {
    public ComeToAidOfMedicalVictimRequiresAmbulance() {
        super(Ambulance.class, ComeToAidOfMedicalVictim.class, true);
    }
}
