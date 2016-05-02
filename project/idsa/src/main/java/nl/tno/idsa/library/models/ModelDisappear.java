package nl.tno.idsa.library.models;

import nl.tno.idsa.framework.agents.Agent;
import nl.tno.idsa.framework.behavior.models.SynchronousSingleStepModel;
import nl.tno.idsa.framework.messaging.Messenger;

/**
 * Created by jongsd on 26-8-15.
 */
@SuppressWarnings("unused")
public class ModelDisappear extends SynchronousSingleStepModel {

    public ModelDisappear() {
    }

    @Override
    public boolean doSingleStep() {
        for (Agent agent : getActors()) {
            this.getEnvironment().removeSimulatedObject(agent);
            Messenger.broadcast(String.format("Agent %s has been removed.", agent));
        }
        if (getTargets() != null) {
            for (Agent agent : getTargets()) {
                this.getEnvironment().removeSimulatedObject(agent);
                Messenger.broadcast(String.format("Agent %s has been removed.", agent));
            }
        }
        return true;
    }
}
