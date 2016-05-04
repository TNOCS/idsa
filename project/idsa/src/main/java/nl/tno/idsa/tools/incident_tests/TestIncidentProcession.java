package nl.tno.idsa.tools.incident_tests;

import nl.tno.idsa.framework.behavior.incidents.Incident;
import nl.tno.idsa.framework.semantics_base.objects.ParameterId;
import nl.tno.idsa.framework.semantics_impl.locations.LocationAndTime;
import nl.tno.idsa.framework.semantics_impl.variables.IntegerVariable;
import nl.tno.idsa.framework.semantics_impl.variables.LocationVariable;
import nl.tno.idsa.framework.semantics_impl.variables.Variable;
import nl.tno.idsa.framework.world.Environment;
import nl.tno.idsa.framework.world.Point;
import nl.tno.idsa.library.incidents.IncidentProcession;

import java.util.Map;

public class TestIncidentProcession extends IncidentTester {

    public static void main(String[] args) throws Exception {
        (new TestIncidentProcession()).testIncident();
    }

    @Override
    protected Incident createIncident(Environment env) throws InstantiationException {
        return new IncidentProcession(env.getWorld());
    }

    @Override
    protected long initializeIncidentParameters(Environment env, Incident incident) {
        Map<ParameterId, Variable> parameters = incident.getParameters();
        parameters.put(IncidentProcession.Parameters.NUMBER_OF_PARTICIPANTS, new IntegerVariable(5));
        long desiredTime = env.getTime().getCopyWithDifference(0, 15, 0).getNanos();      // 15 minuten
        parameters.put(Incident.Parameters.LOCATION_VARIABLE, new LocationVariable(new LocationAndTime(new Point(1876, 2201), desiredTime)));
        return desiredTime;
    }
}


