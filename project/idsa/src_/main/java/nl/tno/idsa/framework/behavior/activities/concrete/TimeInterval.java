package nl.tno.idsa.framework.behavior.activities.concrete;

import nl.tno.idsa.framework.world.Time;

import java.util.ArrayList;
import java.util.List;

/**
 * A time interval.
 */
public class TimeInterval {
    private Time startTime;
    private Time endTime;

    public TimeInterval(Time startTime, Time endTime) {
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public Time getStartTime() {
        return startTime;
    }

    public void setStartTime(Time startTime) {
        this.startTime = startTime;
    }

    public Time getEndTime() {
        return endTime;
    }

    public void setEndTime(Time endTime) {
        this.endTime = endTime;
    }

    public long getDurationInNs() {
        return getEndTime().getNanos() - getStartTime().getNanos();
    }

    public TimeInterval intersect(TimeInterval other) {
        long startN = Math.max(startTime.getNanos(), other.startTime.getNanos());
        long endN = Math.min(endTime.getNanos(), other.endTime.getNanos());
        TimeInterval intersection = new TimeInterval(new Time(startN), new Time(endN));
        if (intersection.startTime.getNanos() >= intersection.endTime.getNanos()) {
            return null; // Invalid interval.
        }
        return intersection;
    }

    public void startNoEarlierThan(Time possibleLaterStart) {
        final long max = Math.max(startTime.getNanos(), possibleLaterStart.getNanos());
        if (max != startTime.getNanos()) {
            startTime = new Time(max);
        }
    }

    public void startNoLaterThan(Time possibleEarlierStart) {
        final long min = Math.min(startTime.getNanos(), possibleEarlierStart.getNanos());
        if (min != startTime.getNanos()) {
            startTime = new Time(min);
        }
    }

    public void endNoEarlierThan(Time possibleLaterEnd) {
        final long max = Math.max(endTime.getNanos(), possibleLaterEnd.getNanos());
        if (max != endTime.getNanos()) {
            endTime = new Time(max);
        }
    }

    public void endNoLaterThan(Time possibleEarlierEnd) {
        final long min = Math.min(endTime.getNanos(), possibleEarlierEnd.getNanos());
        if (min != endTime.getNanos()) {
            endTime = new Time(min);
        }
    }

    public boolean contains(TimeInterval other) {
        return contains(other.startTime, other.endTime);
    }

    public boolean contains(Time start, Time end) {
        return this.startTime.compareTo(start) <= 0 && this.endTime.compareTo(end) >= 0;
    }

    public TimeInterval deepCopy() {
        return new TimeInterval(new Time(startTime.getNanos()), new Time(endTime.getNanos()));
    }

    @Override
    public String toString() {
        return "[" + startTime + "->" + endTime + ']';
    }

    public static List<TimeInterval> intersect(List<TimeInterval> ti1, List<TimeInterval> ti2) {
        List<TimeInterval> result = new ArrayList<>();
        for (int j = 0; j < ti1.size(); ++j) {
            for (int i = 0; i < ti2.size(); ++i) {
                TimeInterval t = ti1.get(j).intersect(ti2.get(i));
                if (t != null) {
                    result.add(t);
                }
            }
        }
        return result;
    }
}
