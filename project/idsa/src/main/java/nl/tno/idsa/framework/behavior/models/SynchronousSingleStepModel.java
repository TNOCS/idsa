package nl.tno.idsa.framework.behavior.models;

import nl.tno.idsa.framework.agents.Agent;
import nl.tno.idsa.framework.world.IGeometry;
import nl.tno.idsa.framework.world.Time;

/**
 * Model that allows all agents to do one single step, once they arrived at the scene.
 * They have to arrive at exactly the same time. This is why the model instructs the planner
 * that agents cannot wait.
 */
public abstract class SynchronousSingleStepModel extends Model {

    private boolean hasRun = false;

    @Override
    protected long doEstimateMinimumTimeToExecute(IGeometry currentActorLocation) {
        return 0;
    }

    @Override
    public boolean agentsCanWaitBeforeStarting() {
        return false;
    }

    @Override
    protected boolean doWaitingStep(Agent agent, double durationInSeconds) {
        return false;
    }

    @Override
    public boolean shouldWaitForAgents() {
        return false;
    }

    @Override
    public void notifyNewEndTime(Time newTime) {
        // Of course other models need to know when to stop running such that this model
        // runs at the right time. But we assume the end time is only changed before running.
    }

    @Override
    public void notifyInterrupted(Agent agent) {
        // Cannot be interrupted.
    }

    @Override
    protected boolean hasNextStep(Agent agent) {
        return !hasRun;
    }

    @Override
    public boolean doStep(double durationInSeconds) {
        doSingleStep();
        hasRun = true;
        return true;
    }

    /**
     * Subclasses implement this method instead of doStep, as other models do.
     */
    protected abstract boolean doSingleStep();
}
