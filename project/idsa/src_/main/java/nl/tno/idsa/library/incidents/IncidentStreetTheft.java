package nl.tno.idsa.library.incidents;

import nl.tno.idsa.framework.behavior.incidents.Incident;
import nl.tno.idsa.framework.semantics_base.objects.ParameterId;
import nl.tno.idsa.framework.semantics_impl.variables.GroupVariable;
import nl.tno.idsa.framework.semantics_impl.variables.IntegerVariable;
import nl.tno.idsa.framework.semantics_impl.variables.Variable;
import nl.tno.idsa.framework.world.GeometryType;
import nl.tno.idsa.framework.world.World;
import nl.tno.idsa.library.actions.StreetTheft;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by jongsd on 4-8-15.
 */
@SuppressWarnings("unused")
public class IncidentStreetTheft extends Incident {

    public static enum Parameters implements ParameterId {NUMBER_OF_OFFENDERS, NUMBER_OF_VICTIMS}

    public IncidentStreetTheft(World world) throws InstantiationException {
        super(world, StreetTheft.class, GeometryType.POINT);
    }

    @Override
    protected Map<ParameterId, Variable> createParameters() {
        Map<ParameterId, Variable> parameters = new HashMap<>();
        parameters.put(Parameters.NUMBER_OF_OFFENDERS, new IntegerVariable(2)); // TODO Domain.
        parameters.put(Parameters.NUMBER_OF_VICTIMS, new IntegerVariable(2)); // TODO Domain.
        return parameters;
    }

    @Override
    protected boolean doBindParameters() {

        IntegerVariable numOffenders = (IntegerVariable) getParameters().get(Parameters.NUMBER_OF_OFFENDERS);
        setDesiredActor(new GroupVariable(numOffenders.getValue()));

        IntegerVariable numVictims = (IntegerVariable) getParameters().get(Parameters.NUMBER_OF_VICTIMS);
        setDesiredTarget(new GroupVariable(numVictims.getValue()));

        return true;
    }

    @Override
    public String getDescription() {
        return "A theft occurs";
    }
}

