package nl.tno.idsa.library.incidents;

import nl.tno.idsa.framework.behavior.incidents.Incident;
import nl.tno.idsa.framework.semantics_base.objects.ParameterId;
import nl.tno.idsa.framework.semantics_impl.variables.*;
import nl.tno.idsa.framework.world.GeometryType;
import nl.tno.idsa.framework.world.World;
import nl.tno.idsa.library.actions.ParticipateInProcession;
import nl.tno.idsa.library.models.ModelParticipateInProcession;
import nl.tno.idsa.library.roles.Civilian;

import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("unused")
public class IncidentProcession extends Incident {

    // TODO Process a drawn line instead of clicking a point and going in a random direction from there.

    public static enum Parameters implements ParameterId {
        NUMBER_OF_PARTICIPANTS,
        DISTANCE,
        WALKING_SPEED,
        DELAY
    }

    public IncidentProcession(World world) throws InstantiationException {
        super(world, ParticipateInProcession.class);
        setDesiredLocation(new LocationVariable(GeometryType.asSet(GeometryType.POINT, GeometryType.POLYLINE)));
    }

    @Override
    protected Map<ParameterId, Variable> createParameters() {
        Map<ParameterId, Variable> parameters = new HashMap<>();
        parameters.put(Parameters.NUMBER_OF_PARTICIPANTS, new IntegerVariable(30));
        parameters.put(Parameters.DISTANCE, new DoubleVariable(500));
        parameters.put(Parameters.WALKING_SPEED, new DoubleVariable(3));
        parameters.put(Parameters.DELAY, new DoubleVariable(3));
        return parameters;
    }

    @Override
    protected boolean doBindParameters() {

        // Find the model that we need to change.
        ModelParticipateInProcession participateInProcession = ((ParticipateInProcession) getEnablingAction()).getModel();

        // Read number of participants.
        // TODO When used from "Road Block", it appears that we get only one agent, instead of the 30 specified above.
        IntegerVariable numberOfParticipants = (IntegerVariable) getParameters().get(Parameters.NUMBER_OF_PARTICIPANTS);
        setDesiredActor(new GroupVariable(numberOfParticipants.getValue(), Civilian.class));

        // Read distance to cover.
        DoubleVariable distance = (DoubleVariable) getParameters().get(Parameters.DISTANCE);
        participateInProcession.setDesiredDistance(distance.getValue());

        // Read walking speed.
        DoubleVariable walkingSpeed = (DoubleVariable) getParameters().get(Parameters.WALKING_SPEED);
        participateInProcession.setWalkingSpeed(walkingSpeed.getValue());

        // Read delay between agents.
        DoubleVariable delay = (DoubleVariable) getParameters().get(Parameters.DELAY);
        participateInProcession.setDelayBetweenParticipants(delay.getValue());

        return true;
    }

    @Override
    public String getDescription() {
        return "A number of citizens participate in a procession";
    }
}