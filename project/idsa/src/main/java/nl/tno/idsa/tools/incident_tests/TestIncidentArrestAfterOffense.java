package nl.tno.idsa.tools.incident_tests;

import nl.tno.idsa.framework.behavior.incidents.Incident;
import nl.tno.idsa.framework.semantics_base.objects.ParameterId;
import nl.tno.idsa.framework.semantics_impl.locations.LocationAndTime;
import nl.tno.idsa.framework.semantics_impl.variables.IntegerVariable;
import nl.tno.idsa.framework.semantics_impl.variables.LocationVariable;
import nl.tno.idsa.framework.semantics_impl.variables.Variable;
import nl.tno.idsa.framework.utils.RandomNumber;
import nl.tno.idsa.framework.world.Environment;
import nl.tno.idsa.framework.world.Point;
import nl.tno.idsa.framework.world.Vertex;
import nl.tno.idsa.library.incidents.IncidentArrestAfterOffense;
import nl.tno.idsa.library.locations.PoliceSpawnPoint;

import java.util.List;
import java.util.Map;

public class TestIncidentArrestAfterOffense extends IncidentTester {
    public static void main(String[] args) throws Exception {
        (new TestIncidentArrestAfterOffense()).testIncident();
    }

    @Override
    protected Incident createIncident(Environment env) throws InstantiationException {
        List<Vertex> vertices = env.getWorld().getVertices();
        int changedVertices = 0;
        while (changedVertices < 50) {
            Vertex randomVertex = vertices.get(RandomNumber.nextInt(vertices.size()));
            if (randomVertex.getArea() != null) {
                randomVertex.getArea().addFunction(new PoliceSpawnPoint());
                changedVertices++;
            }
        }
        return new IncidentArrestAfterOffense(env.getWorld());
    }

    @Override
    protected long initializeIncidentParameters(Environment env, Incident incident) {
        Map<ParameterId, Variable> parameters = incident.getParameters();
        parameters.put(IncidentArrestAfterOffense.Parameters.NUMBER_OF_OFFENDERS, new IntegerVariable(2));
        parameters.put(IncidentArrestAfterOffense.Parameters.NUMBER_OF_ARRESTING_OFFICERS, new IntegerVariable(2));
        // parameters.put(EventArrestAfterOffense.DESIRED_OFFENDER_ROLE, new RoleVariable(Shoplifter.class)); // Turn on if desired.
        long desiredTime = env.getTime().getCopyWithDifference(0, 30, 0).getNanos(); // 30 minutes
        parameters.put(Incident.Parameters.LOCATION_VARIABLE, new LocationVariable(new LocationAndTime(new Point(2000, 2000), desiredTime)));
        return desiredTime;
    }
}


