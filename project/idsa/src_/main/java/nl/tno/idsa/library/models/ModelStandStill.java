package nl.tno.idsa.library.models;

import nl.tno.idsa.framework.agents.Agent;
import nl.tno.idsa.framework.behavior.models.Model;
import nl.tno.idsa.framework.world.IGeometry;
import nl.tno.idsa.framework.world.Time;

/**
 * Created by jongsd on 17-9-15.
 */
public class ModelStandStill extends Model {
    @Override
    public boolean agentsCanWaitBeforeStarting() {
        return true;
    }

    @Override
    protected boolean doWaitingStep(Agent agent, double durationInSeconds) {
        return true;
    }

    @Override
    public boolean shouldWaitForAgents() {
        return false;
    }

    @Override
    public void notifyNewEndTime(Time newTime) {

    }

    @Override
    public void notifyInterrupted(Agent agent) {

    }

    @Override
    protected boolean hasNextStep(Agent agent) {
        return getEnvironment().getTime().getNanos() < getLocationAndEndTime().getTimeNanos();
    }

    @Override
    protected boolean doStep(double durationInSeconds) {
        return true;
    }

    @Override
    protected long doEstimateMinimumTimeToExecute(IGeometry currentActorLocation) {
        return 0;
    }
}
