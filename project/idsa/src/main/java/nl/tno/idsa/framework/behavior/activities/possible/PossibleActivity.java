package nl.tno.idsa.framework.behavior.activities.possible;

import nl.tno.idsa.Constants;
import nl.tno.idsa.framework.agents.Agent;
import nl.tno.idsa.framework.behavior.activities.concrete.Activity;
import nl.tno.idsa.framework.behavior.activities.concrete.BasicMovementActivity;
import nl.tno.idsa.framework.behavior.activities.concrete.LocationData;
import nl.tno.idsa.framework.behavior.activities.concrete.TimeInterval;
import nl.tno.idsa.framework.semantics_impl.groups.Group;
import nl.tno.idsa.framework.semantics_impl.locations.LocationFunction;
import nl.tno.idsa.framework.utils.RandomNumber;
import nl.tno.idsa.framework.world.*;
import nl.tno.idsa.tools.DebugPrinter;

import java.util.*;

/**
 * Base class for all possible activities. These describe the constraints for activities.
 */
public abstract class PossibleActivity implements Comparable<PossibleActivity> {

    private final Class<? extends LocationFunction>[] possibleLocationFunctions;
    private final PossibleTimeIntervals possibleTimeIntervals;

    public static enum Index {MOVE_TO, ACTIVITY, MOVE_FROM} // For created activities.

    /**
     * Create a possible activity.
     *
     * @param possibleTimeIntervals     Times the activity can take place in.
     * @param possibleLocationFunctions Places the activity can take place at (interpreted as an 'or').
     */
    @SafeVarargs
    public PossibleActivity(PossibleTimeIntervals possibleTimeIntervals, Class<? extends LocationFunction>... possibleLocationFunctions) {
        this.possibleTimeIntervals = possibleTimeIntervals;
        this.possibleLocationFunctions = possibleLocationFunctions;
    }

    /**
     * Return a priority between 0...1. In a future system we would love to be able to derive the priority from the
     * restrictiveness or need-based activity generation, but for now it is hardcoded.
     */
    public abstract double getPriority();

    /**
     * Adapt the likelihood of a possible activity depending on the agent.
     */
    public abstract double getMultiplier(Agent agent);

    /**
     * Create a concrete instance of the activity given the time constraints, location, and participants. Called by
     * the non-abstract createActivities method. This may return null if creating an activity is not possible, for
     * example if we try to plan a 2-hour activity an hour before the end of the day.
     */
    protected abstract Activity createActivity(Time start, Time end, LocationData locationData, Group participants);

    /**
     * Return the participants in this activity given the agent provided. This should include the agent itself, but not
     * necessarily the agents accompanying any of the agents. For example, Max (10) and Sophie (8) can go to the cinema,
     * accompanied by Annie (20). Participants are Max and Sophie, but not necessarily Annie in this case.
     */
    public Group getParticipants(Agent agent) {
        return accompanyMinors(agent, true);
    }

    /**
     * Return the agent(s) that bring the agent provided to this activity (e.g. a parent). This
     * should include the agent itself. Note that this is not checked if getAgentsAccompanying
     * returns more than one agent; we assume the accompanying agent(s) also bring this agent.
     */
    protected Group getAgentsBringing(Agent agent) {
        return accompanyMinors(agent, false);
    }

    /**
     * Return the agent(s) that pick up the agent provided from this activity (e.g. a parent). This
     * should include the agent itself. Note that this is not checked if getAgentsAccompanying
     * returns more than one agent; we assume the accompanying agent(s) also pick up this agent.
     */
    protected Group getAgentsPickingUp(Agent agent) {
        return accompanyMinors(agent, false);
    }

    /**
     * Return the agents accompanying the agent provided during this activity (e.g. a parent). This
     * should include the agent itself. These agents will be added to the participants if they are not
     * defined there explicitly.
     */
    protected Group getAgentsAccompanying(Agent agent) {
        return accompanyMinors(agent, true);
    }

    /**
     * Return the oldest child age that requires an older person to accompany them (default is 12).
     */
    protected int getMaxAgeRequiringAccompaniment() {
        return 12;
    }

