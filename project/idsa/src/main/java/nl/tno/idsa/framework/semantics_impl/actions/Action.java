package nl.tno.idsa.framework.semantics_impl.actions;

import nl.tno.idsa.framework.behavior.models.IModelOwner;
import nl.tno.idsa.framework.behavior.models.Model;
import nl.tno.idsa.framework.semantics_base.SemanticLibrary;
import nl.tno.idsa.framework.semantics_base.objects.ParameterId;
import nl.tno.idsa.framework.semantics_base.objects.SemanticObject;
import nl.tno.idsa.framework.semantics_base.relations.SemanticRelation;
import nl.tno.idsa.framework.semantics_impl.locations.LocationFunction;
import nl.tno.idsa.framework.semantics_impl.relations.ActionEnablesRole;
import nl.tno.idsa.framework.semantics_impl.relations.LocationEnablesAction;
import nl.tno.idsa.framework.semantics_impl.relations.RoleEnablesAction;
import nl.tno.idsa.framework.semantics_impl.roles.Role;
import nl.tno.idsa.framework.semantics_impl.variables.GroupVariable;
import nl.tno.idsa.framework.semantics_impl.variables.LocationVariable;
import nl.tno.idsa.framework.semantics_impl.variables.Variable;
import nl.tno.idsa.framework.utils.TextUtils;
import nl.tno.idsa.framework.world.Environment;
import nl.tno.idsa.framework.world.GeometryType;
import nl.tno.idsa.framework.world.Time;

import java.util.*;

/**
 * Action superclass. Actions are atomic plan components with three main components: actors, targets, and location/time.
 * Each action is coupled with one or more models that can perform the behavior belonging to the action. For example, an action
 * called Arrest will have a model called Arrest, where the actor displays arresting behavior towards a target, at a
 * specific place and time.
 *
 * @author jongsd
 */
public abstract class Action extends SemanticObject<Action> implements IModelOwner {

    private static int ID = 1000;
    private int id = ID++;

    private boolean isAbstract;
    private HashSet<Class<? extends Action>> superclasses;

    private final String actionVerb;

    private final GroupVariable actorVariable;
    private final GroupVariable targetVariable;
    private final Set<GeometryType> allowedLocationTypes;

    // Right now we assume this location is where we end up, and if the action cannot move, it is also where we need
    // to start (i.e. via pre- and postconditions).
    private LocationVariable locationVariable; // TODO perhaps rename to something that denotes we END UP there.

    /**
     * Constants to denote whether the action allows automatically inserting a move action before it during plan generation.
     */
    public enum AllowsInsertMoveActionBefore {
        YES, NO
    }

    private AllowsInsertMoveActionBefore allowsInsertMoveActionBefore;

    /**
     * Constants to denote whether the action (model) will realize the agents involved in it (actors and/or targets) e.g. by spawning or a trigger.
     */
    // TODO Should there be a distinction between providing actors and/or targets, rather than neither or both?
    public enum ProvidesAgents {
        YES, NO
    }

    private ProvidesAgents providesAgents;

    private boolean movesActor;
    // TODO Think; for now we assume actions can only move the actors, not the targets. Otherwise, we need to work on the planner.
    // private boolean movesTarget;

    // TODO Think; actions may have other pre- and postconditions. ...
    // For example, an agent must have picked up an object somewhere. This is not in our incidents (yet).
    // Many pre- and postconditions have impact on whether the action can be executed at a certain point/time in
    // the game world, not on a logical sequence of actions.
    private Class<? extends Role> actorRequiredRole;
    private Class<? extends Role> targetRequiredRole;
    private Class<? extends Role> actorRoleAfter;
    private Class<? extends Role> targetRoleAfter;

    private List<Model> models; // TODO Replace multiple models for an action by appropriate handling of multiple models in subclasses. ...
    // The only subclass that uses this is the LastMinuteAction, which inserts an idle model before the actual one so the idle model keeps
    // running until the time in the location variable expires.

    /**
     * Create a new abstract action. This action has no information on how to execute, except for semantic hierarchy.
     */
    public Action() {
        this(null, null, null, null, null, null);
        isAbstract = true;
    }

