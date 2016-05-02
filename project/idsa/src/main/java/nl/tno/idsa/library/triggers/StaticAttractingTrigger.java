package nl.tno.idsa.library.triggers;

import nl.tno.idsa.framework.behavior.models.Model;
import nl.tno.idsa.framework.behavior.triggers.StaticAreaTrigger;
import nl.tno.idsa.framework.semantics_impl.locations.LocationAndTime;
import nl.tno.idsa.framework.world.Point;
import nl.tno.idsa.framework.world.PolyLine;
import nl.tno.idsa.library.models.BasicAttractingModel;

/**
 * Created by jongsd on 17-9-15.
 */
public class StaticAttractingTrigger extends StaticAreaTrigger {

    private BasicAttractingModel model;

    public StaticAttractingTrigger(PolyLine path, double effectiveDistanceFromShape, double probability) {
        super(path, effectiveDistanceFromShape, probability);
    }

    public StaticAttractingTrigger(Point centerPoint, double radius, double probability) {
        super(centerPoint, radius, probability);
    }

    @Override
    public Model getModel() {
        // Create model lazily.
        if (model == null) {
            model = new BasicAttractingModel();
            model.setLocationAndEndTime(new LocationAndTime(getShape(), LocationAndTime.UNDEFINED_TIME));
        }
        // Update the shape.
        model.getLocationAndEndTime().setLocation(getShape());
        return model;
    }
}
