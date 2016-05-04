package nl.tno.idsa.library.models;

import nl.tno.idsa.framework.agents.Agent;
import nl.tno.idsa.framework.behavior.models.SynchronousSingleStepModel;
import nl.tno.idsa.framework.messaging.Messenger;
import nl.tno.idsa.framework.semantics_impl.groups.Group;
import nl.tno.idsa.framework.semantics_impl.locations.LocationAndTime;
import nl.tno.idsa.framework.world.Point;
import nl.tno.idsa.framework.world.Vertex;
import nl.tno.idsa.library.locations.PoliceSpawnPoint;

/**
 * Created by jongsd on 26-8-15.
 */
@SuppressWarnings("unused")
public class ModelArrest extends SynchronousSingleStepModel {

    @Override
    public boolean doSingleStep() {

        // Make sure the police officers are removed after the incident is done (pushed below any move models that come after).
        ModelDisappear modelDisappear = new ModelDisappear();
        modelDisappear.setEnvironment(getEnvironment());
        modelDisappear.setActors(getActors());
        for (Agent agent : getActors()) {
            agent.pushModel(modelDisappear);            // TODO Does this work?
        }

        // Find a police station to deliver the offenders.
        Point origin = getLocationAndEndTime().getLocation().getCenterPoint();
        Class<PoliceSpawnPoint> desiredFunction = PoliceSpawnPoint.class;
        Vertex minVertex = getEnvironment().getWorld().getClosestVertex(origin, desiredFunction);

        // Create a move towards the police station.
        if (minVertex != null) {

            Group actors = new Group();
            actors.addAll(getActors());
            actors.addAll(getTargets());

            Messenger.broadcast(String.format("%s is/are arresting %s at %s and the whole merry gang is going to the police station at %s.", getActors(), getTargets(), getLocationAndEndTime(), minVertex.getPoint()));
            BasicMovementModel basicMovementModel = new BasicMovementModel(BasicMovementModel.WALKING_SPEED_MS);
            basicMovementModel.setActors(actors);
            basicMovementModel.setLocationAndEndTime(new LocationAndTime(minVertex.getPoint()));
            basicMovementModel.setEnvironment(getEnvironment());
            for (Agent agent : actors) {
                agent.pushModel(basicMovementModel);
            }
        }

        // Or leave the offenders on the street and let the police officer(s) disappear mysteriously ;-) (This cannot really happen.)   TODO But does it?
        else {
            Messenger.broadcast(String.format("%s is/are arresting %s at %s and they are going to stay there.", getActors(), getTargets(), getLocationAndEndTime()));
        }

        return true;
    }
}