    /**
     * Create a new non-abstract action.
     *
     * @param model                        The behavior model belonging to the action.
     * @param actorVariable                Template/variable for who performs the action.
     * @param targetVariable               Template/variable for who the action is performed on (optional).
     * @param allowedLocationTypes         The types of location this action can receive as input (e.g. point, polygon).
     * @param providesAgents               Whether the action (model) spawns agents when the model is first executed.
     * @param allowsInsertMoveActionBefore Whether the planner can insert a move action before this action.
     *                                     Probably false for an action that is a move itself.
     */
    protected Action(Model model, GroupVariable actorVariable, GroupVariable targetVariable, Set<GeometryType> allowedLocationTypes,
                     ProvidesAgents providesAgents, AllowsInsertMoveActionBefore allowsInsertMoveActionBefore) {

        // Convenience for subclasses of abstract action: they define semantic superclasses in an array instead of a set.
        Class<? extends Action>[] semanticSuperclassArray = createSemanticSuperclassArray();
        if (semanticSuperclassArray != null) {
            superclasses = new HashSet<>();
            Collections.addAll(superclasses, semanticSuperclassArray);
        }

        this.actionVerb = TextUtils.camelCaseToText(getClass().getSimpleName());

        this.isAbstract = false;
        this.models = new ArrayList<>(1);
        this.models.add(model);

        this.actorVariable = actorVariable;
        this.targetVariable = targetVariable;
        this.allowedLocationTypes = allowedLocationTypes;

        // Allows move before?
        this.allowsInsertMoveActionBefore = allowsInsertMoveActionBefore;

        // Spawning?
        this.providesAgents = providesAgents;
        determineAllowsMoveActionBefore();
    }

    private void initializeSemanticRelations() { // This has to happen lazily. Action constructor cannot use semantic library, as it is constructed in parallel.

        // Return if already done.
        if (locationVariable != null) {
            return;
        }

        // Determine whether this action requires specific locations.
        // TODO We are creating a shortcut here that only works because roles and locations are the only preconditions. ...
        // What would be most neat is if we would investigate all relations below with PreconditionRelation instead of
        // ActionRequiresLocation, and then simply have a list of precondition relations that the planner can query. It
        // should then handle each (known) precondition. Right now, we've explicitly coded the logic for different
        // pre/post-conditions into the action (locations, roles) and the planner.
        // Also, this look-up happens for every instance we make of the action in question, even though in fact the
        // location requirements (and other preconditions) are an attribute of the action class, not the instance.
        Set<SemanticRelation<LocationFunction, Action>> requiredLocationRelations = SemanticLibrary.getInstance().getSemanticRelationsTo(LocationEnablesAction.class, getClass());
        Set<Class<? extends LocationFunction>> requiredLocations = new HashSet<>();
        if (requiredLocationRelations != null && requiredLocationRelations.size() > 0) {
            for (SemanticRelation<LocationFunction, Action> relation : requiredLocationRelations) {
                requiredLocations.add(relation.getFrom());
            }
            this.locationVariable = new LocationVariable(allowedLocationTypes, requiredLocations);
        } else {
            this.locationVariable = new LocationVariable(allowedLocationTypes);
        }

        // Determine whether this action requires specific roles.
        // TODO See above. This is a shortcut to avoid investigating preconditions properly (but complexly).
        Set<SemanticRelation<Role, Action>> requiredRoleRelations = SemanticLibrary.getInstance().getSemanticRelationsTo(RoleEnablesAction.class, getClass());
        if (requiredRoleRelations != null && requiredRoleRelations.size() > 0) {
            boolean actDone = actorRequiredRole != null, targDone = targetRequiredRole != null;
            for (SemanticRelation<Role, Action> relation : requiredRoleRelations) {
                if (actDone && targDone) {
                    break;
                }
                RoleEnablesAction roleEnablesAction = (RoleEnablesAction) relation;
                if (actorRequiredRole == null && roleEnablesAction.isRoleRequiredFromActor()) {
                    actorRequiredRole = roleEnablesAction.getEnabler();
                    actDone = true;
                } else if (targetRequiredRole == null) {
                    targetRequiredRole = roleEnablesAction.getEnabler();
                    targDone = true;
                }
            }
        }

        // Determine whether this action gives agents specific roles.
        // TODO See above.
        Set<SemanticRelation<Action, Role>> createdRoleRelations = SemanticLibrary.getInstance().getSemanticRelationsFrom(getClass(), ActionEnablesRole.class);
        if (requiredRoleRelations != null && createdRoleRelations.size() > 0) {
            boolean actDone = actorRoleAfter != null, targDone = targetRoleAfter != null;
            for (SemanticRelation<Action, Role> relation : createdRoleRelations) {
                if (actDone && targDone) {
                    break;
                }
                ActionEnablesRole actionEnablesRole = (ActionEnablesRole) relation;
                if (actorRoleAfter == null && actionEnablesRole.doesActionGiveActorTheRole()) {
                    actorRoleAfter = actionEnablesRole.getEnabled();
                    actDone = true;
                } else if (targetRoleAfter == null) {
                    targetRoleAfter = actionEnablesRole.getEnabled();
                    targDone = true;
                }
            }
        }
    }

    // A spawning model cannot have a move before it.
    private void determineAllowsMoveActionBefore() {
        if (providesAgents == ProvidesAgents.YES) {
            this.allowsInsertMoveActionBefore = AllowsInsertMoveActionBefore.NO;
        }
    }

    /**
     * Return whether the action is abstract (i.e. no model) or concrete.
     */
    public boolean isAbstract() {
        return isAbstract;
    }

    /**
     * Get the actor variable, i.e. in which we denote who performs the action.
     */
    public GroupVariable getActorVariable() {
        return actorVariable;
    }

