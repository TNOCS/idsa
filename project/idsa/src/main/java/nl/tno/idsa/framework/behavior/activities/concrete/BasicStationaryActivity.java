package nl.tno.idsa.framework.behavior.activities.concrete;

import nl.tno.idsa.framework.behavior.activities.possible.PossibleActivity;
import nl.tno.idsa.framework.behavior.models.Model;
import nl.tno.idsa.framework.semantics_impl.groups.Group;
import nl.tno.idsa.framework.semantics_impl.locations.LocationAndTime;
import nl.tno.idsa.framework.world.Environment;
import nl.tno.idsa.framework.world.Time;
import nl.tno.idsa.framework.world.Vertex;
import nl.tno.idsa.library.models.BasicAreaModel;

/**
 * A simple stationary activity, which optionally uses a basic area model while the agent is at the location.
 */
public class BasicStationaryActivity extends Activity {

    /**
     * Whether the participants should use a model that makes them wander around.
     */
    public static enum WanderPolicy {
        OUTSIDE_ONLY, NEVER
    }

    private Model model;

    public BasicStationaryActivity(PossibleActivity parent, Environment environment, Time start, Time end, Vertex location, Group group, WanderPolicy wanderPolicy) {
        super(parent, environment, location, start, location, end, group);

        // Create a model so the agent moves around the area, if desired.
        if (wanderPolicy == WanderPolicy.OUTSIDE_ONLY) {
            model = new BasicAreaModel();
            model.setLocationAndEndTime(new LocationAndTime(location.getPoint(), end.getNanos()));
            model.setActors(group);
            model.setEnvironment(getEnvironment());
        }
    }

    @Override
    public Model getModel() {
        return model;
    }

    @Override
    public String getName() {
        return getPossibleActivity().getName();
    }
}
