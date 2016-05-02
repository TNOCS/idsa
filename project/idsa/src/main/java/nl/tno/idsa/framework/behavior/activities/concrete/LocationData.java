package nl.tno.idsa.framework.behavior.activities.concrete;

import nl.tno.idsa.framework.world.Path;
import nl.tno.idsa.framework.world.Vertex;
import nl.tno.idsa.framework.world.World;

/**
 * Structure for keeping together data on locations, e.g. paths and distances.
 */
public class LocationData {
    private final Vertex previousLocation;
    private final Path pathFromPrevious;
    private final double timeFromPreviousInS;
    private final Vertex location;
    private final Vertex nextLocation;
    private final Path pathToNext;
    private final double timeToNextInS;

    public LocationData(Vertex location, World world, Vertex locationPreviousActivity, double timeFromPreviousInS, Vertex locationNextActivity, double timeToNextInS) {

        this.location = location;

        this.previousLocation = locationPreviousActivity;
        this.pathFromPrevious = world.getPath(locationPreviousActivity, location);
        this.timeFromPreviousInS = timeFromPreviousInS;

        this.nextLocation = locationNextActivity;
        this.pathToNext = (locationPreviousActivity.equals(locationNextActivity)) ? pathFromPrevious.reverse() : world.getPath(location, locationNextActivity);
        this.timeToNextInS = timeToNextInS;
    }

    public Vertex getLocation() {
        return location;
    }

    public Vertex getPreviousLocation() {
        return previousLocation;
    }

    public Path getPathFromPrevious() {
        return pathFromPrevious;
    }

    public double getTimeFromPreviousInS() {
        return timeFromPreviousInS;
    }

    public Vertex getNextLocation() {
        return nextLocation;
    }

    public Path getPathToNext() {
        return pathToNext;
    }

    public double getTimeToNextInS() {
        return timeToNextInS;
    }

    @Override
    public String toString() {
        return "LocationData{" +
                "previousLocation=" + (previousLocation != null ? previousLocation.getPoint() : "?") +
                ", timeFromPreviousInS=" + timeFromPreviousInS +
                ", location=" + location.getPoint() +
                ", nextLocation=" + (nextLocation != null ? nextLocation.getPoint() : "?") +
                ", timeToNextInS=" + timeToNextInS +
                '}';
    }
}
