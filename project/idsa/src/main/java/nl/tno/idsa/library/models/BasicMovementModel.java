package nl.tno.idsa.library.models;

import nl.tno.idsa.framework.agents.Agent;
import nl.tno.idsa.framework.behavior.models.Model;
import nl.tno.idsa.framework.messaging.Messenger;
import nl.tno.idsa.framework.semantics_impl.groups.Group;
import nl.tno.idsa.framework.semantics_impl.locations.LocationAndTime;
import nl.tno.idsa.framework.world.*;

import java.util.Arrays;

/**
 * Created by jongsd on 18-8-15.
 */
@SuppressWarnings("unused")
public class BasicMovementModel extends Model {

    public static final double WALKING_SPEED_MS = 4d / 3.6;      // TODO Hardcoded. Refer to comfortable speeds? ...
    // (If so, sampling new agents becomes unpredictable; a slow person close-by may not be faster than a fast person further away!)
    public static final double RUNNING_SPEED_MS = 15d / 3.6;      // TODO Hardcoded.
    private static final double ARRIVAL_DISTANCE_TOLERANCE = 0.5; // TODO Reduce once agents steer better, e.g. to 0.25.

    private final double speedInMs;  // The target speed (maximum).

    private long forcedStartTime = LocationAndTime.UNDEFINED_TIME;
    private boolean startTimeDependsOnEndTime;

    private double[] speedsInMs;  // Individual agent speeds may vary.
    private boolean[] agentsArrived;
    private Path[] plannedPaths;

    public BasicMovementModel(double speedInMs) {
        this.speedInMs = speedInMs;
        this.startTimeDependsOnEndTime = false;
    }

    @Override
    public void setActors(Group actors) {
        super.setActors(actors);
        if (actors != null) {
            this.agentsArrived = new boolean[this.getActors().size()];
            this.speedsInMs = new double[this.getActors().size()];
            this.plannedPaths = new Path[this.getActors().size()];
            Arrays.fill(agentsArrived, false);
            Arrays.fill(speedsInMs, speedInMs);
            Arrays.fill(plannedPaths, null);
        }
    }

    public void setStartTimeDependsOnEndTime(boolean startTimeDependsOnEndTime) {
        this.startTimeDependsOnEndTime = startTimeDependsOnEndTime;
    }

    public void setForcedStartTime(long forcedStartTime) {
        this.forcedStartTime = forcedStartTime;
    }

    private boolean shouldWaitForForcedStartTime() {
        return forcedStartTime != LocationAndTime.UNDEFINED_TIME && getEnvironment() != null && getEnvironment().getTime().getNanos() < forcedStartTime;
    }

    @Override
    public boolean agentsCanWaitBeforeStarting() {
        return false; // TODO At the moment, agents all start to move at the same time.
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
        // Reset the planned paths so the speeds are correctly calculated.
        for (int i = 0; i < plannedPaths.length; i++) {
            this.plannedPaths[i] = null;
        }
    }

    @Override
    public void notifyInterrupted(Agent agent) {
        // Clear existing path.
        this.plannedPaths[getActorIndex(agent)] = null;
    }

    @Override
    public boolean hasNextStep(Agent agent) {   // Agent argument is ignored. (Note: the agent provided here may not even be a member of the model actors/targets.)

        // Should we wait for the model to start?
        boolean hasNextStep = shouldWaitForForcedStartTime();

        // Check whether there is agent that still has to arrive at the destination
        for (int i = 0; !hasNextStep && i < this.agentsArrived.length; ++i) {
            if (!this.agentsArrived[i]) {
                hasNextStep = true;
            }
        }

        // And return.
        return hasNextStep;
    }

    private int getActorIndex(Agent a) {
        return this.getActors().indexOf(a);
    }

