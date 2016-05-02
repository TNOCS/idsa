package nl.tno.idsa.framework.behavior.activities.possible;

import nl.tno.idsa.framework.behavior.activities.concrete.Activity;
import nl.tno.idsa.framework.behavior.activities.concrete.BasicStationaryActivity;
import nl.tno.idsa.framework.behavior.activities.concrete.LocationData;
import nl.tno.idsa.framework.semantics_impl.groups.Group;
import nl.tno.idsa.framework.semantics_impl.locations.LocationFunction;
import nl.tno.idsa.framework.utils.RandomNumber;
import nl.tno.idsa.framework.world.Time;
import nl.tno.idsa.framework.world.Vertex;

/**
 * A basic possible stationary activity, which creates a basic stationary activity once it is planned.
 */
public abstract class PossibleStationaryActivity extends PossibleActivity {

    /**
     * Determines whether the activity will fill the entire time slot, or a random part of it.
     */
    protected static enum Fill {
        EntireTimeSlot, RandomPartOfTimeSlot
    }

    private final Fill fillEntireTimeSlot;

    /**
     *
     */
    /**
     * @param possibleTimeIntervals     When can the activity take place?
     * @param fillEntireTimeSlot        Setting for filling the entire time slot, or a random part of it.
     * @param possibleLocationFunctions Location functions that are suitable. If we omit the location functions, the location is assumed to be the agent's house (once we know the agent).
     */
    @SafeVarargs
    protected PossibleStationaryActivity(PossibleTimeIntervals possibleTimeIntervals, Fill fillEntireTimeSlot, Class<? extends LocationFunction>... possibleLocationFunctions) {
        super(possibleTimeIntervals, possibleLocationFunctions);
        this.fillEntireTimeSlot = fillEntireTimeSlot;
    }

    /**
     * Specialisations of this class may want to override this method to create another activity than a basic
     * stationary activity (which basically makes the agent wait and do nothing).
     */
    protected BasicStationaryActivity createStationaryActivity(Time start, Time end, Vertex location, Group group) {
        return new BasicStationaryActivity(this, start, end, location, group, BasicStationaryActivity.Wander.OUTSIDE_ONLY);
    }

    @Override
    protected Activity createActivity(Time startOfFreeInterval, Time endOfFreeInterval, LocationData locationData, Group participants) {
        if (participants == null || participants.size() == 0) {
            return null;
        }
        if (fillEntireTimeSlot == Fill.EntireTimeSlot) {
            return createStationaryActivity(startOfFreeInterval, endOfFreeInterval, locationData.getLocation(), participants);
        } else {
            double totalTravelTimeS = locationData.getTimeFromPreviousInS() + locationData.getTimeToNextInS();
            double minDurationInS = totalTravelTimeS + (getMinimalDurationInMinutes() * 60);
            return createStationaryActivity(startOfFreeInterval, endOfFreeInterval, minDurationInS, locationData.getLocation(), participants);
        }
    }

    private Activity createStationaryActivity(Time start, Time end, double minDurationIncludingTravelInS, Vertex location, Group group) {

        // No activity for no people.
        if (group == null || group.size() == 0) {
            return null;
        }

        // Max length.
        int lengthOfIntervalInMinutes = start.howMuchLaterInMinutesIs(end);
        if (lengthOfIntervalInMinutes < getMinimalDurationInMinutes()) {
            return null;
        }
        int maxLengthOfIntervalInMinutes = Math.min(getMaximalDurationInMinutes(), lengthOfIntervalInMinutes);

        // Min length.
        int minLengthOfIntervalInMinutes = (int) Math.ceil(minDurationIncludingTravelInS / 60);

        // Randomly do something.
        int durationInMinutes = RandomNumber.nextBoundedInt(minLengthOfIntervalInMinutes, maxLengthOfIntervalInMinutes);
        int slackMinutes = lengthOfIntervalInMinutes - durationInMinutes;
        int startTimeInMinutes = RandomNumber.nextBoundedInt(0, slackMinutes);
        start = start.incrementByMinutes(startTimeInMinutes);
        end = start.incrementByMinutes(durationInMinutes);
        BasicStationaryActivity basicStationaryActivity = createStationaryActivity(start, end, location, group);

        if (start.howMuchLaterInMinutesIs(end) <= minDurationIncludingTravelInS / 60) {
            return null; // This is possible when an activity is planned close to the end of the day (5am).
        }

        return basicStationaryActivity;
    }
}
