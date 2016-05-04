package nl.tno.idsa.library.models;

import nl.tno.idsa.framework.agents.Agent;
import nl.tno.idsa.framework.behavior.models.SynchronousSingleStepModel;
import nl.tno.idsa.framework.semantics_impl.roles.Role;

/**
 * Created by jongsd on 14-10-15.
 */
public class SetRoleModel extends SynchronousSingleStepModel {

    private Class<? extends Role> desiredRole;

    protected static enum Who {ACTORS, TARGETS, ALL}

    protected Who who;

    public SetRoleModel(Class<? extends Role> desiredRole, Who who) {
        this.desiredRole = desiredRole;
        this.who = who;
    }

    @Override
    protected boolean doSingleStep() {
        if (who == Who.ACTORS || who == Who.ALL) {
            if (getActors() != null) {
                for (Agent agent : getActors()) {
                    agent.setRole(desiredRole);
                }
            }
        }
        if (who == Who.TARGETS || who == Who.ALL) {
            if (getTargets() != null) {
                for (Agent agent : getTargets()) {
                    agent.setRole(desiredRole);
                }
            }
        }
        return true;
    }
}
