package nl.tno.idsa.library.triggers;

import nl.tno.idsa.framework.behavior.models.Model;
import nl.tno.idsa.framework.behavior.triggers.MovingAreaTrigger;
import nl.tno.idsa.framework.semantics_impl.locations.LocationAndTime;
import nl.tno.idsa.framework.world.Point;
import nl.tno.idsa.framework.world.Polygon;
import nl.tno.idsa.library.models.BasicDeterringModel;

/**
 * Created by jongsd on 17-9-15.
 */
// TODO Document the class.
// TODO Check whether working and well implemented. Same for all triggers.
public class MovingDeterringTrigger extends MovingAreaTrigger {

    private BasicDeterringModel model;

    public MovingDeterringTrigger(double sizeOfDeterringZoneInM, double probability) {
        super(createShape(sizeOfDeterringZoneInM), probability);
    }

    private static Polygon createShape(double sizeOfDeterringZone) {
        Point[] points = new Point[]{
                new Point(sizeOfDeterringZone, sizeOfDeterringZone),
                new Point(-sizeOfDeterringZone, sizeOfDeterringZone),
                new Point(-sizeOfDeterringZone, -sizeOfDeterringZone),
                new Point(sizeOfDeterringZone, -sizeOfDeterringZone)
        };
        return new Polygon(points);
    }

    @Override
    public Model getModel() {
        if (model == null) {
            model = new BasicDeterringModel();
            model.setLocationAndEndTime(new LocationAndTime(getLocation()));
        }
        model.getLocationAndEndTime().setLocation(getLocation());
        return model;
    }
}
