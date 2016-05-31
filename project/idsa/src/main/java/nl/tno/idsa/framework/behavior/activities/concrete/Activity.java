package nl.tno.idsa.framework.behavior.activities.concrete;

import nl.tno.idsa.framework.agents.Agent;
import nl.tno.idsa.framework.behavior.activities.possible.PossibleActivity;
import nl.tno.idsa.framework.behavior.models.Model;
import nl.tno.idsa.framework.semantics_impl.groups.Group;
import nl.tno.idsa.framework.world.Environment;
import nl.tno.idsa.framework.world.Time;
import nl.tno.idsa.framework.world.Vertex;

/**
 * Base class for concrete activities.
 */
public abstract class Activity implements Comparable {

    private final Environment environment;
    private Vertex startLocation;
    private Time startTime;
    private Vertex endLocation;
    private Time endTime;

    private Group participants;

    private final PossibleActivity possibleActivity;

    public Activity(PossibleActivity possibleActivity, Environment environment, Vertex startLocation, Time startTime, Vertex endLocation, Time endTime, Group participants) {
        this.possibleActivity = possibleActivity;
        this.environment = environment;
        this.startTime = startTime;
        this.endTime = endTime;
        this.startLocation = startLocation;
        this.endLocation = endLocation;
        this.participants = participants;
    }

    @Override
    public int compareTo(Object o) {
        Activity a = (Activity) o;
        return startTime.compareTo(a.getStartTime());
    }

    public PossibleActivity getPossibleActivity() {
        return possibleActivity;
    }

    public Environment getEnvironment() {
        return environment;
    }

    public Time getStartTime() {
        return startTime;
    }

    public Time getEarliestPossibleStartTime() {
        TimeInterval theInterval = possibleActivity.getActivityTimeSlots().getPossibleTimeIntervalContaining(getStartTime(), getEndTime());
        if (theInterval != null) {
            Time earliestStart = theInterval.getStartTime();
            Time longestDuration = getEndTime().decrementByMinutes(possibleActivity.getMaximalDurationInMinutes());
            return earliestStart.latest(longestDuration); // The latest of the two.
        }
        return getStartTime();
    }

    public void setStartTime(Time startTime) {
        this.startTime = startTime;
    }

    public Time getEndTime() {
        return endTime;
    }

    public Time getLatestPossibleEndTime() {
        TimeInterval theInterval = possibleActivity.getActivityTimeSlots().getPossibleTimeIntervalContaining(getStartTime(), getEndTime());
        if (theInterval != null) {
            Time latestEnd = theInterval.getEndTime();
            Time longestDuration = getStartTime().incrementByMinutes(possibleActivity.getMaximalDurationInMinutes());
            return latestEnd.earliest(longestDuration); // The earliest of the two.
        }
        return getEndTime();
    }

    /**
     * Note: models support changing the end time after being initialized,
     * but unpredictable behavior may occur if this is done during runtime.
     * For example, a model will not spontaneously restart if the end time
     * is changed from a time before now to a time after now.
     */
    public void setEndTime(Time endTime) {
        this.endTime = endTime;
        getModel().notifyNewEndTime(endTime);
    }

    public Group getParticipants() {
        return participants;
    }

    public Vertex getStartLocation() {
        return startLocation;
    }

    public Vertex getEndLocation() {
        return endLocation;
    }

    public boolean startsAtHomeFor(Agent agent) {
        return (startLocation.equals(agent.getHouse()));
    }

    public boolean endsAtHomeFor(Agent agent) {
        return (endLocation.equals(agent.getHouse()));
    }

    public boolean isAtHomeFor(Agent agent) {
        return startsAtHomeFor(agent) && endsAtHomeFor(agent);
    }

    public boolean isMoving() {
        return !startLocation.equals(endLocation);
    }

    public abstract Model getModel();

    public String getName() {
        return getPossibleActivity().getName();
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{" +
                "possibleActivity=" + possibleActivity.getClass().getSimpleName() +
                ", startTime=" + startTime +
                ", endTime=" + endTime +
                ", startLocation=" + startLocation.getPoint() +
                ", endLocation=" + endLocation.getPoint() +
                ", participants=" + participants +
                '}';
    }
}
