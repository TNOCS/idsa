package nl.tno.idsa.library.relations.action_requires_role;

import nl.tno.idsa.framework.semantics_impl.relations.RoleEnablesAction;
import nl.tno.idsa.library.actions.ComeToAidOfCrimeVictim;
import nl.tno.idsa.library.roles.VictimOfCrime;

/**
 * Created by jongsd on 7-8-15.
 */
// TODO Add ignore unused. Document the class.
// TODO Create another way to couple actor/target roles than using specific subclass actions and relations. I.e.: a target->actor role relation, or other way around (CrimeVictimRequiresPolice)
public class ComeToAidOfCrimeVictimRequiresCrimeVictim extends RoleEnablesAction {
    public ComeToAidOfCrimeVictimRequiresCrimeVictim() {
        super(VictimOfCrime.class, ComeToAidOfCrimeVictim.class, false); // The crew that comes to aid must suit the victim, so we need a specific role.
    }
}
