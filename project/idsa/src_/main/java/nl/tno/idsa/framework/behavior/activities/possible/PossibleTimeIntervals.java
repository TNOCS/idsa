package nl.tno.idsa.framework.behavior.activities.possible;

import nl.tno.idsa.framework.behavior.activities.concrete.TimeInterval;
import nl.tno.idsa.framework.utils.RandomNumber;
import nl.tno.idsa.framework.world.Time;

import java.util.ArrayList;
import java.util.List;

/**
 * Store time intervals and minimum/maximum allowed durations.
 */
public class PossibleTimeIntervals {

    private final List<TimeInterval> possibleTimeIntervals;
    private final int minimalDurationInMinutes;
    private final int maximalDurationInMinutes;

    public PossibleTimeIntervals(Time startTime, Time endTime, int minimalDurationInMinutes, int maximalDurationInMinutes) {
        this(new TimeInterval(startTime, endTime), minimalDurationInMinutes, maximalDurationInMinutes);
    }

    public PossibleTimeIntervals(TimeInterval singleTimeInterval, int minimalDurationInMinutes, int maximalDurationInMinutes) {
        this(asList(singleTimeInterval), minimalDurationInMinutes, maximalDurationInMinutes);
    }

    public PossibleTimeIntervals(List<TimeInterval> possibleTimeIntervals, int minimalDurationInMinutes, int maximalDurationInMinutes) {
        this.possibleTimeIntervals = possibleTimeIntervals;
        this.minimalDurationInMinutes = minimalDurationInMinutes;
        this.maximalDurationInMinutes = maximalDurationInMinutes;
    }

    private static <T> List<T> asList(T element) {
        ArrayList<T> list = new ArrayList<>();
        list.add(element);
        return list;
    }

    public List<TimeInterval> getPossibleTimeIntervals() {
        return possibleTimeIntervals;
    }

    public TimeInterval samplePossibleTimeIntervalIn(Time startTime, Time endTime) {
        List<TimeInterval> validTimeIntervals = getAllPossibleTimeIntervalsIn(startTime, endTime);
        if (validTimeIntervals.size() == 0) {
            return null;
        } else {
            final int index = RandomNumber.nextInt(validTimeIntervals.size());
            return validTimeIntervals.get(index);
        }
    }

    public List<TimeInterval> getAllPossibleTimeIntervalsIn(Time startTime, Time endTime) {
        ArrayList<TimeInterval> validTimeIntervals = new ArrayList<>();
        for (TimeInterval possibleTimeInterval : possibleTimeIntervals) {
            final TimeInterval candidateTimeInterval = possibleTimeInterval.deepCopy();
            candidateTimeInterval.startNoEarlierThan(startTime);
            candidateTimeInterval.endNoLaterThan(endTime);
            if (isShortEnough(candidateTimeInterval) && isLongEnough(candidateTimeInterval)) {
                validTimeIntervals.add(candidateTimeInterval);
            }
        }
        return validTimeIntervals;
    }

    public TimeInterval getPossibleTimeIntervalContaining(Time startTime, Time endTime) {
        TimeInterval theInterval = null;
        for (TimeInterval interval : getPossibleTimeIntervals()) {
            if (interval.contains(startTime, endTime)) {
                theInterval = interval;
                break;
            }
        }
        return theInterval;
    }

    private boolean isShortEnough(TimeInterval candidateTimeInterval) {
        return candidateTimeInterval.getDurationInNs() <= maximalDurationInMinutes * Time.NANO_SECOND * 60;
    }

    private boolean isLongEnough(TimeInterval candidateTimeInterval) {
        return candidateTimeInterval.getDurationInNs() >= minimalDurationInMinutes * Time.NANO_SECOND * 60;
    }

    public int getMinimalDurationInMinutes() {
        return minimalDurationInMinutes;
    }

    public int getMaximalDurationInMinutes() {
        return maximalDurationInMinutes;
    }

    @Override
    public String toString() {
        return "PossibleTimeIntervals{" +
                "possibleTimeIntervals=" + possibleTimeIntervals +
                ", minimalDurationInMinutes=" + minimalDurationInMinutes +
                ", maximalDurationInMinutes=" + maximalDurationInMinutes +
                '}';
    }
}
