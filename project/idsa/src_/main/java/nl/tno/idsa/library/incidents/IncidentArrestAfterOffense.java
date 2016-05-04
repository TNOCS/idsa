package nl.tno.idsa.library.incidents;

import nl.tno.idsa.framework.behavior.incidents.Incident;
import nl.tno.idsa.framework.semantics_base.SemanticLibrary;
import nl.tno.idsa.framework.semantics_base.objects.ParameterId;
import nl.tno.idsa.framework.semantics_impl.variables.GroupVariable;
import nl.tno.idsa.framework.semantics_impl.variables.IntegerVariable;
import nl.tno.idsa.framework.semantics_impl.variables.RoleVariable;
import nl.tno.idsa.framework.semantics_impl.variables.Variable;
import nl.tno.idsa.framework.world.GeometryType;
import nl.tno.idsa.framework.world.World;
import nl.tno.idsa.library.actions.Arrest;
import nl.tno.idsa.library.roles.Offender;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by jongsd on 4-8-15.
 */
@SuppressWarnings("unused")

// TODO More incidents would of course be great. Also perhaps we want to call them incidents.

public class IncidentArrestAfterOffense extends Incident {

    public static enum Parameters implements ParameterId {
        NUMBER_OF_OFFENDERS, NUMBER_OF_ARRESTING_OFFICERS, DESIRED_OFFENDER_ROLE
    }


    public IncidentArrestAfterOffense(World world) throws InstantiationException {
        super(world, Arrest.class, GeometryType.POINT);
    }

    @Override
    protected Map<ParameterId, Variable> createParameters() {
        HashMap<ParameterId, Variable> parameters = new HashMap<>();
        parameters.put(Parameters.NUMBER_OF_OFFENDERS, new IntegerVariable(2)); // TODO Domain.
        parameters.put(Parameters.NUMBER_OF_ARRESTING_OFFICERS, new IntegerVariable(2)); // TODO Domain.
        RoleVariable roleVariable = new RoleVariable();
        Set<Class<? extends Offender>> roleVariableDomain = SemanticLibrary.getInstance().listSemanticSubclasses(Offender.class);
        roleVariable.setDomain(roleVariableDomain);
        roleVariable.setValue(Offender.class); // Default value.
        parameters.put(Parameters.DESIRED_OFFENDER_ROLE, roleVariable);
        return parameters;
    }

    @Override
    protected boolean doBindParameters() {

        IntegerVariable numPolice = (IntegerVariable) getParameters().get(Parameters.NUMBER_OF_ARRESTING_OFFICERS);
        setDesiredActor(new GroupVariable(numPolice.getValue()));

        IntegerVariable numOffenders = (IntegerVariable) getParameters().get(Parameters.NUMBER_OF_OFFENDERS);
        RoleVariable desiredRoleOfOffenders = (RoleVariable) getParameters().get(Parameters.DESIRED_OFFENDER_ROLE);
        setDesiredTarget(new GroupVariable(numOffenders.getValue(), desiredRoleOfOffenders.getValue()));

        return true;
    }

    @Override
    public String getDescription() {
        return "The police arrest one or more offenders";
    }
}
