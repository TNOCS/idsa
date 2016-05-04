package nl.tno.idsa.framework.semantics_impl.locations;

import nl.tno.idsa.framework.world.IGeometry;
import nl.tno.idsa.framework.world.Time;

// TODO Document class.

public class LocationAndTime {

    private IGeometry location;

    public static long UNDEFINED_TIME = -1;
    private long timeNanos;

    public LocationAndTime(IGeometry location) {
        this(location, UNDEFINED_TIME);
    }

    public LocationAndTime(IGeometry location, long timeNanos) {
        this.location = location;
        this.timeNanos = timeNanos;
    }

    public IGeometry getLocation() {
        return location;
    }

    public void setLocation(IGeometry location) {
        this.location = location;
    }

    public boolean isTimeDefined() {
        return timeNanos != UNDEFINED_TIME;
    }

    public long getTimeNanos() {
        return timeNanos;
    }

    public void setTimeNanos(long timeNanos) {
        this.timeNanos = timeNanos;
    }

    @Override
    public String toString() {
        if (timeNanos != UNDEFINED_TIME) {
            return String.format("@%s at %s", location, new Time(timeNanos));
        } else {
            return String.format("@%s", location);
        }

    }
}
