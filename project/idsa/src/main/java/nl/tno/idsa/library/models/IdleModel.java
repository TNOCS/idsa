package nl.tno.idsa.library.models;

import nl.tno.idsa.framework.agents.Agent;
import nl.tno.idsa.framework.behavior.models.SynchronousSingleStepModel;
import nl.tno.idsa.framework.messaging.Messenger;

/**
 * This model blocks with no behavior until the time provided in LocationAndTime has passed.
 */
public class IdleModel extends SynchronousSingleStepModel {

    @Override
    protected boolean hasNextStep(Agent agent) {
        return false; // There is no step. The model simply waits for the end time.
    }

    @Override
    protected boolean doSingleStep() {
        // Doesn't do anything except wait for the time in the location & time constraints.
        Messenger.broadcast("Idle model finished. This should not be printed."); // TODO...
        return false;
    }
}
