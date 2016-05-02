package nl.tno.idsa.framework.behavior.incidents;

import nl.tno.idsa.framework.planners.ActionPlan;
import nl.tno.idsa.framework.semantics_base.SemanticLibrary;
import nl.tno.idsa.framework.semantics_base.objects.ParameterId;
import nl.tno.idsa.framework.semantics_base.objects.ParametrizedObject;
import nl.tno.idsa.framework.semantics_impl.actions.Action;
import nl.tno.idsa.framework.semantics_impl.variables.ClassVariable;
import nl.tno.idsa.framework.semantics_impl.variables.GroupVariable;
import nl.tno.idsa.framework.semantics_impl.variables.LocationVariable;
import nl.tno.idsa.framework.semantics_impl.variables.VariableBinder;
import nl.tno.idsa.framework.utils.RandomNumber;
import nl.tno.idsa.framework.utils.TextUtils;
import nl.tno.idsa.framework.world.GeometryType;
import nl.tno.idsa.framework.world.World;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Base class for scenario incidents.
 */
public abstract class Incident extends ParametrizedObject implements Comparable<Incident> {

    private final World world;

    private Class<? extends Action> enablingActionClass;
    private Action enablingAction; // Instance of the class once the incident is initialized.

    private VariableBinder binder = new VariableBinder();
    private ActionPlan actionPlan;

    public static enum Parameters implements ParameterId {LOCATION_VARIABLE, ENABLING_ACTION_OPTIONS}

    /**
     * Note: to be found by the semantic library,
     * implementations must provide a constructor that has the world as only parameter.
     */
    @SuppressWarnings("unchecked")
    protected Incident(World world, Class<? extends Action> enablingActionClass, GeometryType... allowedGeometryTypes)
            throws InstantiationException {
        this.world = world;

        // Handle enabling action class.
        this.enablingActionClass = enablingActionClass;
        if (enablingActionClass == null) {
            throw new IllegalArgumentException("Cannot create an incident based on no action.");
        }

        // Try to find a concrete implementation of the enabling action class.
        try {
            enablingAction = SemanticLibrary.getInstance().createSemanticInstance(enablingActionClass);
        } catch (InstantiationException e) {
            // Someone probably make a semantically abstract action abstract in Java, too.
        }

        // If we found no implementation, or only an abstract one, we need to find all subclasses that are okay.
        if (enablingAction == null || enablingAction.isAbstract()) {
            enablingAction = null; // Erase abstract class.
            ClassVariable<Action> actionClassVariable = new ClassVariable<>();
            Set<Class<? extends Action>> subclasses = SemanticLibrary.getInstance().listSemanticSubclasses(enablingActionClass);
            if (subclasses == null) {
                throw new InstantiationException("Cannot create an incident based on action " + enablingActionClass.getSimpleName() + ". No non-abstract subclasses found.");
            }
            Set viableSubclasses = new HashSet();
            for (Object next : subclasses) {
                Class nextClass = (Class) next;
                try {
                    Action nextAction = (Action) SemanticLibrary.getInstance().createSemanticInstance(nextClass);
                    if (nextAction != null && !nextAction.isAbstract()) {
                        viableSubclasses.add(next);
                    }
                } catch (InstantiationException e) {
                    // Ok so this class is not valid :)
                }
            }
            if (viableSubclasses.size() == 0) {
                throw new IllegalArgumentException("Cannot create an incident based on action " + enablingActionClass.getSimpleName() + ". No suitable non-abstract subclasses found.");
            }
            actionClassVariable.setDomain(viableSubclasses);
            getParameters().put(Parameters.ENABLING_ACTION_OPTIONS, actionClassVariable);
        }

        // Handle geometry options.
        HashSet<GeometryType> allowedGeometryTypesS = new HashSet<GeometryType>();
        allowedGeometryTypesS.addAll(Arrays.asList(allowedGeometryTypes));
        LocationVariable locationVariable = new LocationVariable(allowedGeometryTypesS);
        getParameters().put(Parameters.LOCATION_VARIABLE, locationVariable);
    }