    /**
     * Return the minimal age someone must have to accompany a child (default is 18).
     */
    protected int getMinAgeOfAccompaniment() {
        return 18;
    }

    // Cache who is accompanying who.
    private HashMap<Agent, Group> accompanying = new HashMap<>();

    /**
     * Used to make sure minors are accompanied by an older person from the same household.
     */
    // TODO At the moment, we don't only include an adult, but also all children in the household. ...
    // This prevents e.g. having to take two different kids to two different playgrounds.
    protected Group accompanyMinors(Agent agent, boolean fixAccompaniment) {
        Group accompanimentForAgent = accompanying.get(agent);
        if (accompanimentForAgent != null) {
            return accompanimentForAgent;
        }
        accompanimentForAgent = new Group(agent);
        boolean accompanimentFound = false;
        if (agent.getAge() <= getMaxAgeRequiringAccompaniment()) {
            for (Agent householdMember : agent.getHousehold()) {
                if (!accompanimentFound && householdMember.getAge() >= getMinAgeOfAccompaniment()) {
                    accompanimentForAgent.add(householdMember);
                    accompanimentFound = true;
                } else if (getMultiplier(householdMember) > 0 && householdMember.getAge() <= getMaxAgeRequiringAccompaniment() && !householdMember.equals(agent)) {
                    accompanimentForAgent.add(householdMember);
                }
            }
            if (fixAccompaniment) {
                accompanying.put(agent, accompanimentForAgent);
            }
        }
        // TODO Warn (or fail) if we are left with a group of only children.
        return accompanimentForAgent;
    }

    public Class<? extends LocationFunction>[] getPossibleLocationFunctions() {
        return possibleLocationFunctions;
    }

    /**
     * Get the number of unique agents participating, bringing, or picking up in this activity, given the agent.
     */
    public int getNumberAgentsInvolved(Agent agent) {
        return getAgentsInvolved(agent).size();
    }

    /**
     * Get the unique agents participating, bringing, or picking up in this activity, given the agent.
     */
    public Group getAgentsInvolved(Agent agent) {
        return mergeGroups(getParticipants(agent), getAgentsBringing(agent), getAgentsPickingUp(agent), getAgentsAccompanying(agent));
    }

    private Group mergeGroups(Group... groups) {
        Set<Agent> uniqueAgents = new HashSet<>();
        for (Group group : groups) {
            if (group != null) {
                uniqueAgents.addAll(group);
            }
        }
        return new Group(uniqueAgents);
    }

    /**
     * Get a name for the activity, i.e. by default, the class name minus the word "Possible".
     */
    public final String getName() {
        String name = getClass().getSimpleName();
        if (name.startsWith("Possible")) {
            name = name.substring(8);
        }
        return name;
    }

