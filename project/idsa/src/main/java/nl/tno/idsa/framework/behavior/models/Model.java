package nl.tno.idsa.framework.behavior.models;

import nl.tno.idsa.framework.agents.Agent;
import nl.tno.idsa.framework.semantics_impl.groups.Group;
import nl.tno.idsa.framework.semantics_impl.locations.LocationAndTime;
import nl.tno.idsa.framework.world.Environment;
import nl.tno.idsa.framework.world.IGeometry;
import nl.tno.idsa.framework.world.Time;

import java.util.HashSet;
import java.util.Set;

/**
 * Model superclass.
 */
public abstract class Model {

    private Environment environment;
    private Set<Model> modelsToWaitFor;

    private Group actors;
    private Group targets;

    private LocationAndTime locationAndEndTime;

    private long explicitDuration = LocationAndTime.UNDEFINED_TIME;
    private long currentDuration = 0;

    private boolean agentsMustArriveSimultaneously = false;
    private boolean partOfIncident = false;

    private long lastUpdateTime = -1; // Prevents models from running twice if multiple agents have them on their stack.
    private boolean finished = false;

    public Environment getEnvironment() {
        return environment;
    }

    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    /**
     * Denote that this model must wait until the other model is finished. Called by the planner.
     */
    public void addModelToWaitFor(Model modelToWaitFor) {
        if (this.modelsToWaitFor == null) {
            this.modelsToWaitFor = new HashSet<>();
        }
        this.modelsToWaitFor.add(modelToWaitFor);
    }

    /**
     * Ask whether this model is waiting for another model to finish.
     */
    public boolean isWaitingForOtherModel() {
        if (modelsToWaitFor != null) {
            for (Model model : modelsToWaitFor) {
                for (Agent a : getActors()) {
                    if (!model.isFinished(a)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public Group getActors() {
        return actors;
    }

    public void setActors(Group actors) {
        this.actors = actors;
    }

    public Group getTargets() {
        return targets;
    }

    public void setTargets(Group targets) {
        this.targets = targets;
    }

    public LocationAndTime getLocationAndEndTime() {
        return locationAndEndTime;
    }

    public void setLocationAndEndTime(LocationAndTime locationAndEndTime) {
        this.locationAndEndTime = locationAndEndTime;
    }

    public long getExplicitDuration() {
        return explicitDuration;
    }

    public void setExplicitDuration(long explicitDuration) {
        this.explicitDuration = explicitDuration;
    }

    /**
     * Returns whether agents must arrive at this model's location at exactly the same time.
     */
    public boolean agentsMustArriveSimultaneously() {
        return agentsMustArriveSimultaneously;
    }

    /**
     * Set whether agents must arrive at this model's location at exactly the same time.
     */
    public void setAgentsMustArriveSimultaneously(boolean agentsMustArriveSimultaneously) {
        this.agentsMustArriveSimultaneously = agentsMustArriveSimultaneously;
    }

    /**
     * Returns whether agents that arrive before other agents can perform some kind of waiting behavior, controlled by this model.
     */
    public abstract boolean agentsCanWaitBeforeStarting();

    /**
     * Called by an agent when the agent in question is waiting for others to arrive or for the model's real behavior to begin.
     */
    public boolean nextWaitingStep(Agent agent, double durationInSeconds) {
        return agentsCanWaitBeforeStarting() && doWaitingStep(agent, durationInSeconds);
    }

    /**
     * Subclasses implement waiting behavior in this method.
     */
    protected abstract boolean doWaitingStep(Agent agent, double durationInSeconds);

    /**
     * If agents can arrive at their own chosen time, and have to perform the model's action simultaneously, the model
     * is waiting for agents that are not there yet. Obviously, only returns true for models in which agents can wait and
     * agents do not have to arrive exactly at the same time.
     */
    public abstract boolean shouldWaitForAgents();

    /**
     * Is the model done?
     */
    public boolean isFinished(Agent agent) {
        return finished || (finished = (!shouldWaitForAgents() && !hasNextStep(agent) && !shouldWaitForEndTime()));
    }

    // The model should not finish too soon.
    // TODO We could introduce special behavior for agents that have performed the model behavior and are now waiting for the end time.
    private boolean shouldWaitForEndTime() {
        if (getLocationAndEndTime() != null && getLocationAndEndTime().isTimeDefined()) {
            if (environment.getTime().getNanos() < getLocationAndEndTime().getTimeNanos()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Called when the end time of the model changes. (Note: start time does not matter in our models.)
     * Note that models support changing the end time after being initialized, but unpredictable behavior
     * may occur if this is done during model execution. (E.g. we move the end time forward for a single step
     * model, meaning that the start time also moves, but start time in our code only depends on the end time
     * of the previous model; therefore, the model still runs at the previous end time, because the previous
     * model's end time has not been changed.) Also, if we move the end time from BEFORE to AFTER the current
     * time, the model will generally not magically start running again, because it is already done.
     */
    public abstract void notifyNewEndTime(Time newTime);

    /**
     * Called when another model takes over. Note that the model may become active again after some time.
     */
    public abstract void notifyInterrupted(Agent agent);

    /**
     * This method can/will be called multiple times per time step, so make sure in implementations that it
     * returns the same answer if called multiple times within one time step. In other words, this is not
     * a method to clean up e.g. stacks or iterators in. Do that in doStep.
     */
    protected abstract boolean hasNextStep(Agent agent);

    /**
     * Perform the next model step.
     * This automatically does nothing if the model needs to wait for a previous model.
     */
    public boolean nextStep(double durationInSeconds) {
        if (isWaitingForOtherModel()) {
            return true;
        }

        // End running the model if an explicit duration was set.
        if (explicitDuration != LocationAndTime.UNDEFINED_TIME) {
            if (explicitDuration < currentDuration) {
                return false;
            }
        }

        // Models with multiple agents are called multiple times. Prevent this by calling them once per time step.
        long currentTime = getEnvironment().getTime().getNanos();
        boolean stepped = false;
        if (this.lastUpdateTime != currentTime) {
            this.lastUpdateTime = currentTime;

            // Do a step.
            stepped = doStep(durationInSeconds);
        }

        currentDuration += durationInSeconds;
        return stepped;
    }

    /**
     * Implementations of the model class perform a model step here.
     */
    protected abstract boolean doStep(double durationInSeconds);

    /**
     * Models may be either running to due daily behavior, or due to an incident. If models know they are part of an incident,
     * so will the agents. This allows us to e.g. display these agents differently or treat them preferentially.
     */
    public boolean isPartOfIncident() {
        return partOfIncident;
    }

    /**
     * Models may be either running to due daily behavior, or due to an incident.
     */
    public void setPartOfIncident(boolean partOfIncident) {
        this.partOfIncident = partOfIncident;
    }

    /**
     * Give a reliable estimate of the time it would take to run this model if the behavior was performed as fast as possible.
     */
    public long estimateMinimumTimeToExecute(IGeometry currentActorLocation) {
        if (explicitDuration != LocationAndTime.UNDEFINED_TIME) {
            return explicitDuration;
        } else {
            return doEstimateMinimumTimeToExecute(currentActorLocation);
        }
    }

    /**
     * Return an estimated duration for this model (given that there is no explicit duration set).
     */
    protected abstract long doEstimateMinimumTimeToExecute(IGeometry currentActorLocation);

    @Override
    public String toString() {
        return getClass().getSimpleName();
    }
}
