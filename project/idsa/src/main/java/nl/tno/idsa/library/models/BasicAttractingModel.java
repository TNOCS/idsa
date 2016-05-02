package nl.tno.idsa.library.models;

import nl.tno.idsa.framework.agents.Agent;
import nl.tno.idsa.framework.behavior.models.Model;
import nl.tno.idsa.framework.utils.GeometryUtils;
import nl.tno.idsa.framework.world.IGeometry;
import nl.tno.idsa.framework.world.Point;
import nl.tno.idsa.framework.world.Time;

public class BasicAttractingModel extends Model {

    // TODO Very unimpressive model.

    private double minimumDesiredDistance;

    public BasicAttractingModel() {
        this(15);
    }

    public BasicAttractingModel(double minimumDesiredDistance) {
        this.minimumDesiredDistance = minimumDesiredDistance;
    }

    @Override
    public boolean agentsCanWaitBeforeStarting() {
        return false;
    }

    @Override
    protected boolean doWaitingStep(Agent agent, double durationInSeconds) {
        return false;
    }

    @Override
    public boolean shouldWaitForAgents() {
        return false;
    }

    @Override
    public void notifyNewEndTime(Time newTime) {

    }

    @Override
    public void notifyInterrupted(Agent agent) {

    }

    @Override
    protected boolean hasNextStep(Agent agent) {
        return agent.getLocation().euclideanDistanceTo(getLocationAndEndTime().getLocation().getCenterPoint()) > minimumDesiredDistance;
    }

    @Override
    protected boolean doStep(double durationInSeconds) {
        for (Agent agent : getActors()) {
            if (!hasNextStep(agent)) {
                continue;
            }
            Point c = GeometryUtils.getClosestIntersectionPoint(agent.getLocation(), getLocationAndEndTime().getLocation());
            if (c == null) {
                continue;
            }
            double cx = c.getX();
            double cy = c.getY();
            double ax = agent.getLocation().getX();
            double ay = agent.getLocation().getY();
            double dx = cx - ax;
            double dy = cy - ay;
            double currentDistance = Math.sqrt(dx * dx + dy * dy);
            double moveDistance = durationInSeconds / 2; // TODO Hardcoded move 0.5 meter per second forward.
            double newDistance = Math.max(currentDistance - moveDistance, minimumDesiredDistance);
            double ratio = newDistance / currentDistance;
            ax = cx - ratio * dx;
            ay = cy - ratio * dy;
            agent.setLocation(new Point(ax, ay)); // TODO No path planning, no checks whether the agent bumps into anything.
        }
        return true;
    }

    @Override
    protected long doEstimateMinimumTimeToExecute(IGeometry currentActorLocation) {
        return 0;
    }
}