    /**
     * Create a concrete activity based on this possible activity, along with an activity to move towards and move away
     * from this concrete activity. This may return null if a concrete activity cannot be created.
     */
    public EnumMap<Index, Activity> createActivities(Agent agent, World world) {

        // TODO At the moment, we assume agents involved need to be there the whole time. ...
        // In practice, they might only be involved in bringing or picking up others. This means we are too restrictive
        // in planning our activities with multiple agents.
        Group participants = getAgentsInvolved(agent);

        DebugPrinter.println("               Create activity for " + getClass().getSimpleName());
        DebugPrinter.println("               Participants including all accompaniment: " + participants);

        // Quick fail if there are no people.
        if (participants == null || participants.size() == 0) {
            DebugPrinter.println("               No participants");
            return null;
        }

        // Determine the agent's availability.
        List<TimeInterval> freeTimeSlots = null;
        for (int i = 0; i < participants.size(); ++i) {
            Agent a = participants.get(i);
            if (freeTimeSlots == null) {
                freeTimeSlots = a.getFreeTimeSlots();
            } else {
                freeTimeSlots = TimeInterval.intersect(freeTimeSlots, a.getFreeTimeSlots());
            }
        }
        DebugPrinter.println("               Free time slots: " + freeTimeSlots);

        if (freeTimeSlots == null || freeTimeSlots.size() == 0) {
            return null;
        }

        // Initialize.
        final List<TimeInterval> activitySlots = getActivityTimeSlots().getPossibleTimeIntervals();
        DebugPrinter.println("               Possible time intervals: " + activitySlots);

        // Find the intersection between participant time slots and activity time slots
        freeTimeSlots = TimeInterval.intersect(freeTimeSlots, activitySlots);
        Collections.shuffle(freeTimeSlots, RandomNumber.getRandom()); // Not always in the morning please :-)

        // Try a few times.
        Activity suitableActivity = null;
        LocationData suitableLocationData = null;
        for (TimeInterval possibleTimeSlot : freeTimeSlots) {
            DebugPrinter.println("                  - Selected time slot: " + possibleTimeSlot);

            // Does it fit at all?
            if (possibleTimeSlot.getDurationInNs() / Time.NANO_SECOND < getMinimalDurationInMinutes() * 60) {
                continue;
            }

            // Location.
            Vertex previousLocation = getPreviousLocation(agent, possibleTimeSlot.getStartTime());
            Vertex nextLocation = getNextLocation(agent, possibleTimeSlot.getEndTime());
            Time earliestStart = possibleTimeSlot.getStartTime();
            Time latestEnd = possibleTimeSlot.getEndTime();

            // TODO This does not take into account travel times for participants other than the agent itself.
            suitableLocationData = getSuitableLocation(world, agent, previousLocation, earliestStart, nextLocation, latestEnd);

            // Result?
            if (suitableLocationData != null) {
                DebugPrinter.println("                  Found suitable location: " + suitableLocationData.getLocation().getPoint());
                suitableActivity = createActivity(earliestStart, latestEnd, suitableLocationData, getParticipants(agent));
                break;
            }
        }

        // Create three activities: one going towards, one being there, one going away. Then, test for duration again.
        if (suitableLocationData != null && suitableActivity != null && suitableActivity.getParticipants() != null && suitableActivity.getParticipants().size() != 0) {
            DebugPrinter.println("  Activity " + this);
            DebugPrinter.println("  Activity " + getClass().getSimpleName() + " travel details: " + suitableLocationData);

            EnumMap<Index, Activity> activities = new EnumMap<>(Index.class);
            activities.put(Index.ACTIVITY, suitableActivity);

            // Subtract the travel times toward the actual activity. Create travel activity where needed.
            long timeFromPreviousInNs = (long) (Time.NANO_SECOND * suitableLocationData.getTimeFromPreviousInS());
            if (timeFromPreviousInNs > 0) {
                DebugPrinter.println("  - Travel from previous to activity requires " + suitableLocationData.getTimeFromPreviousInS() + "s");

                // Find agents required to accompany or bring the agent.
                Group movementParticipants = getAgentsAccompanying(agent);
                DebugPrinter.println("    Agent " + agent + " requires accompaniment: " + movementParticipants);

                // Maybe only the agent itself is doing the activity, but perhaps the agent must be brought there.
                if (movementParticipants == null || movementParticipants.size() == 1) {
                    movementParticipants = getAgentsBringing(agent);
                    DebugPrinter.println("    Agent " + agent + " requires accompaniment+bringing: " + movementParticipants);
                }

                // Increment the start time and create the movement activity.
                Time laterStartTime = suitableActivity.getStartTime().incrementByMinutes(0); // Clone.
                laterStartTime.increment(timeFromPreviousInNs);

                DebugPrinter.println("    Start of activity moved from " + suitableActivity.getStartTime() + " to " + laterStartTime);
                DebugPrinter.println("    Plan move from " + suitableLocationData.getPreviousLocation().getPoint() + " to " + suitableLocationData.getLocation().getPoint());

                BasicMovementActivity moveTowardsActivity = new BasicMovementActivity(this, // TODO Some activities might require different movement?
                        suitableLocationData.getPreviousLocation(), suitableActivity.getStartTime().incrementByMinutes(0),
                        suitableLocationData.getLocation(), laterStartTime,
                        movementParticipants, true);

                DebugPrinter.println("    -> Moving towards with: " + moveTowardsActivity);

                activities.put(Index.MOVE_TO, moveTowardsActivity);
                suitableActivity.getStartTime().increment(timeFromPreviousInNs);
            }

            // Same for the travel time from the actual activity to the next.
            long timeToNextInNs = (long) (Time.NANO_SECOND * suitableLocationData.getTimeToNextInS());
            if (timeToNextInNs > 0) {
                DebugPrinter.println("  - Travel from activity to next requires " + suitableLocationData.getTimeToNextInS() + "s");

                // Find agents required to accompany or pick up the agent.
                Group movementParticipants = getAgentsAccompanying(agent);
                DebugPrinter.println("    Agent " + agent + " requires accompaniment: " + movementParticipants);

                // Maybe only the agent itself is doing the activity, but perhaps the agent must be picked up there.
                if (movementParticipants == null || movementParticipants.size() == 1) {
                    movementParticipants = getAgentsPickingUp(agent);
                    DebugPrinter.println("    Agent " + agent + " requires accompaniment+pickup: " + movementParticipants);
                }

                // Decrement the end time and create the movement activity.
                Time earlierEndTime = suitableActivity.getEndTime().incrementByMinutes(0); // Clone.
                earlierEndTime.decrement(timeToNextInNs);

                DebugPrinter.println("    End of activity moved from " + suitableActivity.getEndTime() + " to " + earlierEndTime);
                DebugPrinter.println("    Plan move from " + suitableLocationData.getLocation().getPoint() + " to " + suitableLocationData.getNextLocation().getPoint());

                BasicMovementActivity moveFromActivity = new BasicMovementActivity(this, // TODO Some activities might require different movement?
                        suitableLocationData.getLocation(), earlierEndTime,
                        suitableLocationData.getNextLocation(), suitableActivity.getEndTime().incrementByMinutes(0),
                        movementParticipants, false);

                DebugPrinter.println("    -> Move away with: " + moveFromActivity);

                activities.put(Index.MOVE_FROM, moveFromActivity);
                suitableActivity.getEndTime().decrement(timeToNextInNs);
            }

            // Test for duration. This should not happen.
            int durationInMinutes = suitableActivity.getStartTime().howMuchLaterInMinutesIs(suitableActivity.getEndTime());
            if (durationInMinutes < getMinimalDurationInMinutes()) {
                DebugPrinter.println("     Activity became too short!");
                return null;
            }

            // Return the resulting activities.
            return activities;
        }

        // Return that this failed.
        return null;
    }