    /**
     * Get the target variable, i.e. in which we denote on who the action is performed, optionally.
     */
    public GroupVariable getTargetVariable() {
        return targetVariable;
    }

    /**
     * Get the location variable, i.e. in which we denote where and when the action ends up.
     */
    public LocationVariable getLocationVariable() {
        initializeSemanticRelations(); // Location variable uses semantic information.
        return locationVariable;
    }

    /**
     * Get the allowed location types. (These are only used in constructor of Action.class.)
     */
    protected Set<GeometryType> getAllowedLocationTypes() {
        return allowedLocationTypes;
    }

    /**
     * Does the action allow the planner to automatically insert a move action before it?
     */
    public AllowsInsertMoveActionBefore allowsInsertMoveActionBefore() {
        return allowsInsertMoveActionBefore;
    }

    /**
     * Does the action provide agents when first executed, or do we need to look for agents?
     */
    public ProvidesAgents providesAgents() {
        return providesAgents;
    }

    /**
     * Return the (optional) role required from the actor in order to perform this action. This knowledge can be
     * added to the semantic library by adding an instance of the ActionRequiresRole relation.
     */
    public Class<? extends Role> getActorRequiredRole() {
        initializeSemanticRelations();
        return actorRequiredRole;
    }

    /**
     * Return the (optional) role required from the target in order to have this action performed on it. This knowledge can be
     * added to the semantic library by adding an instance of the ActionRequiresRole relation.
     */
    public Class<? extends Role> getTargetRequiredRole() {
        initializeSemanticRelations();
        return targetRequiredRole;
    }

    /**
     * Get the role of the actor after the action (optional). This knowledge can be
     * added to the semantic library by adding an instance of the RoleRequiresAction relation.
     */
    public Class<? extends Role> getActorRoleAfter() {
        initializeSemanticRelations();
        return actorRoleAfter;
    }

    /**
     * Get the role of the target after the action (optional). This knowledge can be
     * added to the semantic library by adding an instance of the RoleRequiresAction relation.
     */
    public Class<? extends Role> getTargetRoleAfter() {
        initializeSemanticRelations();
        return targetRoleAfter;
    }

    /**
     * Return whether the action will move the actor towards the location in the location variable.
     */
    public boolean movesActor() {
        return movesActor;
    }

    /**
     * Set whether the action will move the actor towards the location in the location variable.
     */
    protected void setMovesActor(boolean movesActor) {
        this.movesActor = movesActor;
    }

    // TODO Document this method. Why is it used? When does explicit duration take precedence over the time in the location variable?
    public void setExplicitDuration(long duration) {
        getModel().setExplicitDuration(Time.NANO_SECOND * duration);
    }

    /**
     * Get a verb describing this action. Default is the name of the class.
     */
    public String getVerb() {
        return actionVerb;
    }

    /**
     * Return the underlying model.
     */
    @Override
    public Model getModel() {
        return models.get(0);
    }

    /**
     * Return the underlying models. Multiple models can only be created by a subclass and will not count for e.g. estimating durations for this action.
     */
    protected List<Model> getModels() {
        return models;
    }

    /**
     * Set the underlying model.
     */
    protected void setModel(Model model) {
        this.models.clear();
        this.models.add(model);
    }

    /**
     * Called once the action is executed.
     * Makes sure that all variables are instantiated in the models.
     */
    @Override
    public void instantiateModelVariables(Environment environment) {
        if (models == null) {
            return;
        }
        for (Model model : models) {
            if (getLocationVariable() != null) {
                model.setLocationAndEndTime(getLocationVariable().getValue());
            }
            if (getActorVariable() != null) {
                model.setActors(getActorVariable().getValue());
            }
            if (getTargetVariable() != null) {
                model.setTargets(getTargetVariable().getValue());
            }
            model.setEnvironment(environment);
        }
    }

    /**
     * Return the semantic superclasses. (For example, a policeman is also a first responder.)
     */
    public final Set<Class<? extends Action>> getSemanticSuperclasses() {
        return superclasses;
    }

    /**
     * Convenience method for implementations of the action class to deliver superclasses. Return an empty array or even
     * null if there are no superclasses.
     */
    protected abstract Class<? extends Action>[] createSemanticSuperclassArray();

    @Override
    protected void addOwnParameters(Map<ParameterId, Variable> parameterMap) {
        // super.addOwnParameters(parameterMap); // Is abstract.
    }

    /**
     * To string.
     */
    @Override
    public String toString() {
        return actionVerb; // + "(" + id + ")";
        // [" +
        //        ( // (model != null ? " Model[" + model.getClass().getSimpleName() + "]" : "") +
        //                (getActorVariable() != null ? " [" + getActorVariable() + "]" : "") +
        //                        (getTargetVariable() != null ? " -> [" + getTargetVariable() + "]" : "") +
        //                        (" at/to [" + getLocationVariable() + "]")).trim() +
        //        "]";
    }
}