    protected World getWorld() {
        return world;
    }

    @SuppressWarnings("unchecked")
    public Action getEnablingAction() {

        // Try to read the value the user wanted.
        if (enablingAction == null) {
            try {
                Class actionClass = (Class) getParameters().get(Parameters.ENABLING_ACTION_OPTIONS).getValue();
                if (actionClass != null) {
                    enablingAction = (Action) SemanticLibrary.getInstance().createSemanticInstance(actionClass);
                }
            } catch (Exception e) {
                // This is impossible, all options have been tested.
            }
        }

        // Choose a random one.
        if (enablingAction == null) {
            try {
                Set possibilities = getParameters().get(Parameters.ENABLING_ACTION_OPTIONS).getDomain();
                Class actionClass = (Class) RandomNumber.randomElement(possibilities);
                enablingAction = (Action) SemanticLibrary.getInstance().createSemanticInstance(actionClass);
            } catch (Exception e) {
                // This is impossible, all options have been tested.
            }
        }

        // TODO Handle the problem that we may not have any options for an enabling action.
        return enablingAction;
    }

    public VariableBinder getBinder() {
        return binder;
    }

    public Set<GeometryType> getAllowedLocationTypes() {
        return ((LocationVariable) getParameters().get(Parameters.LOCATION_VARIABLE)).getAllowedGeometryTypes();
    }

    protected void setDesiredActor(GroupVariable desiredActor) {
        if (getEnablingAction() == null || getEnablingAction().getActorVariable() == null) return;
        getBinder().bind(getEnablingAction().getActorVariable(), desiredActor);
    }

    protected void setDesiredTarget(GroupVariable desiredTarget) {
        if (getEnablingAction() == null || getEnablingAction().getTargetVariable() == null) return;
        getBinder().bind(getEnablingAction().getTargetVariable(), desiredTarget);
    }

    protected void setDesiredLocation(LocationVariable desiredLocation) {
        if (getEnablingAction() == null || getEnablingAction().getLocationVariable() == null) return;
        getBinder().bind(getEnablingAction().getLocationVariable(), desiredLocation);
    }

    protected LocationVariable getDesiredLocation() {
        if (getEnablingAction() == null) return null;
        return getEnablingAction().getLocationVariable();
    }

    @SuppressWarnings("unchecked")
    public boolean bindParameters() {

        // Create the enabling action if needed.
        if (enablingAction == null) {
            try {
                Class chosenActionClass = (Class) getParameters().get(Parameters.ENABLING_ACTION_OPTIONS).getValue();
                enablingAction = (Action) SemanticLibrary.getInstance().createSemanticInstance(chosenActionClass); // Unchecked 2x.
            } catch (Exception e) {
                // This is impossible, all options have been tested.
            }
        }

        // Handle the location.
        setDesiredLocation((LocationVariable) getParameters().get(Incident.Parameters.LOCATION_VARIABLE));

        // TODO Check here whether the desired location is valid for the enabling action. ...
        // If not, probably return false or throw an exception. Currently, we can e.g. shoplift on the street.
        return doBindParameters();
    }

    /**
     * Returns false if the parameters are invalid.
     */
    protected abstract boolean doBindParameters();

    /**
     * @return Default name, which is the name of the enabling action class.
     */
    public String getName() {
        return TextUtils.camelCaseToText(enablingActionClass.getSimpleName());
    }

    public abstract String getDescription();

    public ActionPlan getActionPlan() {
        return actionPlan;
    }

    public void setActionPlan(ActionPlan actionPlan) {
        this.actionPlan = actionPlan;
    }

    /**
     * For sorting incidents alphabetically.
     */
    @Override
    public int compareTo(Incident o) {
        return getName().compareTo(o.getName());
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{" +
                "enablingActionClass=" + enablingActionClass.getSimpleName() +
                '}';
    }
}