    /**
     * Get the location the agent is at before the time given.
     */
    protected Vertex getPreviousLocation(Agent agent, Time start) {
        for (int i = agent.getAgenda().size() - 1; i >= 0; i--) {
            Activity activity = agent.getAgenda().get(i);
            if (start.compareTo(activity.getEndTime()) >= 0) {
                return activity.getEndLocation();
            }
        }
        return (Vertex) agent.getHouse();
    }

    /**
     * Get the location the agent is at after the time given.
     */
    protected Vertex getNextLocation(Agent agent, Time end) {
        DebugPrinter.println("                                              Next location of agent after time " + end);
        for (int i = 0; i < agent.getAgenda().size(); ++i) {
            Activity activity = agent.getAgenda().get(i);
            DebugPrinter.println("                                              -> Investigating activity " + activity + ". It starts at " + activity.getStartTime());
            if (end.compareTo(activity.getStartTime()) <= 0) {
                DebugPrinter.println("                                              -> Found: activity " + activity + " starts at " + activity.getStartLocation().getPoint());
                return activity.getStartLocation();
            }
        }
        DebugPrinter.println("                                              -> No later activity, agent must go home");
        return (Vertex) agent.getHouse();
    }

    /**
     * Get a suitable location for the activity.
     * This is currently either a location with a possible location function, e.g. a
     * random workplace, or the agent's house.
     */
    public LocationData getSuitableLocation(World world, Agent agent,
                                            Vertex locationPreviousActivity, Time endTimePreviousActivity,
                                            Vertex locationNextActivity, Time startTimeNextActivity) {
        if (possibleLocationFunctions != null && possibleLocationFunctions.length != 0) {
            return getSuitableLocation(world, agent, locationPreviousActivity, endTimePreviousActivity, locationNextActivity, startTimeNextActivity, possibleLocationFunctions);
        } else {
            return getSuitableLocation(world, agent, locationPreviousActivity, endTimePreviousActivity, locationNextActivity, startTimeNextActivity, getHouse(agent));
        }
    }

