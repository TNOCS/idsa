package nl.tno.idsa.library.incidents;

import nl.tno.idsa.framework.behavior.incidents.Incident;
import nl.tno.idsa.framework.semantics_base.objects.ParameterId;
import nl.tno.idsa.framework.semantics_impl.variables.Variable;
import nl.tno.idsa.framework.world.GeometryType;
import nl.tno.idsa.framework.world.World;
import nl.tno.idsa.library.actions.abstract_actions.RoadBlock;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by jongsd on 4-8-15.
 */
@SuppressWarnings("unused")
public class IncidentRoadBlock extends Incident {

    public IncidentRoadBlock(World world) throws InstantiationException {
        super(world, RoadBlock.class, GeometryType.POINT);     // Note that this is an abstract action class!
    }

    @Override
    protected Map<ParameterId, Variable> createParameters() {
        return new HashMap<>();
    }

    @Override
    protected boolean doBindParameters() {
        return true;
    }

    @Override
    public String getDescription() {
        return "Road is blocked by some activity";
    }
}
