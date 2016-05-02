package nl.tno.idsa.library.models;

import nl.tno.idsa.framework.agents.Agent;
import nl.tno.idsa.framework.behavior.models.SynchronousSingleStepModel;
import nl.tno.idsa.framework.semantics_impl.groups.Group;
import nl.tno.idsa.framework.semantics_impl.locations.LocationAndTime;
import nl.tno.idsa.framework.world.Path;
import nl.tno.idsa.framework.world.Point;
import nl.tno.idsa.framework.world.Vertex;
import nl.tno.idsa.library.locations.Inside;
import nl.tno.idsa.library.locations.Public;

/**
 * Created by jongsd on 15-10-15.
 */
public class ModelGoInside extends SynchronousSingleStepModel {

    // TODO Does this work, and is it used?

    private boolean goHome = false;

    public void setGoHome(boolean goHome) {
        this.goHome = goHome;
    }

    @Override
    @SuppressWarnings("unchecked")
    protected boolean doSingleStep() {
        for (Agent a : getActors()) {
            Point destination;
            if (goHome) {
                destination = a.getHouse().getPoint();
            } else {
                Vertex closestVertex = getEnvironment().getWorld().getClosestVertex(a.getLocation(), false, Inside.class, Public.class); // Find public buildings.
                destination = closestVertex.getPoint();
            }
            Path path = getEnvironment().getWorld().getPath(a.getLocation(), destination);
            BasicMovementModel movementModel = new BasicMovementModel(BasicMovementModel.WALKING_SPEED_MS);
            movementModel.setEnvironment(getEnvironment());
            movementModel.setActors(new Group(a));
            movementModel.setLocationAndEndTime(new LocationAndTime(path.getPolyLine(), LocationAndTime.UNDEFINED_TIME));
            a.pushModel(movementModel);
        }
        return true;
    }
}
