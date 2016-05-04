package nl.tno.idsa.framework.behavior.models;

import nl.tno.idsa.framework.agents.Agent;
import nl.tno.idsa.framework.world.IGeometry;
import nl.tno.idsa.framework.world.Time;

/**
 * Model that allows agents to arrive somewhere at different times.
 * Once they have all arrived, they all perform a single step.
 */
public abstract class AsynchronousSingleStepModel extends Model {
    private boolean hasRun = false;
    private ModelLock lock = new ModelLock(this);

    @Override
    public boolean agentsCanWaitBeforeStarting() {
        return true;
    }

    @Override
    protected boolean doWaitingStep(Agent agent, double durationInSeconds) {
        return true; // Simply wait. Subclasses may decide to do something else than nothing.
    }

    @Override
    public boolean shouldWaitForAgents() {
        return lock.shouldWait();
    }

    @Override
    public void notifyNewEndTime(Time newTime) {

    }

    @Override
    public void notifyInterrupted(Agent agent) {

    }

    @Override
    protected boolean hasNextStep(Agent agent) {
        return !hasRun;
    }

    protected final boolean doStep(double durationInSeconds) { // Final so we don't implement it in subclasses.
        if (!hasRun) {
            hasRun = true;
            return doSingleStep();
        }
        return false;
    }

    /**
     * Subclasses implement this method instead of doStep, as other models do.
     */
    protected abstract boolean doSingleStep();

    @Override
    protected long doEstimateMinimumTimeToExecute(IGeometry currentActorLocation) {
        return 0;
    }

    private static class ModelLock {
        private final Model owner;

        public ModelLock(Model owner) {
            this.owner = owner;
        }

        public boolean shouldWait() {
            for (Agent aAgent : owner.getActors()) {
                if (!aAgent.isRunning(owner)) {
                    return true; // Some agent is not ready with a previous model yet.
                }
            }
            return false;
        }
    }
}
