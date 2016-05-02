package nl.tno.idsa.library.models;

import nl.tno.idsa.framework.agents.Agent;
import nl.tno.idsa.framework.behavior.models.SynchronousSingleStepModel;
import nl.tno.idsa.framework.messaging.Messenger;
import nl.tno.idsa.framework.population.Gender;
import nl.tno.idsa.framework.semantics_impl.actions.Action;
import nl.tno.idsa.framework.semantics_impl.groups.Group;
import nl.tno.idsa.framework.semantics_impl.roles.Role;
import nl.tno.idsa.framework.semantics_impl.variables.GroupVariable;

/**
 * Created by jongsd on 26-8-15.
 */
@SuppressWarnings("unused")
public class ModelSpawn extends SynchronousSingleStepModel {

    private final GroupVariable actorVariable;
    private final Class<? extends Role> desiredRole;

    public ModelSpawn(Action parent, Class<? extends Role> desiredRole) {
        this.desiredRole = desiredRole;
        actorVariable = parent.getActorVariable();
    }

    @Override
    public boolean doSingleStep() {
        Group spawned = new Group();
        for (int i = 0; i < actorVariable.getNumMembers(); ++i) {
            Agent newAgent = new Agent(32, Gender.MALE, null, null, getEnvironment().getYear()); // TODO Perhaps create a more diverse police force than 32-year old males? :)
            newAgent.setLocation(getLocationAndEndTime().getLocation().getLastPoint());
            newAgent.setRole(desiredRole);

            Messenger.broadcast(String.format("New agent %s created with role %s.", newAgent, desiredRole.getSimpleName()));

            spawned.add(newAgent);

            // TODO register new agent via population generator
            this.getEnvironment().addSimulatedObject(newAgent);
            // newAgent.setEnvironment(getEnvironment());
        }

        //System.out.println("Actor variable: " + actorVariable + " gets value: " + spawned);

        actorVariable.setValue(spawned);
        return true;
    }
}