    @Override
    public boolean doStep(double durationInSeconds) {

        if (shouldWaitForForcedStartTime()) {
            return true;
        }

        boolean stepped = false;
        long currentTime = getEnvironment().getTime().getNanos();
        long endTime = getLocationAndEndTime().getTimeNanos();
        boolean endTimeSpecified = (endTime != LocationAndTime.UNDEFINED_TIME) || getExplicitDuration() != LocationAndTime.UNDEFINED_TIME;
        if (endTime == LocationAndTime.UNDEFINED_TIME) {
            endTime = currentTime + getExplicitDuration();
        }
        long availableTime = endTimeSpecified ? endTime - currentTime : LocationAndTime.UNDEFINED_TIME;
        IGeometry location = getLocationAndEndTime().getLocation();

        // Create paths per agent.
        for (int j = 0; j < agentsArrived.length; ++j) {

            // If the agent arrived or has a valid path, skip this one.
            if (agentsArrived[j] || plannedPaths[j] != null) {
                continue;
            }

            // Create a path for the agent.
            Path path = null;
            Agent agent = getActors().get(j);

            // Walk along a given poly line.
            if (location instanceof PolyLine) {
                Point[] points = location.getPoints();
                path = getEnvironment().getWorld().getPath(agent.getLocation(), location.getFirstPoint());
                path.append(points);
            }

            // Or walk towards the end of the given location.
            else {
                Point locationPoint = getLocationAndEndTime().getLocation().getLastPoint();
                path = getEnvironment().getWorld().getPath(agent.getLocation(), locationPoint);
                if (startTimeDependsOnEndTime) {
                    forcedStartTime = getLocationAndEndTime().getTimeNanos() - (long) (path.lengthInM() / speedInMs) * Time.NANO_SECOND;
                    //System.out.println("start: " + new Time(forcedStartTime) + ", end: " + new Time(getLocationAndEndTime().getTimeNanos())) ;
                }
            }

            // Regulate speed.
            if (agentsMustArriveSimultaneously() && endTimeSpecified) {
                double pathLength = path.lengthInM(); // in m
                double speedMs = (pathLength * Time.NANO_SECOND) / availableTime;

                // TODO I have seen this happening. It should not happen.
                if (pathLength < 0) {
                    System.err.println("Negative path length: " + path);      // TODO More graceful error.
                }
                if (availableTime < 0) {
                    System.err.println("Negative available time: " + availableTime + "; end time=" + endTime + " current time=" + currentTime); // TODO More graceful error.
                }

                this.speedsInMs[j] = speedMs;
            }

            // Store paths.
            plannedPaths[j] = path;

            // Notify.
            if (isPartOfIncident()) {
                Messenger.broadcast(agent, "will walk towards %s with a speed of %skm/h.", getLocationAndEndTime().getLocation().getLastPoint(), 3.6 * speedsInMs[j]);
            }
        }

        // Walk
        for (int j = 0; j < agentsArrived.length; ++j) {
            if (!agentsArrived[j]) {
                Agent agent = this.getActors().get(j);
                // Try to perform movement
                //boolean hasSteered = agent.steer(durationInSeconds);
                agent.setLocation(plannedPaths[j].moveAlong(agent.getLocation(), speedsInMs[j] * durationInSeconds));
                stepped = true;
                // Check whether the agent is done
                agentsArrived[j] = plannedPaths[j].endReached() || agent.getLocation().euclideanDistanceTo(getLocationAndEndTime().getLocation().getLastPoint()) <= ARRIVAL_DISTANCE_TOLERANCE;

                // Notify.
                if (agentsArrived[j] && isPartOfIncident()) {
                    Messenger.broadcast(agent, "arrived at %s.", getLocationAndEndTime().getLocation().getLastPoint());
                }
            }
        }

        // And done.
        return stepped;
    }

    @Override
    protected long doEstimateMinimumTimeToExecute(IGeometry currentActorLocation) {
        double actorDistance = getEnvironment().getWorld().getPathLengthInM(currentActorLocation.getFirstPoint(), getLocationAndEndTime().getLocation().getLastPoint());
        double actorTime = actorDistance / speedInMs; // In seconds.
        long duration = (long) (actorTime * Time.NANO_SECOND);
        // System.out.format("     [[ %s | p1 %s | p2 %s | distance %skm | speed %skm/h | time %sh | duration %sms ]]%n", getClass().getSimpleName(), currentActorLocation.getFirstPoint(), getLocationAndEndTime().getLocation().getLastPoint(), actorDistance, speedInMs, actorTime, duration/1000000);
        return duration;
    }

    // For debug visualisation purposes     TODO Keep?
    public Path getPath(Agent a) {
        Path result = null;
        int index = getActorIndex(a);
        if (index >= 0) {
            result = plannedPaths[index];
        }
        return result;
    }
}
