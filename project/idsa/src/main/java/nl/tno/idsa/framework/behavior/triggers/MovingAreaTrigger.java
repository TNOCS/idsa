package nl.tno.idsa.framework.behavior.triggers;

import nl.tno.idsa.framework.agents.Agent;
import nl.tno.idsa.framework.behavior.models.Model;
import nl.tno.idsa.framework.utils.RandomNumber;
import nl.tno.idsa.framework.world.Point;
import nl.tno.idsa.framework.world.Polygon;

import java.util.ArrayList;
import java.util.List;

/**
 * Area trigger that can move around.
 */
public abstract class MovingAreaTrigger extends AreaTrigger {

    private Point location;

    public MovingAreaTrigger(Polygon polygonAroundOrigin, double probability) {
        super(polygonAroundOrigin, probability);
    }

    @Override
    public Point getLocation() {
        return (location != null) ? location : getShape().getCenterPoint();
    }

    public void setLocation(Point location) {
        this.location = location;
        Model model = getModel();
        if (model != null) {
            model.getLocationAndEndTime().setLocation(location);
        }
    }

    @Override
    protected List<Agent> determineAffectedAgents(double probability) {
        List<Agent> agentsIn = getEnvironment().getAgentsIn(getShape().translate(location));
        if (probability < 1) {
            List<Agent> agentsAffected = new ArrayList<>();
            for (Agent agent : agentsIn) {
                if (RandomNumber.nextDouble() < probability) {
                    agentsAffected.add(agent);
                }
            }
            agentsIn = agentsAffected;
        }
        return agentsIn;
    }
}