    private Vertex getHouse(Agent agent) {
        return (Vertex) agent.getHouse();
    }

    private LocationData getSuitableLocation(World world, Agent agent,
                                             Vertex locationPreviousActivity, Time endTimePreviousActivity,
                                             Vertex locationNextActivity, Time startTimeNextActivity,
                                             Class<? extends LocationFunction>[] possibleLocationFunctions) {

        // Early stop for failure if we don't have enough time in the first place.
        final int timeWindowInMinutes = endTimePreviousActivity.howMuchLaterInMinutesIs(startTimeNextActivity);
        if (timeWindowInMinutes < getMinimalDurationInMinutes()) {
            DebugPrinter.println("                  - The time window is too short, we have " + timeWindowInMinutes + " min and require " + getMinimalDurationInMinutes() + "min");
            return null;
        }

        // Iterate through locations.
        LocationData suitableLocation = null;
        double totalWalkingTimeS = 0;
        ClosestFirstIterator iterator = world.getClosestFirstIterator(locationPreviousActivity);

        // The last condition is a terminator.
        while (iterator.hasNext() && suitableLocation == null && totalWalkingTimeS < Constants.MAX_WALKING_TIME_S && timeWindowInMinutes - Math.ceil(totalWalkingTimeS / 60) >= getMinimalDurationInMinutes()) {

            // Determine whether the location is suitable.
            Vertex candidateLocation = iterator.next();
            Area candidateArea = candidateLocation.getArea();

            // Test for function and proximity.
            if (candidateArea != null && candidateArea.hasAnyFunction(possibleLocationFunctions)) {
                DebugPrinter.println("                  - Found a vertex @ " + candidateLocation.getPoint() + " (coming from " + locationPreviousActivity.getPoint() + "; going to " + locationNextActivity.getPoint() + ")");

                // Determine walking times.
                double timeFromPreviousInS = durationOfCurrentPathInS(agent, iterator);
                double timeToNextInS = 0;
                DebugPrinter.println("                  - Walking time from previous activity to vertex: " + timeFromPreviousInS + "s");

                if (locationPreviousActivity.equals(locationNextActivity)) {
                    timeToNextInS = timeFromPreviousInS;
                } else {
                    timeToNextInS = world.getPathLengthInM(candidateLocation.getPoint(), locationNextActivity.getPoint()) / agent.getNormalSpeedMs();
                }
                totalWalkingTimeS = timeFromPreviousInS + timeToNextInS;
                DebugPrinter.println("                  - Require " + getMinimalDurationInMinutes() + "min; time window is " + timeWindowInMinutes + "min; walking time is ~" + (int) (totalWalkingTimeS / 60.0) + "min");

                // Can we walk and respect the minimal duration?
                if (timeWindowInMinutes - Math.ceil(totalWalkingTimeS / 60.0) > getMinimalDurationInMinutes()) {
                    DebugPrinter.println("                  - Enough time!");
                    suitableLocation = new LocationData(candidateLocation, world, locationPreviousActivity, timeFromPreviousInS, locationNextActivity, timeToNextInS);
                } else {
                    DebugPrinter.println("                  - NOT enough time! :(");
                }
            }

            // Test for capacity.
            if (suitableLocation != null) {
                DebugPrinter.println("                  - Test for capacity");
                int numParticipants = -1;
                boolean suitable = possibleLocationFunctions == null || possibleLocationFunctions.length == 0; // Without location function, who cares.
                if (!suitable) {
                    numParticipants = mergeGroups(getParticipants(agent), getAgentsAccompanying(agent)).size();
                    for (Class<? extends LocationFunction> possibleLocationFunction : possibleLocationFunctions) {
                        DebugPrinter.println("                  - -> Possible location function: " + possibleLocationFunction.getSimpleName() + ".");
                        LocationFunction function = candidateArea.getFunction(possibleLocationFunction);
                        DebugPrinter.println("                  - -> Location functions at area: " + candidateArea + "; looking at: " + function + ".");
                        if (function != null) {
                            DebugPrinter.println("                  - -> Location function: " + function);
                            if (function.canFit(numParticipants)) {
                                DebugPrinter.println("                  - Location function " + function + " can fit " + numParticipants + "; current occupancy is " + function.getOccupancy() + " and capacity is " + function.getCapacity() + ".");
                                function.increaseOccupancy(numParticipants);
                                suitable = true;
                                break;
                            } else
                                DebugPrinter.println("                  - Location function " + function + " cannot fit " + numParticipants + "; current occupancy is " + function.getOccupancy() + " and capacity is " + function.getCapacity() + ".");
                        }
                    }
                } else
                    DebugPrinter.println("                  - Location function for " + this + " is unspecified, so can happen anywhere.");
                if (!suitable) {
                    DebugPrinter.println("                  - No location function of this area can fit " + numParticipants + " people.");
                    suitableLocation = null;
                }
            }
        }

        if (suitableLocation == null) {
            DebugPrinter.println("                  No location function of any nearby area fits. Need " + getMinimalDurationInMinutes() + "min for activity; time slot is " + timeWindowInMinutes + "min; looked for locations up to a walking time of " + totalWalkingTimeS / 60 + "min");
        }

        // And return.
        return suitableLocation;
    }

