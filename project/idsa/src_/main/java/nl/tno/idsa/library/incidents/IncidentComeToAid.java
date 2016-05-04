package nl.tno.idsa.library.incidents;

import nl.tno.idsa.framework.behavior.incidents.Incident;
import nl.tno.idsa.framework.semantics_base.objects.ParameterId;
import nl.tno.idsa.framework.semantics_impl.variables.GroupVariable;
import nl.tno.idsa.framework.semantics_impl.variables.IntegerVariable;
import nl.tno.idsa.framework.semantics_impl.variables.Variable;
import nl.tno.idsa.framework.world.GeometryType;
import nl.tno.idsa.framework.world.World;
import nl.tno.idsa.library.actions.abstract_actions.ComeToAid;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by jongsd on 4-8-15.
 */
@SuppressWarnings("unused")
public class IncidentComeToAid extends Incident {

    public static enum Parameters implements ParameterId {NUMBER_OF_VICTIMS, NUMBER_OF_FIRST_RESPONDERS}

    public IncidentComeToAid(World world) throws InstantiationException {
        super(world, ComeToAid.class, GeometryType.POINT);     // Note that this is an abstract action class!
    }

    @Override
    protected Map<ParameterId, Variable> createParameters() {
        Map<ParameterId, Variable> parameters = new HashMap<>();
        parameters.put(Parameters.NUMBER_OF_VICTIMS, new IntegerVariable(1)); // TODO Domain.
        parameters.put(Parameters.NUMBER_OF_FIRST_RESPONDERS, new IntegerVariable(1)); // TODO Domain.
        return parameters;
    }

    @Override
    protected boolean doBindParameters() {

        IntegerVariable numResponders = (IntegerVariable) getParameters().get(Parameters.NUMBER_OF_FIRST_RESPONDERS);
        setDesiredActor(new GroupVariable(numResponders.getValue()));

        IntegerVariable numVictims = (IntegerVariable) getParameters().get(Parameters.NUMBER_OF_VICTIMS);
        setDesiredTarget(new GroupVariable(numVictims.getValue()));

        return true;
    }

    @Override
    public String getDescription() {
        return "First responders come to the aid of a victim";
    }
}
