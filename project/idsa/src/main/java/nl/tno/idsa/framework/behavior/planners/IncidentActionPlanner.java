package nl.tno.idsa.framework.behavior.planners;

import nl.tno.idsa.framework.behavior.incidents.Incident;
import nl.tno.idsa.framework.behavior.incidents.PlannedIncident;
import nl.tno.idsa.framework.behavior.plans.ActionPlan;
import nl.tno.idsa.framework.messaging.Messenger;
import nl.tno.idsa.framework.semantics_base.SemanticLibrary;
import nl.tno.idsa.framework.semantics_base.relations.SemanticRelation;
import nl.tno.idsa.framework.semantics_impl.actions.Action;
import nl.tno.idsa.framework.semantics_impl.relations.ActionEnablesRole;
import nl.tno.idsa.framework.semantics_impl.relations.RoleEnablesAction;
import nl.tno.idsa.framework.semantics_impl.roles.Role;
import nl.tno.idsa.framework.semantics_impl.variables.GroupVariable;
import nl.tno.idsa.framework.semantics_impl.variables.VariableBinder;
import nl.tno.idsa.framework.utils.RandomNumber;
import nl.tno.idsa.framework.utils.TextUtils;
import nl.tno.idsa.framework.world.Environment;
import nl.tno.idsa.library.actions.MoveTo;
import nl.tno.idsa.library.roles.Civilian;
import nl.tno.idsa.tools.DebugPrinter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Planning an incident by determining a sequence of actions for all agents involved. Agents and locations are kept
 * as variables, which are later instantiated by the agent and location sampler.
 */
public class IncidentActionPlanner {

    public static IncidentActionPlanner getInstance() {
        return instance;
    }

    private static IncidentActionPlanner instance = new IncidentActionPlanner();

    private IncidentActionPlanner() {
    }

    public PlannedIncident planIncidentActions(Environment environment, Incident incident) {

        // Start the plan.
        ActionPlan actionPlan = new ActionPlan(environment, incident.getBinder());
        actionPlan.setGoalAction(incident.getEnablingAction());
        DebugPrinter.println("Target action %s", incident.getEnablingAction());

        // First, create a plan that brings the groups involved in the right role or sequence of roles.
        resolveRolePreconditions(actionPlan, incident.getEnablingAction(), incident.getBinder());

        // Second, insert appropriate move actions between the various role actions.
        boolean success = resolveMovesBetweenLocations(actionPlan, incident.getEnablingAction(), incident.getBinder());
        if (!success) {
            return null; // Plan failed.
        }

        DebugPrinter.println("%Incident plan%n----------%n%s", actionPlan);
        Messenger.broadcast(actionPlan.toString());

        // And return.
        return new PlannedIncident(incident, actionPlan);
    }

    // TODO For now, in case there are multiple options to realize a plan, we choose randomly. ...
    // We do not remember what we tried before, nor look at suitability of the plan.
    private void resolveRolePreconditions(ActionPlan actionPlan, Action action, VariableBinder binder) {
        DebugPrinter.println("Resolve actor role preconditions for action %s and role %s", action, action.getActorRequiredRole());
        addActionSatisfyingRolePrecondition(action.getActorRequiredRole(), action.getActorVariable(), actionPlan, action, binder);
        DebugPrinter.println("Resolve target role preconditions for action %s and role %s", action, action.getTargetRequiredRole());
        addActionSatisfyingRolePrecondition(action.getTargetRequiredRole(), action.getTargetVariable(), actionPlan, action, binder);
    }

