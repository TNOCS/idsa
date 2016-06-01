package nl.tno.idsa.framework.behavior.incidents;

import nl.tno.idsa.framework.behavior.plans.ActionPlan;
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

    /**
     * Incident parameters. Individual incidents can include their own parameters as well.
     */
    public static enum Parameters implements ParameterId {
        /**
         * The desired location.
         */
        LOCATION_VARIABLE,
        /**
         * The variable will contain a domain of concrete actions that can be chosen, if the enabling action is abstract.
         */
        ENABLING_ACTION_OPTIONS
    }

    /**
     * Note: to be found by the semantic library, incident implementations must provide a constructor that has the world
     * object as the only parameter. The other parameters must be set in
     */
    @SuppressWarnings("unchecked")
    protected Incident(World world, Class<? extends Action> enablingActionClass, GeometryType... allowedGeometryTypes)
            throws InstantiationException {
        this.world = world;

        // Handle enabling action class.
        this.enablingActionClass = enablingActionClass;
        if (enablingActionClass == null) {
            throw new IllegalArgumentException("Cannot create an incident if there is no enabling action.");
        }

        // Try to find a concrete implementation of the enabling action class.
        try {
            enablingAction = SemanticLibrary.getInstance().createSemanticInstance(enablingActionClass);
        } catch (InstantiationException e) {
            // Someone probably made a semantically abstract action abstract in Java, too. This is allowed, but
            // gives an instantiation exception here.
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
                    // Ok... so this class is not valid :)
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

    /**
     * @return The action that enables the incident, e.g. Arrest enables an incident where someone is arrested.
     */
    @SuppressWarnings("unchecked")
    public Action getEnablingAction() {

        // If the enabling action is null, this means the incident has an abstract enabling action. We can now try to
        // see whether the user indicated which concrete action is desired.
        if (enablingAction == null) {
            try {
                Class actionClass = (Class) getParameters().get(Parameters.ENABLING_ACTION_OPTIONS).getValue();
                if (actionClass != null) {
                    enablingAction = (Action) SemanticLibrary.getInstance().createSemanticInstance(actionClass);
                }
            } catch (Exception e) {
                // The user did not specify a valid concrete action.
            }
        }

        // If the user did not choose, we choose a random possibility (and fix it).
        if (enablingAction == null) {
            try {
                Set possibilities = getParameters().get(Parameters.ENABLING_ACTION_OPTIONS).getDomain();
                Class actionClass = (Class) RandomNumber.randomElement(possibilities);
                enablingAction = (Action) SemanticLibrary.getInstance().createSemanticInstance(actionClass);
            } catch (Exception e) {
                // This is impossible, as all options have been tested for instantiation.
                // We also know that the set is not empty, because otherwise the constructor would have failed.
            }
        }

        // This is certainly not null.
        return enablingAction;
    }

    /**
     * @return The variable binder for this incident.
     */
    public VariableBinder getBinder() {
        return binder;
    }

    /**
     * @param desiredActor The actor variable to use.
     *                     This is bound to the actor variable of the enabling action.
     */
    protected void setDesiredActor(GroupVariable desiredActor) {
        if (getEnablingAction() == null || getEnablingAction().getActorVariable() == null) return;
        getBinder().bind(getEnablingAction().getActorVariable(), desiredActor);
    }

    /**
     * @param desiredTarget The target variable to use.
     *                      This is bound to the target variable of the enabling action.
     */
    protected void setDesiredTarget(GroupVariable desiredTarget) {
        if (getEnablingAction() == null || getEnablingAction().getTargetVariable() == null) return;
        getBinder().bind(getEnablingAction().getTargetVariable(), desiredTarget);
    }

    /**
     * @param desiredLocation The location variable to use.
     *                        This is bound to the location variable of the enabling action.
     */
    protected void setDesiredLocation(LocationVariable desiredLocation) {
        if (getEnablingAction() == null || getEnablingAction().getLocationVariable() == null) return;
        getBinder().bind(getEnablingAction().getLocationVariable(), desiredLocation);
    }

    /**
     * After the incident's parameters have been set, this method must be called in order for the parameters to take
     * effect. (For example, the location parameter is interpreted.)
     *
     * @return Whether the parameter values are valid and have indeed been bound.
     */
    @SuppressWarnings("unchecked")
    public boolean bindParameters() {

        // Create the enabling action if needed.
        getEnablingAction();

        // Handle the location.
        setDesiredLocation((LocationVariable) getParameters().get(Parameters.LOCATION_VARIABLE));

        // TODO Check here whether the desired location is valid for the enabling action. ...
        // If not, we probably should return false or throw an exception. Currently, we can e.g. shoplift on the street.
        return doBindParameters();
    }

    /**
     * @return Whether the parameter values are valid and have indeed been bound.
     */
    protected abstract boolean doBindParameters();

    /**
     * Return a string representation of the incident.
     *
     * @return Default name, which is the name of the enabling action class.
     */
    public String getName() {
        return TextUtils.camelCaseToText(enablingActionClass.getSimpleName());
    }

    /**
     * @return A description of this incident in a more verbose form.
     */
    public abstract String getDescription();

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
