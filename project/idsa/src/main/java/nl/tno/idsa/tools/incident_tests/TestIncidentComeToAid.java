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
import nl.tno.idsa.library.incidents.IncidentComeToAid;
import nl.tno.idsa.library.locations.PoliceSpawnPoint;

import java.util.List;
import java.util.Map;

public class TestIncidentComeToAid extends IncidentTester {
    public static void main(String[] args) throws Exception {
        (new TestIncidentComeToAid()).testIncident();
    }

    @Override
    protected Incident createIncident(Environment env) throws InstantiationException {
        // Add some police stations, randomly, as they are not in the world yet.
        // TODO Create police stations in the world.
        List<Vertex> vertices = env.getWorld().getVertices();
        int changedVertices = 0;
        while (changedVertices < 50) {
            Vertex randomVertex = vertices.get(RandomNumber.nextInt(vertices.size()));
            if (randomVertex.getArea() != null) {
                randomVertex.getArea().addFunction(new PoliceSpawnPoint());
                changedVertices++;
            }
        }
        return new IncidentComeToAid(env.getWorld());
    }

    @Override
    protected long initializeIncidentParameters(Environment env, Incident incident) {
        Map<ParameterId, Variable> parameters = incident.getParameters();
        long desiredTime = env.getTime().getCopyWithDifference(0, 30, 0).getNanos();
        parameters.put(Incident.Parameters.LOCATION_VARIABLE, new LocationVariable(new LocationAndTime(new Point(1876, 2201), desiredTime)));
        parameters.put(IncidentComeToAid.Parameters.NUMBER_OF_VICTIMS, new IntegerVariable(2));
        return desiredTime;
    }
}


