package nl.tno.idsa.framework.behavior.triggers;

import nl.tno.idsa.framework.agents.Agent;
import nl.tno.idsa.framework.utils.GeometryUtils;
import nl.tno.idsa.framework.utils.RandomNumber;
import nl.tno.idsa.framework.utils.Tuple;
import nl.tno.idsa.framework.world.IGeometry;
import nl.tno.idsa.framework.world.Point;
import nl.tno.idsa.framework.world.PolyLine;

import java.util.ArrayList;
import java.util.List;

/**
 * Area trigger that cannot move around.
 */
public abstract class StaticAreaTrigger extends AreaTrigger {

    private double effectiveDistanceFromShape;

    public StaticAreaTrigger(PolyLine path, double effectiveDistanceFromShape, double probability) {
        super(path, probability);
        this.effectiveDistanceFromShape = effectiveDistanceFromShape;
    }

    public StaticAreaTrigger(Point centerPoint, double radius, double probability) {
        super(centerPoint, probability);
        this.effectiveDistanceFromShape = radius;
    }

    @Override
    public IGeometry getShape() {
        return super.getShape();
    }

    @Override
    public Point getLocation() {
        return getShape().getCenterPoint();
    }

    @Override
    protected List<Agent> determineAffectedAgents(double probability) {
        ArrayList<Agent> affectedAgents = new ArrayList<>();

        // For poly-lines, test proximity.
        if (getShape() instanceof PolyLine) {
            for (Agent agent : getEnvironment().getAgents()) {
                if (agent.isPartOfEvent()) {
                    continue;
                }
                Point[] points = getShape().getPoints();
                for (int i = 0; i < points.length - 1; i++) {
                    Tuple<Double, Double> tuple = GeometryUtils.distanceToLineSegment(agent.getLocation(), points[i], points[i + 1]);
                    if (tuple.getFirst() <= effectiveDistanceFromShape) {
                        affectedAgents.add(agent);
                        break;
                    }
                }
            }
        }

        // For points, the same.
        else if (getShape() instanceof Point) {
            for (Agent agent : getEnvironment().getAgents()) {
                if (agent.isPartOfEvent()) {
                    continue;
                }
                if (agent.getLocation().euclideanDistanceTo(getShape().getCenterPoint()) <= effectiveDistanceFromShape) {
                    affectedAgents.add(agent);
                }
            }
        }

        // Probabilistic attraction.
        if (probability < 1) {
            ArrayList<Agent> reallyAffectedAgents = new ArrayList<>();
            for (Agent agent : affectedAgents) {
                if (RandomNumber.nextDouble() < probability) {
                    reallyAffectedAgents.add(agent);
                }
            }
            affectedAgents = reallyAffectedAgents;
        }

        // Return what we found.
        return affectedAgents;
    }
}