    private void addActionSatisfyingRolePrecondition(Class<? extends Role> requiredRole, GroupVariable associatedGroupVariable, ActionPlan actionPlan, Action action, VariableBinder binder) {

        // Do we need to satisfy a certain role?
        if (requiredRole != null) {
            DebugPrinter.println("Resolving required role %s for action %s.", requiredRole, action);

            // Make sure we satisfy the most specific role.
            GroupVariable mostSpecificVariable = (GroupVariable) binder.getMostSpecificVariable(associatedGroupVariable);
            if (mostSpecificVariable != null) {
                associatedGroupVariable = mostSpecificVariable;
                if (associatedGroupVariable.getMemberRole() != null) {
                    if (!requiredRole.equals(associatedGroupVariable.getMemberRole())) {
                        if (SemanticLibrary.getInstance().isSemanticSubclass(requiredRole, associatedGroupVariable.getMemberRole())) {
                            requiredRole = associatedGroupVariable.getMemberRole();
                            DebugPrinter.println("We are asked for a more specific role, namely %s.", requiredRole);
                        }
                    }
                }
            }

            // Find an action that can realize the role.
            Action actionToFulfillRolePrecondition = null;
            boolean actionGivesActorThisRole = false; // Alternatively, it gives the target this role.
            Set<EnablingAction> enablingActions = getEnablingActions(requiredRole);
            DebugPrinter.println("Enabling actions for role: %s.", enablingActions);

            // TODO Generate a more graceful error if we cannot find an enabling action.
            if (enablingActions == null || enablingActions.size() == 0) {
                throw new Error(requiredRole.getSimpleName() + " cannot be realized; no actions found that enable this role.");
            }

            // TODO Conversion Set->List is very inefficient.
            ArrayList<EnablingAction> enablingActionsArray = new ArrayList<EnablingAction>(enablingActions);

            // We try a few times just in case someone threw in a badly programmed action that cannot be instantiated.
            // Generally we can assume that the while loop has to be executed only once.
            // TODO Change this so that getEnablingActions(...) only returns valid actions.
            int attempts = 0;
            while (actionToFulfillRolePrecondition == null && attempts++ < 10) {
                try {
                    EnablingAction enablingAction = enablingActionsArray.get(RandomNumber.nextInt(enablingActionsArray.size()));
                    actionToFulfillRolePrecondition = createNonAbstractInstance(enablingAction.getEnablingAction());
                    actionGivesActorThisRole = enablingAction.enablesActor();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            DebugPrinter.println("Enabling action chosen for role: %s. This action enables it's actor: %s.", actionToFulfillRolePrecondition, actionGivesActorThisRole);

            // Find a postcondition of this action that realizes the role.
            if (actionToFulfillRolePrecondition != null) {
                if (actionGivesActorThisRole) {
                    binder.bind(actionToFulfillRolePrecondition.getActorVariable(), associatedGroupVariable);
                } else {
                    binder.bind(actionToFulfillRolePrecondition.getTargetVariable(), associatedGroupVariable);
                }
            }

            // TODO Generate a more graceful error if we cannot plan for a precondition.
            else {
                throw new Error("Cannot make plan. No action found to realize role " + requiredRole + ".");
            }

            // Add the action to the plan.
            actionPlan.addActionBefore(actionToFulfillRolePrecondition, action, actionGivesActorThisRole);
            DebugPrinter.println("Add action %s before %s. This action serves the actor? %s.", actionToFulfillRolePrecondition, action, actionGivesActorThisRole);

            // If needed, add target dependency.
            if (action.getTargetVariable() != null && action.getTargetVariable().isBoundTo(actionToFulfillRolePrecondition.getActorVariable())) {
                actionPlan.addTargetDependency(action, actionToFulfillRolePrecondition);
                DebugPrinter.println("Added action causes a target dependency. Target variable %s of %s is bound to actor variable %s of %s.", action.getTargetVariable(), action, actionToFulfillRolePrecondition.getActorVariable(), actionToFulfillRolePrecondition);
            } else if (action.getTargetVariable() != null && action.getTargetVariable().isBoundTo(actionToFulfillRolePrecondition.getTargetVariable())) {
                actionPlan.addTargetDependency(action, actionToFulfillRolePrecondition);
                DebugPrinter.println("Added action causes a target dependency. Target variable %s of %s is bound to target variable %s of %s.", action.getTargetVariable(), action, actionToFulfillRolePrecondition.getTargetVariable(), actionToFulfillRolePrecondition);
            }

            // Remove the role constraint for the earlier part of the plan. Otherwise, we cannot create a plan
            // with a sequence of roles, or we think we need to satisfy the role immediately. This is not the case:
            // the action created the role, and that's enough.
            // TODO This might break if we create a plan where an agent needs to obtain a role, then obtain another role ...
            // and finally obtain the first role again. But this would be quite a complex plan.
            associatedGroupVariable.removeRoleConstraint(requiredRole);
            DebugPrinter.println("Removed role constraint %s for group %s.", requiredRole, associatedGroupVariable);

            // Recursion for the action to fulfill...
            resolveRolePreconditions(actionPlan, actionToFulfillRolePrecondition, binder);

            // TODO Check whether the action happens to resolve any other preconditions. ...
            // If so, we should somehow remove them from the role/location preconditions to satisfy.
        } else {
            DebugPrinter.println("No role defined, so no actions needed.");
        }
    }

    private Action createNonAbstractInstance(Class<? extends Action> actionClass) {
        if (!Action.class.isAssignableFrom(actionClass)) { // The action class is abstract.
            ArrayList<Action> validActions = new ArrayList<>();
            Set<Class<? extends Action>> subclasses = SemanticLibrary.getInstance().listSemanticSubclasses(actionClass);
            for (Class<? extends Action> subclass : subclasses) {
                if (Action.class.isAssignableFrom(subclass)) {
                    try {
                        validActions.add(SemanticLibrary.getInstance().createSemanticInstance(subclass));
                    } catch (InstantiationException e) {
                        // Ok, not a valid action.
                    }
                }
            }
            if (validActions.size() > 0) {
                return validActions.get((int) (Math.random() * validActions.size()));
            }
        } else { // The action class is not abstract.
            try {
                return SemanticLibrary.getInstance().createSemanticInstance(actionClass);
            } catch (InstantiationException e) {
                // Ok, not a valid action.
            }
        }
        return null;
    }

    private boolean resolveMovesBetweenLocations(ActionPlan actionPlan, Action action, VariableBinder binder) {

        DebugPrinter.println("Resolve moves towards action %s.", action);

        // Check whether the action spawns agents. If so, we need to find a suitable location for that.
        boolean spawning = false;
        if (action.providesAgents() == Action.ProvidesAgents.YES) {
            binder.addVariable(action.getLocationVariable());
            if (action.getActorVariable() != null) {
                action.getActorVariable().setAgentsAreProvided(true);
                spawning = true;
                DebugPrinter.println("Action %s spawns or finds actors. Done moving.", action);
            }
            if (action.getTargetVariable() != null) {
                action.getTargetVariable().setAgentsAreProvided(true);
                spawning = true;
                DebugPrinter.println("Action %s spawns or finds targets. Done moving.", action);
            }
        }
        if (spawning) {
            return true;
        }

        // Check whether the action allows moving towards it. If it does not AND does not spawn agents,
        // we need to locate this action at the same location as an agent.
        if (action.allowsInsertMoveActionBefore() == Action.AllowsInsertMoveActionBefore.NO) {
            DebugPrinter.println("Action %s does not allow a move action before. It needs to happen at a place where an agent is.", action);
            action.getLocationVariable().restrictToAgentLocations(action.getActorVariable()); // TODO We skip the target here... This might create unexpected results.
            return true;
        }

        // Insert a suitable move.
        ArrayList<Action> actionsDirectlyBefore = actionPlan.getActionsDirectlyBefore(action);
        DebugPrinter.println("Action %s has actions before: %s.", action, actionsDirectlyBefore);

        // If the action is the first one in the plan branch, move actor and target towards the action in the plan.
        if (actionsDirectlyBefore == null || actionsDirectlyBefore.size() == 0) {
            DebugPrinter.println("Action %s is the first action in the plan. Move towards it if needed.", action);

            // If the action is a move action itself, it does not need moving towards it.
            if (action.movesActor()) {
                DebugPrinter.println("Action %s will move the actor itself.", action);
                return true;
            }

            // TODO Think; we assume the first move in an incident plan can be an ordinary MoveTo. ...
            // This assumes no agents in the world have roles at the moment we start planning.
            // The planner cannot take this into account, so if we want to facilitate this, we need to think how.
            try {
                addMoveBetween(actionPlan, action, true, null, binder, SemanticLibrary.getInstance().createSemanticInstance(MoveTo.class));  // Null: no action before the move.
                addMoveBetween(actionPlan, action, false, null, binder, SemanticLibrary.getInstance().createSemanticInstance(MoveTo.class)); // Null: no action before the move.
            } catch (Exception e) {
                // This should not happen, MoveTo must be instantiable.
                System.err.println("Serious error: MoveTo not instantiable!");
                return false;
            }

            return true;
        }

        // Insert moves between this action and the action(s) directly before.
        boolean targetHandled = false;
        for (Action actionBefore : actionsDirectlyBefore) {
            DebugPrinter.println("Resolve moves between action %s and the action directly before, %s.", action, actionBefore);
            boolean success = resolveMoveBetween(actionPlan, action, actionBefore, binder);
            if (!success) {
                return false; // Could not resolve a move.
            }
            success = resolveMovesBetweenLocations(actionPlan, actionBefore, binder);  // Recursive call.
            if (!success) {
                return false; // Could not resolve a move deeper in the plan tree.
            }
            if (action.getTargetVariable() != null) {
                targetHandled |=
                        binder.areVariablesBound(action.getTargetVariable(), actionBefore.getActorVariable()) ||
                                binder.areVariablesBound(action.getTargetVariable(), actionBefore.getTargetVariable());
            }
        }

        // Check whether we might have skipped moving the target.
        if (action.getTargetVariable() != null && !targetHandled) {
            DebugPrinter.println("Action %s is the first action in the plan for the target. Move towards it if needed.", action);
            addMoveBetween(actionPlan, action, false, null, binder, new MoveTo()); // Null: no action before the move.
        }

        return true;
    }

    private boolean resolveMoveBetween(ActionPlan actionPlan, Action actionTo, Action actionFrom, VariableBinder binder) {
        if (binder.areVariablesBound(actionTo.getActorVariable(), actionFrom.getActorVariable())) {
            DebugPrinter.println("Actor of %s is also actor of %s.", actionTo, actionFrom);
            return addActorMoveBetween(actionPlan, actionTo, actionFrom, true, binder);
        } else if (binder.areVariablesBound(actionTo.getTargetVariable(), actionFrom.getTargetVariable())) {
            DebugPrinter.println("Target of %s is also target of %s.", actionTo, actionFrom);
            return addTargetMoveBetween(actionPlan, actionTo, actionFrom, true, binder);
        } else if (binder.areVariablesBound(actionTo.getActorVariable(), actionFrom.getTargetVariable())) {
            DebugPrinter.println("Actor of %s is target of %s.", actionTo, actionFrom);
            return addActorMoveBetween(actionPlan, actionTo, actionFrom, false, binder);
        } else if (binder.areVariablesBound(actionTo.getTargetVariable(), actionFrom.getActorVariable())) {
            DebugPrinter.println("Target of %s is actor of %s.", actionTo, actionFrom);
            return addTargetMoveBetween(actionPlan, actionTo, actionFrom, false, binder);
        }
        return false;
    }

    private boolean addActorMoveBetween(ActionPlan actionPlan, Action actionTo, Action actionFrom, boolean moveActorIsAlsoActorInActionFrom, VariableBinder binder) {

        // Find move actions that are relevant for the role of the actor.
        Class<? extends Role> actorRoleWhileMoving = moveActorIsAlsoActorInActionFrom
                ? actionPlan.getActorRoleDirectlyAfter(actionFrom)
                : actionPlan.getTargetRoleDirectlyAfter(actionFrom);
        DebugPrinter.println("Actor has a role while moving: %s.", actorRoleWhileMoving);

        ArrayList<Action> suitableActions = findMoveActionsForRole(actorRoleWhileMoving);
        DebugPrinter.println("Suitable move actions: %s.", TextUtils.classNamesToString(suitableActions));

        Action chosenAction = null;

        // If the action after the move requires a target role, try to insert a move action for which the target role
        // approaches this role as much as possible. Example; if we want to arrest a thief, we can choose a move action that has
        // the thief as target, but not e.g. a shoplifter. On the other hand, we prefer a move action that has
        // the thief as target over a move action that has an offender (i.e. superclass of thief), and that over a move action
        // that has no target required role at all. This way, we can intelligently plan moving behavior tailored to the action
        // that comes after. For instance, the police can chase a thief toward the arrest location.
        Class<? extends Role> targetRequiredRole = actionTo.getTargetRequiredRole();
        if (targetRequiredRole != null) {
            DebugPrinter.println("Action we move to (%s) has a target with required role %s, try to find a matching move action.", actionTo.getClass(), targetRequiredRole);
            int minDist = Integer.MAX_VALUE;
            for (Action moveCandidate : suitableActions) {
                int inheritanceDistance = Integer.MAX_VALUE - 1;
                if (moveCandidate.getTargetRequiredRole() != null) {
                    inheritanceDistance = SemanticLibrary.getInstance().getInheritanceDistance(moveCandidate.getTargetRequiredRole(), targetRequiredRole);
                }
                DebugPrinter.println("Candidate move action (%s) requires role %s, which is %s steps from the role %s.", moveCandidate.getClass(), moveCandidate.getTargetRequiredRole(), inheritanceDistance, targetRequiredRole);
                if (chosenAction == null || inheritanceDistance < minDist) {
                    minDist = inheritanceDistance;
                    chosenAction = moveCandidate;
                }
            }
            if (chosenAction == null) {
                System.err.println("There is no move action with a target that can be inserted before " + actionTo.getClass() + ".");
                return false;
            }
            if (chosenAction.getTargetVariable() != null) {
                binder.bind(actionTo.getTargetVariable(), chosenAction.getTargetVariable());  // Make sure we know we're talking about the same agents.
            }
            DebugPrinter.println("Chose the best match for the move action: %s.", chosenAction);
        } else {
            Collections.shuffle(suitableActions, RandomNumber.getRandom());
            for (Action moveCandidate : suitableActions) {
                if (moveCandidate.getTargetRoleAfter() == null) {
                    chosenAction = moveCandidate;
                    break;
                }
            }
            if (chosenAction == null) {
                System.err.println("There is no move action without a target that can be inserted before " + actionTo.getClass() + ".");
                return false;
            }
            DebugPrinter.println("Chose a random move action (with no target): %s.", chosenAction);
        }

        // Add the move.
        addMoveBetween(actionPlan, actionTo, true, actionFrom, binder, chosenAction);
        return true;
    }

    private boolean addTargetMoveBetween(ActionPlan actionPlan, Action actionTo, Action actionFrom, boolean isAlsoTargetInActionFrom, VariableBinder binder) {

        // Find move actions that are relevant for the role of the target.
        Class<? extends Role> targetRoleWhileMoving = (isAlsoTargetInActionFrom
                ? actionPlan.getTargetRoleDirectlyAfter(actionFrom)
                : actionPlan.getActorRoleDirectlyAfter(actionFrom));
        DebugPrinter.println("Target has a role while moving: %s.", targetRoleWhileMoving);

        ArrayList<Action> suitableActions = findMoveActionsForRole(targetRoleWhileMoving);
        DebugPrinter.println("Suitable move actions: %s.", TextUtils.classNamesToString(suitableActions));

        Action chosenAction = null;
        Collections.shuffle(suitableActions, RandomNumber.getRandom());
        for (Action moveCandidate : suitableActions) {
            if (moveCandidate.getTargetRoleAfter() == null) {
                chosenAction = moveCandidate;
                break;
            }
        }
        if (chosenAction == null) {
            System.err.println("There is no move action without a target that can be inserted before " + actionTo.getClass() + ".");
            return false;
        }

        DebugPrinter.println("Chose a random move action (with no target): %s.", chosenAction);
        addMoveBetween(actionPlan, actionTo, false, actionFrom, binder, chosenAction);
        return true;
    }

    private void addMoveBetween(ActionPlan actionPlan, Action actionTo, boolean forActorOfActionTo,
                                Action actionFrom, VariableBinder binder, Action actionToMoveWith) {

        GroupVariable variable = (forActorOfActionTo ? actionTo.getActorVariable() : actionTo.getTargetVariable());

        if (variable != null) {
            DebugPrinter.println("Group %s must move from %s to %s using action %s.", variable, actionFrom, actionTo, actionToMoveWith);
            binder.bind(actionToMoveWith.getActorVariable(), variable);
            binder.bind(actionToMoveWith.getLocationVariable(), actionTo.getLocationVariable());
            if (actionFrom == null) {
                boolean linkServesActor = forActorOfActionTo;
                actionPlan.addActionBefore(actionToMoveWith, actionTo, linkServesActor);
            } else {
                boolean linkAfterServesActor = forActorOfActionTo;
                boolean linkBeforeServesActor = true;
                actionPlan.addActionBetween(actionToMoveWith, actionTo, actionFrom, linkAfterServesActor, linkBeforeServesActor);
            }
            resolveRolePreconditionsForMove(actionPlan, actionTo, actionFrom, binder, actionToMoveWith);
        }
    }

    private ArrayList<Action> findMoveActionsForRole(Class<? extends Role> actorRoleWhileMoving) {
        ArrayList<Action> suitableActions = new ArrayList<>();
        Set<Class<? extends Action>> availableActions = getActionsForRole(actorRoleWhileMoving);
        for (Class<? extends Action> actionClass : availableActions) {
            try {
                // Examine whether this role action can move the actor toward the desired location.
                Action actionInstance = SemanticLibrary.getInstance().createSemanticInstance(actionClass);
                if (actionInstance != null) {
                    if (actionInstance.movesActor()) {
                        // TODO We don't check constraints on location variables when chaining them.
                        // (Also see LocationVariable.checkValidity(...) by the way.)
                        suitableActions.add(actionInstance);
                    }
                }
                // TODO Think; perhaps we can think of situations where an action moves a target and not (only) an actor.
                // If so, we need a target location after action in the Action class, and corresponding logic here.
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return suitableActions;
    }

    private void resolveRolePreconditionsForMove(ActionPlan actionPlan, Action actionTo, Action actionFrom, VariableBinder binder, Action actionToMoveWith) {

        Class<? extends Role> actorRequiredRole = actionToMoveWith.getActorRequiredRole();
        if (actorRequiredRole != null && !actorRequiredRole.equals(Civilian.class)) {
            resolveRolePreconditionForMove(actionPlan, actionTo, actionFrom, binder, actionToMoveWith, actorRequiredRole);
        }

        Class<? extends Role> targetRequiredRole = actionToMoveWith.getTargetRequiredRole();
        if (targetRequiredRole != null && !targetRequiredRole.equals(Civilian.class)) {
            resolveRolePreconditionForMove(actionPlan, actionTo, actionFrom, binder, actionToMoveWith, targetRequiredRole);
        }
    }

    private void resolveRolePreconditionForMove(ActionPlan actionPlan, Action actionTo, Action actionFrom,
                                                VariableBinder binder, Action actionToMoveWith, Class<? extends Role> requiredRole) {
        if (SemanticLibrary.getInstance().isSemanticSubclass(requiredRole, actionPlan.getActorRoleDirectlyAfter(actionFrom))) {
            DebugPrinter.println("Role %s is required and provided by %s.", requiredRole, actionFrom);
            actionPlan.copyTargetDependencies(actionFrom, actionToMoveWith);
        } else if (SemanticLibrary.getInstance().isSemanticSubclass(requiredRole, actionTo.getActorRequiredRole())) {
            DebugPrinter.println("Role %s is required and action %s requires a resolved subclass for actor: %s.", requiredRole, actionTo, actionTo.getActorRequiredRole());
            actionPlan.copyTargetDependencies(actionTo, actionToMoveWith);
        } else if (SemanticLibrary.getInstance().isSemanticSubclass(requiredRole, actionTo.getTargetRequiredRole())) {
            DebugPrinter.println("Role %s is required and action %s requires a resolved subclass for target: %s.", requiredRole, actionTo, actionTo.getTargetRequiredRole());
            actionPlan.copyTargetDependencies(actionTo, actionToMoveWith);
        } else if (SemanticLibrary.getInstance().isSemanticSuperclass(requiredRole, actionTo.getActorRequiredRole())) {
            // TODO: We don't support the situation where a move action requires a more specific role than the action after it. ...
            // We need to 1) remove the part of the plan addressed to realizing the less specific role for the
            // action after, and 2) plan for realizing the role for the move action.
            throw new Error(String.format("Role %s is required and action %s requires a superclass for actor, which is a problem: %s.", requiredRole, actionTo, actionTo.getActorRequiredRole()));
        } else if (SemanticLibrary.getInstance().isSemanticSuperclass(requiredRole, actionTo.getTargetRequiredRole())) {
            throw new Error(String.format("Role %s is required and action %s requires a superclass for target, which is a problem: %s.", requiredRole, actionTo, actionTo.getActorRequiredRole()));
            // TODO: See above.
        } else {
            // The role needs separate realization.
            DebugPrinter.println("Role %s is required and no other action in the plan realizes it, so recursion is needed.", requiredRole);
            resolveRolePreconditions(actionPlan, actionToMoveWith, binder);
        }
    }

    public static Set<EnablingAction> getEnablingActions(Class<? extends Role> roleClass) {
        Set<SemanticRelation<Action, Role>> relations = SemanticLibrary.getInstance().getSemanticRelationsTo(ActionEnablesRole.class, roleClass);
        HashSet<EnablingAction> enablingActions = new HashSet<>();
        for (SemanticRelation<Action, Role> relation : relations) {
            ActionEnablesRole actionEnablesRole = (ActionEnablesRole) relation;
            EnablingAction enablingAction = new EnablingAction(actionEnablesRole.getEnabler(), actionEnablesRole.doesActionGiveActorTheRole());
            enablingActions.add(enablingAction);
        }
        return enablingActions;
    }

    public static Set<Class<? extends Action>> getActionsForRole(Class<? extends Role> roleClass) {
        Set<SemanticRelation<Role, Action>> relations = SemanticLibrary.getInstance().getSemanticRelationsFrom(roleClass, RoleEnablesAction.class);
        HashSet<Class<? extends Action>> roleActions = new HashSet<>();
        for (SemanticRelation<Role, Action> relation : relations) {
            RoleEnablesAction roleEnablesAction = (RoleEnablesAction) relation;
            if (roleEnablesAction.isRoleRequiredFromActor()) {
                roleActions.add(relation.getTo());
            }
        }
        return roleActions;
    }

    public static class EnablingAction {
        Class<? extends Action> enablingAction;
        boolean enablesActor; // or target?

        public EnablingAction(Class<? extends Action> enablingAction, boolean enablesActor) {
            this.enablingAction = enablingAction;
            this.enablesActor = enablesActor;
        }

        public Class<? extends Action> getEnablingAction() {
            return enablingAction;
        }

        public boolean enablesActor() {
            return enablesActor;
        }

        public boolean enablesTarget() {
            return !enablesActor;
        }

        public String toString() {
            return enablingAction.getSimpleName() + (enablesActor ? "(a)" : "(t)");
        }
    }
}