    private LocationData getSuitableLocation(World world, Agent agent,
                                             Vertex locationPreviousActivity, Time endTimePreviousActivity,
                                             Vertex locationNextActivity, Time startTimeNextActivity,
                                             Vertex candidateLocation) {
        // TODO Check candidate location for capacity?
        Path pathFromPrevious = world.getPath(locationPreviousActivity.getPoint(), candidateLocation.getPoint());
        double timeFromPreviousInS = pathFromPrevious.lengthInM() / agent.getNormalSpeedMs();
        Path pathToNext = world.getPath(candidateLocation.getPoint(), locationNextActivity.getPoint());
        double timeToNextInS = pathToNext.lengthInM() / agent.getNormalSpeedMs();
        int timeWindowInMinutes = endTimePreviousActivity.howMuchLaterInMinutesIs(startTimeNextActivity);
        double totalWalkingTimeS = timeFromPreviousInS + timeToNextInS;
        if (timeWindowInMinutes - Math.ceil(totalWalkingTimeS / 60.0) > getMinimalDurationInMinutes()) {
            DebugPrinter.println("                        Activity planned at home: " + getName() + " for " + agent + ".");
            return new LocationData(candidateLocation, world, locationPreviousActivity, timeFromPreviousInS, locationNextActivity, timeToNextInS);
        } else {
            DebugPrinter.println("                        Too little time for activity to be planned at home: " + getName() + " for " + agent + ".");
            return null;
        }
    }

    private double durationOfCurrentPathInS(Agent agent, ClosestFirstIterator iterator) {
        return iterator.getCurrentPathLengthInM() / agent.getNormalSpeedMs();
    }

    public PossibleTimeIntervals getActivityTimeSlots() {
        return possibleTimeIntervals;
    }

    public int getMinimalDurationInMinutes() {
        return possibleTimeIntervals.getMinimalDurationInMinutes();
    }

    public int getMaximalDurationInMinutes() {
        return possibleTimeIntervals.getMaximalDurationInMinutes();
    }


    @Override
    public String toString() {
        return getClass().getSimpleName();
    }

    @Override
    public int compareTo(PossibleActivity o) {
        return toString().compareTo(o.toString());
    }
}
