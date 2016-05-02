package nl.tno.idsa.library.models;

import nl.tno.idsa.framework.agents.Agent;
import nl.tno.idsa.framework.messaging.Messenger;
import nl.tno.idsa.framework.semantics_impl.groups.Group;
import nl.tno.idsa.library.roles.StreetThief;

/**
 * Created by jongsd on 26-8-15.
 */
@SuppressWarnings("unused")
public class ModelStreetTheft extends SetRoleModel {

    public ModelStreetTheft() {
        super(StreetThief.class, Who.ACTORS);
    }

    @Override
    public boolean doSingleStep() {
        Group agents = getActors();
        for (Agent agent : agents) {
            agent.setRole(StreetThief.class);
        }
        Messenger.broadcast(String.format("Agent(s) %s is/are thieving on the street at %s", getActors(), getLocationAndEndTime()));
        return true;
    }
}
