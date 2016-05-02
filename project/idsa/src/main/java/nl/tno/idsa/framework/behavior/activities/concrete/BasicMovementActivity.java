package nl.tno.idsa.framework.behavior.activities.concrete;

import nl.tno.idsa.framework.agents.Agent;
import nl.tno.idsa.framework.behavior.activities.possible.PossibleActivity;
import nl.tno.idsa.framework.behavior.models.Model;
import nl.tno.idsa.framework.semantics_impl.groups.Group;
import nl.tno.idsa.framework.semantics_impl.locations.LocationAndTime;
import nl.tno.idsa.framework.world.Time;
import nl.tno.idsa.framework.world.Vertex;
import nl.tno.idsa.library.models.BasicMovementModel;

/**
 * Basic activity for moving. Uses a basic movement model that is shared by all participants.
 */
public class BasicMovementActivity extends Activity {

    private Model model;
    private final boolean towards;

    /**
     * Create a basic movement activity.
     *
     * @param possibleActivity The possible activity that created this activity.
     * @param startLocation    Where to start.
     * @param startTime        When to start.
     * @param endLocation      Where to end up.
     * @param endTime          When to end up there.
     * @param participants     Who is moving.
     * @param towards          Whether agents are moving towards, or from, an activity.
     */
    public BasicMovementActivity(PossibleActivity possibleActivity, Vertex startLocation, Time startTime, Vertex endLocation, Time endTime, Group participants, boolean towards) {
        super(possibleActivity, startLocation, startTime, endLocation, endTime, participants);
        this.towards = towards;

        // Make the movement model.
        double slowest = Double.MAX_VALUE;
        for (Agent agent : participants) {
            if (agent.getNormalSpeedMs() < slowest) {
                slowest = agent.getNormalSpeedMs();
            }
        }
        BasicMovementModel model = new BasicMovementModel(slowest);
        model.setLocationAndEndTime(new LocationAndTime(endLocation.getPoint(), endTime.getNanos()));
        model.setActors(participants);

        // TODO Pass environment instead of getting it from an agent?
        Agent a = (Agent) participants.get(0);
        model.setEnvironment(a.getEnvironment());
        model.setForcedStartTime(startTime.getNanos());

        this.model = model;
    }

    @Override
    public Model getModel() {
        return model;
    }

    @Override
    public String getName() {
        if (getPossibleActivity() == null) {
            return "Travel";
        }
        if (towards) {
            return "To" + getPossibleActivity().getName();
        } else {
            return "From" + getPossibleActivity().getName();
        }
    }
}
