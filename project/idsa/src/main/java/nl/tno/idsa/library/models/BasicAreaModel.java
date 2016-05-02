package nl.tno.idsa.library.models;

import nl.tno.idsa.framework.agents.Agent;
import nl.tno.idsa.framework.behavior.models.Model;
import nl.tno.idsa.framework.semantics_impl.groups.Group;
import nl.tno.idsa.framework.semantics_impl.locations.LocationAndTime;
import nl.tno.idsa.framework.utils.GeometryUtils;
import nl.tno.idsa.framework.utils.RandomNumber;
import nl.tno.idsa.framework.world.*;
import nl.tno.idsa.library.locations.Outside;

import java.awt.geom.Rectangle2D;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

// TODO Document all models.

public class BasicAreaModel extends Model {

    private static final int MAX_RANDOM_SAMPLES = 100;

    private boolean movementNeeded = true;
    private Map<Agent, BasicMovementModel> movementModels;

    private Polygon polygonToMoveIn;
    private Rectangle2D boundingBoxOfPolygonToMoveIn;

    private HashSet<Agent> interrupted = new HashSet<>();

    @Override
    public boolean agentsCanWaitBeforeStarting() {
        return true;
    }

    @Override
    protected boolean doWaitingStep(Agent agent, double durationInSeconds) {
        return true;
    }

    @Override
    public boolean shouldWaitForAgents() {
        return false;
    }

    @Override
    public void notifyNewEndTime(Time newEndTime) {
        // We don't have to do anything, the model checks the end time with every step.
    }

    @Override
    public void notifyInterrupted(Agent agent) {
        interrupted.add(agent);
    }

    @Override
    protected boolean hasNextStep(Agent agent) {
        if (interrupted.contains(agent)) {
            return true;
        }
        return getLocationAndEndTime().getTimeNanos() - getEnvironment().getTime().getNanos() >= 0;
    }

    @Override
    protected boolean doStep(double durationInSeconds) {

        // Initialize.
        if (movementNeeded && movementModels == null) {
            Point centerPoint = getLocationAndEndTime().getLocation().getCenterPoint();
            Vertex associatedVertex = getEnvironment().getWorld().getVertex(centerPoint);
            if (associatedVertex == null) {
                associatedVertex = getEnvironment().getWorld().getClosestVertex(centerPoint);
            }
            if (!(associatedVertex.getArea().hasFunction(Outside.class))) {
                movementNeeded = false;
                return true;
            }
            polygonToMoveIn = associatedVertex.getArea().getPolygon();
            boundingBoxOfPolygonToMoveIn = GeometryUtils.getBoundingBox(polygonToMoveIn);
            movementModels = new HashMap<>(getActors().size());
            for (Agent agent : getActors()) {
                BasicMovementModel movementModel = createMovementModel(agent);
                movementModels.put(agent, movementModel);
            }
        }

        // Return if no movement is done.
        if (!movementNeeded || movementModels == null) {
            return true;
        }

        // Refresh movement models where needed.
        for (Agent agent : getActors()) {
            BasicMovementModel basicMovementModel = movementModels.get(agent);
            if (basicMovementModel == null || !basicMovementModel.hasNextStep(agent)) {
                BasicMovementModel movementModel = createMovementModel(agent);
                movementModels.put(agent, movementModel);
            }
        }

        // Activate movement models.
        for (Agent agent : getActors()) {
            BasicMovementModel basicMovementModel = movementModels.get(agent);
            if (basicMovementModel != null) {
                basicMovementModel.nextStep(durationInSeconds);
            }
        }

        // And return.
        return true;
    }

    @Override
    protected long doEstimateMinimumTimeToExecute(IGeometry currentActorLocation) {
        return 0;
    }

    private BasicMovementModel createMovementModel(Agent agent) {
        Point pointWithinArea = null;
        int i = 0;
        while (pointWithinArea == null && i < MAX_RANDOM_SAMPLES) {
            double randomX = boundingBoxOfPolygonToMoveIn.getMinX() + RandomNumber.nextDouble(boundingBoxOfPolygonToMoveIn.getMaxX() - boundingBoxOfPolygonToMoveIn.getMinX());
            double randomY = boundingBoxOfPolygonToMoveIn.getMinY() + RandomNumber.nextDouble(boundingBoxOfPolygonToMoveIn.getMaxY() - boundingBoxOfPolygonToMoveIn.getMinY());
            Point point = new Point(randomX, randomY);
            if (polygonToMoveIn.contains(point)) {
                pointWithinArea = point;
            }
            ++i;
        }
        if (pointWithinArea == null) {
            pointWithinArea = RandomNumber.randomElement(Arrays.asList(polygonToMoveIn.getPoints()));
        }
        BasicMovementModel movementModel = new BasicMovementModel(BasicMovementModel.WALKING_SPEED_MS);
        movementModel.setEnvironment(getEnvironment());
        movementModel.setLocationAndEndTime(new LocationAndTime(pointWithinArea));
        movementModel.setActors(new Group(agent));
        agent.pushModel(movementModel);
        return movementModel;
    }
}
