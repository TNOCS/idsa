package nl.tno.idsa.library.incidents;

import nl.tno.idsa.framework.behavior.incidents.Incident;
import nl.tno.idsa.framework.semantics_base.objects.ParameterId;
import nl.tno.idsa.framework.semantics_impl.variables.Variable;
import nl.tno.idsa.framework.world.GeometryType;
import nl.tno.idsa.framework.world.World;
import nl.tno.idsa.library.actions.abstract_actions.Crime;

import java.util.SortedMap;
import java.util.TreeMap;

/**
 * Created by jongsd on 4-8-15.
 */
@SuppressWarnings("unused")
public class IncidentCrime extends Incident {

    public IncidentCrime(World world) throws InstantiationException {
        super(world, Crime.class, GeometryType.POINT);     // Note that this is an abstract action class!
    }

    @Override
    protected SortedMap<ParameterId, Variable> createParameters() {
        return new TreeMap<>();
    }

    @Override
    protected boolean doBindParameters() {
        return true;
    }

    @Override
    public String getDescription() {
        return "Criminal activity";
    }
}
