package nl.tno.idsa.framework.world;

import java.io.Serializable;

// TODO Document class.
public class Time implements Comparable, Serializable {

    public static final long NANO_SECOND = 1_000_000_000;
    public static final Time MIN_TIME = new Time(5, 0, 1);
    public static final Time MAX_TIME = new Time(4, 59, 59);

    private long nanosSince5Am = 0;

    public Time() {

    }

    public Time(long timeNanos) {
        nanosSince5Am = timeNanos;
    }

    public Time(int hours, int minutes, int seconds) {
        nanosSince5Am = NANO_SECOND * ((hours - 5) * 3600 + minutes * 60 + seconds);
        while (nanosSince5Am < 0) {
            nanosSince5Am += 3600 * 24 * NANO_SECOND;
        }
    }

    public Time(int hours, int minutes) {
        nanosSince5Am = NANO_SECOND * ((hours - 5) * 3600 + minutes * 60);
        while (nanosSince5Am < 0) {
            nanosSince5Am += 3600 * 24 * NANO_SECOND;
        }
    }

    public Time(int hours) {
        nanosSince5Am = NANO_SECOND * ((hours - 5) * 3600);
        while (nanosSince5Am < 0) {
            nanosSince5Am += 3600 * 24 * NANO_SECOND;
        }
    }

    public Time getCopyWithDifference(int hours, int minutes, int seconds) {
        Time laterTime = new Time(nanosSince5Am);
        laterTime.increment(NANO_SECOND * (3600 * hours + 60 * minutes + seconds));
        return laterTime;
    }

    public long getNanos() {
        return nanosSince5Am;
    }

    public void setNanos(long nanosSince5Am) {
        this.nanosSince5Am = nanosSince5Am;
    }

    public int getHour() {
        return (int) ((5 + (nanosSince5Am / (NANO_SECOND * 60 * 60))) % 24);
    }

    public int getMinute() {
        return (int) ((nanosSince5Am / (NANO_SECOND * 60)) % 60);
    }

    public int getSecond() {
        return (int) (nanosSince5Am / NANO_SECOND) % 60;
    }

    public int howMuchLaterInMinutesIs(Time other) {
        return Math.abs(60 * (other.getHour() - getHour()) + other.getMinute() - getMinute());
    }

    public void increment(long nanos) {
        nanosSince5Am += nanos;
        if (3600 * 24 < nanosSince5Am / NANO_SECOND) {
            throw new Error("Time of day advanced after 5am. We don't have support for multiple days yet."); // TODO More graceful.
        }
    }

    public void decrement(long nanos) {
        nanosSince5Am -= nanos;
        if (3600 * 24 < nanosSince5Am / NANO_SECOND) {
            throw new Error("Time of day got before 5am. We don't have support for multiple days yet.");   // TODO More graceful.
        }
    }

    public Time incrementByMinutes(int minutes) {
        long m = (long) minutes;
        Time time = new Time(nanosSince5Am);
        try {
            time.increment(m * 60 * NANO_SECOND);
        } catch (Error e) {
            return Time.MAX_TIME;
        }
        return time;
    }

    public Time decrementByMinutes(int minutes) {
        long m = (long) minutes;
        Time time = new Time(nanosSince5Am);
        try {
            time.decrement(m * 60 * NANO_SECOND);
        } catch (Error e) {
            return Time.MIN_TIME;
        }
        return time;
    }

    public static Time max(Time t1, Time t2) {
        if (t1.compareTo(t2) > 0) {
            return t1;
        }
        return t2;
    }

    public static Time min(Time t1, Time t2) {
        if (t1.compareTo(t2) < 0) {
            return t1;
        }
        return t2;
    }

    @Override
    public int compareTo(Object o) {
        Time other = (Time) o;
        if (other.nanosSince5Am == nanosSince5Am) {
            return 0;
        } else if (other.nanosSince5Am > nanosSince5Am) {
            return -1;
        }
        return 1;
    }

    public Time earliest(Time otherTime) {
        return compareTo(otherTime) <= 0 ? this : otherTime;
    }

    public Time latest(Time otherTime) {
        return compareTo(otherTime) <= 0 ? otherTime : this;
    }

    public static String durationToString(long timeNanos) {
        long hour = (long) (1.0 * timeNanos / (NANO_SECOND * 60 * 60)) % 24;
        long minute = (long) (1.0 * timeNanos / (NANO_SECOND * 60)) % 60;
        long second = (timeNanos / NANO_SECOND) % 60;
        return hour + ":" + minute + ":" + second;  // TODO Nicely format 19:01:04.
    }

    @Override
    public String toString() {
        return ff(getHour()) + ":" + ff(getMinute()) + ":" + ff(getSecond());
    }

    private String ff(int in) {
        String ret = "" + in;
        while (ret.length() < 2) {
            ret = "0" + ret;
        }
        return ret;
    }
}
