package nl.tno.idsa.library.triggers;

import nl.tno.idsa.framework.behavior.models.Model;
import nl.tno.idsa.framework.behavior.triggers.StaticAreaTrigger;
import nl.tno.idsa.framework.semantics_impl.locations.LocationAndTime;
import nl.tno.idsa.framework.world.Point;
import nl.tno.idsa.library.models.ModelGoInside;

/**
 * Created by jongsd on 15-10-15.
 */
public class PrecipitationTrigger extends StaticAreaTrigger {

    private ModelGoInside model;

    public PrecipitationTrigger(Point centerPoint, double radius) {
        super(centerPoint, radius, 0.05); // TODO Hardcoded.
    }

    @Override
    public Model getModel() {
        if (model == null) {
            model = new ModelGoInside();
            model.setGoHome(false); // Just go inside anywhere
            model.setLocationAndEndTime(new LocationAndTime(getShape(), LocationAndTime.UNDEFINED_TIME));
        }
        // model.getLocationAndEndTime().setLocation(getLocation());
        return model;
    }
}
