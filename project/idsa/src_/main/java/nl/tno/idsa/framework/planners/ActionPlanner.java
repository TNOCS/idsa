package nl.tno.idsa.framework.planners;

import nl.tno.idsa.framework.behavior.incidents.Incident;
import nl.tno.idsa.framework.messaging.Messenger;
import nl.tno.idsa.framework.semantics_base.SemanticLibrary;
import nl.tno.idsa.framework.semantics_impl.actions.Action;
import nl.tno.idsa.framework.semantics_impl.roles.Role;
import nl.tno.idsa.framework.semantics_impl.variables.GroupVariable;
import nl.tno.idsa.framework.semantics_impl.variables.VariableBinder;
import nl.tno.idsa.framework.utils.RandomNumber;
import nl.tno.idsa.framework.utils.TextUtils;
import nl.tno.idsa.framework.world.Environment;
import nl.tno.idsa.library.actions.MoveTo;
import nl.tno.idsa.library.roles.Civilian;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Set;

/**
 * Created by jongsd on 7-8-15.
 */

// TODO Document class.

public class ActionPlanner {

    // DEBUG FROM HERE ...

    private static final boolean PRINT_DEBUG = true; // TODO Debug logging facility instead of this.

    private static void p(String s) {
        if (PRINT_DEBUG) {
            System.out.println(s);
        }
    }

    private static void f(String s, Object... vars) {
        for (int i = 0; i < vars.length; i++) {
            if (vars[i] instanceof Class) {
                vars[i] = ((Class) vars[i]).getSimpleName();
            }
        }
        if (PRINT_DEBUG) {
            System.out.println(String.format(s, vars));
        }
    }

    // ... UNTIL HERE

    public static ActionPlanner getInstance() {
        return instance;
    }

    private static ActionPlanner instance = new ActionPlanner();

    private ActionPlanner() {
    }

    public ActionPlan createPlan(Environment environment, Incident incident)
            throws Exception {

        // Start the plan.
        ActionPlan actionPlan = new ActionPlan(environment, incident.getBinder());
        actionPlan.setGoalAction(incident.getEnablingAction());
        f("Target action %s", incident.getEnablingAction());

        // First, create a plan that brings the groups involved in the right role or sequence of roles.
        resolveRolePreconditions(actionPlan, incident.getEnablingAction(), incident.getBinder());

        // Second, insert appropriate move actions between the various role actions.
        resolveMovesBetweenLocations(actionPlan, incident.getEnablingAction(), incident.getBinder());

        f("%nEvent plan%n----------%n%s", actionPlan);
        Messenger.broadcast(actionPlan.toString());

        // And return.
        incident.setActionPlan(actionPlan);
        return actionPlan;
    }

    // TODO Think; for now, in case there are multiple options to realize a plan, we choose randomly. ...
    // We do not remember what we tried before, nor look at suitability of the plan.
    private void resolveRolePreconditions(ActionPlan actionPlan, Action action, VariableBinder binder)
            throws Exception {
        f("Resolve actor role preconditions for action %s and role %s", action, action.getActorRequiredRole());
        addActionSatisfyingRolePrecondition(action.getActorRequiredRole(), action.getActorVariable(), actionPlan, action, binder);
        f("Resolve target role preconditions for action %s and role %s", action, action.getTargetRequiredRole());
        addActionSatisfyingRolePrecondition(action.getTargetRequiredRole(), action.getTargetVariable(), actionPlan, action, binder);
    }

    private void addActionSatisfyingRolePrecondition(Class<? extends Role> requiredRole, GroupVariable associatedGroupVariable, ActionPlan actionPlan, Action action, VariableBinder binder)
            throws Exception {

        // Do we need to satisfy a certain role?
        if (requiredRole != null) {

            f("Resolving required role %s for action %s.", requiredRole, action);

            // Make sure we satisfy the most specific role.
            GroupVariable mostSpecificVariable = (GroupVariable) binder.getMostSpecificVariable(associatedGroupVariable);
            if (mostSpecificVariable != null) {
                associatedGroupVariable = mostSpecificVariable;
                if (associatedGroupVariable.getMemberRole() != null) {
                    if (!requiredRole.equals(associatedGroupVariable.getMemberRole())) {
                        if (SemanticLibrary.getInstance().isSemanticSubclass(requiredRole, associatedGroupVariable.getMemberRole())) {
                            requiredRole = associatedGroupVariable.getMemberRole();
                            f("We are asked for a more specific role, namely %s.", requiredRole);
                        }
                    }
                }
            }

            // Find an action that can realize the role.
            Action actionToFulfillRolePrecondition = null;
            boolean actionGivesActorThisRole = false; // Alternatively, it gives the target this role.
            Set<RoleActionLookup.EnablingAction> enablingActions = RoleActionLookup.getInstance().getEnablingActions(requiredRole);
            f("Enabling actions for role: %s.", enablingActions);
            if (enablingActions == null || enablingActions.size() == 0) {
                throw new Exception(requiredRole.getSimpleName() + " cannot be realized; no actions found that enable this role.");
            }

            // TODO Improve; conversion Set->List is very inefficient.
            ArrayList<RoleActionLookup.EnablingAction> enablingActionsArray = new ArrayList<RoleActionLookup.EnablingAction>(enablingActions);

            // We try a few times just in case someone threw in a badly programmed action that cannot be instantiated.
            // Generally we can assume that the while loop has to be executed only once.
            // TODO Change this so that RoleActionLookup.getInstance().getEnablingActions above only returns valid actions.
            int attempts = 0;
            while (actionToFulfillRolePrecondition == null && attempts++ < 10) {
                try {
                    RoleActionLookup.EnablingAction enablingAction = enablingActionsArray.get(RandomNumber.nextInt(enablingActionsArray.size()));
                    actionToFulfillRolePrecondition = createNonAbstractInstance(enablingAction.getEnablingAction());
                    actionGivesActorThisRole = enablingAction.enablesActor();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            f("Enabling action chosen for role: %s. This action enables it's actor: %s.", actionToFulfillRolePrecondition, actionGivesActorThisRole);

            // Find a postcondition of this action that realizes the role.
            if (actionToFulfillRolePrecondition != null) {
                if (actionGivesActorThisRole) {
                    binder.bind(actionToFulfillRolePrecondition.getActorVariable(), associatedGroupVariable);
                } else {
                    binder.bind(actionToFulfillRolePrecondition.getTargetVariable(), associatedGroupVariable);
                }
            }

            // TODO Implement; generate a more graceful error if we cannot plan for a precondition.
            else {
                throw new Exception("Cannot make plan. No action found to realize role " + requiredRole + ".");
            }

            // Add the action to the plan.
            actionPlan.addActionBefore(actionToFulfillRolePrecondition, action, actionGivesActorThisRole);
            f("Add action %s before %s. This action serves the actor? %s.", actionToFulfillRolePrecondition, action, actionGivesActorThisRole);

            // If needed, add target dependency.
            if (action.getTargetVariable() != null && action.getTargetVariable().isBoundTo(actionToFulfillRolePrecondition.getActorVariable())) {
                actionPlan.addTargetDependency(action, actionToFulfillRolePrecondition);
                f("Added action causes a target dependency. Target variable %s of %s is bound to actor variable %s of %s.", action.getTargetVariable(), action, actionToFulfillRolePrecondition.getActorVariable(), actionToFulfillRolePrecondition);
            } else if (action.getTargetVariable() != null && action.getTargetVariable().isBoundTo(actionToFulfillRolePrecondition.getTargetVariable())) {
                actionPlan.addTargetDependency(action, actionToFulfillRolePrecondition);
                f("Added action causes a target dependency. Target variable %s of %s is bound to target variable %s of %s.", action.getTargetVariable(), action, actionToFulfillRolePrecondition.getTargetVariable(), actionToFulfillRolePrecondition);
            }

            // Remove the role constraint for the earlier part of the plan. Otherwise, we cannot create a plan
            // with a sequence of roles, or we think we need to satisfy the role immediately. This is not the case:
            // the action created the role, and that's enough.
            // TODO This might break if we create a plan where an agent needs to obtain a role, then obtain another role ...
            // and finally obtain the first role again. But this would be quite a complex plan.
            associatedGroupVariable.removeRoleConstraint(requiredRole);
            f("Removed role constraint %s for group %s.", requiredRole, associatedGroupVariable);

            // Recursion for the action to fulfill...
            resolveRolePreconditions(actionPlan, actionToFulfillRolePrecondition, binder);

            // TODO Implement; check whether the action happens to resolve any other preconditions. ...
            // If so, we should somehow remove them from the role/location preconditions to satisfy.
        } else {
            p("No role defined, so no actions needed.");
        }
    }

    private Action createNonAbstractInstance(Class<? extends Action> actionClass) {

        System.out.println("Create non-abstract instance of " + actionClass.getSimpleName());

        if (!Action.class.isAssignableFrom(actionClass)) { // While the action class is abstract.

            System.out.println("Class is abstract");

            ArrayList<Action> validActions = new ArrayList<>();
            Set<Class<? extends Action>> subclasses = SemanticLibrary.getInstance().listSemanticSubclasses(actionClass);
            for (Class<? extends Action> subclass : subclasses) {
                if (Action.class.isAssignableFrom(subclass)) {
                    try {
                        validActions.add((Action) SemanticLibrary.getInstance().createSemanticInstance(subclass));
                    } catch (InstantiationException e) {
                        // Ok, not a valid action.
                    }
                }
            }
            if (validActions.size() > 0) {

                System.out.println("Choose at random from " + validActions);

                Action action = validActions.get((int) (Math.random() * validActions.size()));

                System.out.println("Chose " + action);

                return action;
            }
        } else {

            System.out.println("Class is not abstract");

            try {
                Action action = (Action) SemanticLibrary.getInstance().createSemanticInstance(actionClass);

                System.out.println("Created " + action);

                return action;
            } catch (InstantiationException e) {
                // Ok, not a valid action.
            }
        }
        return null;
    }

    private void resolveMovesBetweenLocations(ActionPlan actionPlan, Action action, VariableBinder binder)
            throws Exception {

        f("Resolve moves towards action %s.", action);

        // Check whether the action spawns agents. If so, we need to find a suitable location for that.
        boolean spawning = false;
        if (action.providesAgents() == Action.ProvidesAgents.YES) {
            binder.addVariable(action.getLocationVariable());
            if (action.getActorVariable() != null) {
                action.getActorVariable().setAgentsAreProvided(true);
                spawning = true;
                f("Action %s spawns or finds actors. Done moving.", action);
            }
            if (action.getTargetVariable() != null) {
                action.getTargetVariable().setAgentsAreProvided(true);
                spawning = true;
                f("Action %s spawns or finds targets. Done moving.", action);
            }
        }
        if (spawning) {
            return;
        }

        // Check whether the action allows moving towards it. If it does not AND does not spawn agents,
        // we need to locate this action near an agent.
        // TODO This all seems rather dirty and complicated.
        if (action.allowsInsertMoveActionBefore() == Action.AllowsInsertMoveActionBefore.NO) {
            f("Action %s does not allow a move action before. It needs to happen at a place where an agent is.", action);
            action.getLocationVariable().restrictToAgentLocations(action.getActorVariable()); // TODO We skip the target here... also this might seem dirty.
            return;
        }

        // Insert a suitable move.
        ArrayList<Action> actionsDirectlyBefore = actionPlan.getActionsDirectlyBefore(action);
        f("Action %s has actions before: %s.", action, actionsDirectlyBefore);

        // If the action is the first one in the plan branch, move actor and target towards the action in the plan.
        if (actionsDirectlyBefore == null || actionsDirectlyBefore.size() == 0) {
            f("Action %s is the first action in the plan. Move towards it if needed.", action);

            // If the action is a move action itself, it does not need moving towards it.
            if (action.movesActor()) {
                f("Action %s will move the actor itself.", action);
                return;
            }

            // TODO Think; we assume this first move can be an ordinary MoveTo. ...
            // This assumes no agents in the world have roles at the moment we start planning.
            // The planner cannot take this into account, so if we want to facilitate this, we need to think how.
            addMoveBetween(actionPlan, action, true, null, binder, SemanticLibrary.getInstance().createSemanticInstance(MoveTo.class));  // Null: no action before the move.
            addMoveBetween(actionPlan, action, false, null, binder, SemanticLibrary.getInstance().createSemanticInstance(MoveTo.class)); // Null: no action before the move.

            return;
        }

        // Insert moves between this action and the action(s) directly before.
        boolean targetHandled = false;
        for (Action actionBefore : actionsDirectlyBefore) {
            f("Resolve moves between action %s and the action directly before, %s.", action, actionBefore);
            resolveMoveBetween(actionPlan, action, actionBefore, binder);
            resolveMovesBetweenLocations(actionPlan, actionBefore, binder);  // Recursive call.
            if (action.getTargetVariable() != null) {
                targetHandled |=
                        binder.areVariablesBound(action.getTargetVariable(), actionBefore.getActorVariable()) ||
                                binder.areVariablesBound(action.getTargetVariable(), actionBefore.getTargetVariable());
            }
        }

        // Check whether we might have skipped moving the target.
        if (action.getTargetVariable() != null && !targetHandled) {
            f("Action %s is the first action in the plan for the target. Move towards it if needed.", action);
            addMoveBetween(actionPlan, action, false, null, binder, new MoveTo()); // Null: no action before the move.
        }
    }

    private void resolveMoveBetween(ActionPlan actionPlan, Action actionTo, Action actionFrom, VariableBinder binder)
            throws Exception {
        if (binder.areVariablesBound(actionTo.getActorVariable(), actionFrom.getActorVariable())) {
            f("Actor of %s is also actor of %s.", actionTo, actionFrom);
            addActorMoveBetween(actionPlan, actionTo, actionFrom, true, binder);
        } else if (binder.areVariablesBound(actionTo.getTargetVariable(), actionFrom.getTargetVariable())) {
            f("Target of %s is also target of %s.", actionTo, actionFrom);
            addTargetMoveBetween(actionPlan, actionTo, actionFrom, true, binder);
        } else if (binder.areVariablesBound(actionTo.getActorVariable(), actionFrom.getTargetVariable())) {
            f("Actor of %s is target of %s.", actionTo, actionFrom);
            addActorMoveBetween(actionPlan, actionTo, actionFrom, false, binder);
        } else if (binder.areVariablesBound(actionTo.getTargetVariable(), actionFrom.getActorVariable())) {
            f("Target of %s is actor of %s.", actionTo, actionFrom);
            addTargetMoveBetween(actionPlan, actionTo, actionFrom, false, binder);
        }
    }

    private void addActorMoveBetween(ActionPlan actionPlan, Action actionTo, Action actionFrom, boolean isAlsoActorInActionFrom, VariableBinder binder)
            throws Exception {

        // Find move actions that are relevant for the role of the actor.
        Class<? extends Role> actorRoleWhileMoving = isAlsoActorInActionFrom
                ? actionPlan.getActorRoleDirectlyAfter(actionFrom)
                : actionPlan.getTargetRoleDirectlyAfter(actionFrom);
        f("Actor has a role while moving: %s.", actorRoleWhileMoving);

        ArrayList<Action> suitableActions = findMoveActionsForRole(actorRoleWhileMoving);
        f("Suitable move actions: %s.", TextUtils.classNamesToString(suitableActions));

        Action chosenAction = null;

        // If the action after the move requires a target role, try to insert a move action for which the target role
        // approaches this role as much as possible. Example; if we want to arrest a thief, we can choose a move action that has
        // the thief as target, but not a specific thief, e.g. a shoplifter. On the other hand, we prefer a move action that has      // TODO but not a specific thief? what does that mean?
        // the thief as target over a move action that has an offender (i.e. superclass of thief), and that over a move action
        // that has no target required role at all. This way, we can intelligently plan moving behavior tailored to the action
        // that comes after. For instance, the police can chase a thief toward the arrest location.
        Class<? extends Role> targetRequiredRole = actionTo.getTargetRequiredRole();
        if (targetRequiredRole != null) {
            f("Action we move to (%s) has a target with required role %s, try to find a matching move action.", actionTo.getClass(), targetRequiredRole);
            int minDist = Integer.MAX_VALUE;
            for (Action moveCandidate : suitableActions) {
                int inheritanceDistance = Integer.MAX_VALUE - 1;
                if (moveCandidate.getTargetRequiredRole() != null) {
                    inheritanceDistance = SemanticLibrary.getInstance().getInheritanceDistance(moveCandidate.getTargetRequiredRole(), targetRequiredRole);
                }
                f("Candidate move action (%s) requires role %s, which is %s steps from the role %s.", moveCandidate.getClass(), moveCandidate.getTargetRequiredRole(), inheritanceDistance, targetRequiredRole);
                if (chosenAction == null || inheritanceDistance < minDist) {
                    minDist = inheritanceDistance;
                    chosenAction = moveCandidate;
                }
            }
            if (chosenAction == null) {
                throw new Exception("There is no move action with a target that can be inserted before " + actionTo.getClass() + ".");
            }
            if (chosenAction.getTargetVariable() != null) {
                binder.bind(actionTo.getTargetVariable(), chosenAction.getTargetVariable());  // Make sure we know we're talking about the same agents.
            }
            f("Chose the best match for the move action: %s.", chosenAction);
        } else {
            Collections.shuffle(suitableActions); // TODO Replace by RandomNumber... call.
            for (Action moveCandidate : suitableActions) {
                if (moveCandidate.getTargetRoleAfter() == null) {
                    chosenAction = moveCandidate;
                    break;
                }
            }
            if (chosenAction == null) {
                throw new Exception("There is no move action without a target that can be inserted before " + actionTo.getClass() + ".");
            }
            f("Chose a random move action (with no target): %s.", chosenAction);
        }

        // Add the move.
        addMoveBetween(actionPlan, actionTo, true, actionFrom, binder, chosenAction);
    }

    private void addTargetMoveBetween(ActionPlan actionPlan, Action actionTo, Action actionFrom, boolean isAlsoTargetInActionFrom, VariableBinder binder)
            throws Exception {

        // Find move actions that are relevant for the role of the target.
        Class<? extends Role> targetRoleWhileMoving = (isAlsoTargetInActionFrom
                ? actionPlan.getTargetRoleDirectlyAfter(actionFrom)
                : actionPlan.getActorRoleDirectlyAfter(actionFrom));
        f("Target has a role while moving: %s.", targetRoleWhileMoving);

        ArrayList<Action> suitableActions = findMoveActionsForRole(targetRoleWhileMoving);
        f("Suitable move actions: %s.", TextUtils.classNamesToString(suitableActions));

        Action chosenAction = null;
        Collections.shuffle(suitableActions); // TODO Replace by RandomNumber... call.
        for (Action moveCandidate : suitableActions) {
            if (moveCandidate.getTargetRoleAfter() == null) {
                chosenAction = moveCandidate;
                break;
            }
        }
        if (chosenAction == null) {
            throw new Error("There is no move action without a target that can be inserted before " + actionTo.getClass() + ".");
        }

        f("Chose a random move action (with no target): %s.", chosenAction);
        addMoveBetween(actionPlan, actionTo, false, actionFrom, binder, chosenAction);
    }

    private void addMoveBetween(ActionPlan actionPlan, Action actionTo, boolean forActorOfActionTo,
                                Action actionFrom, VariableBinder binder, Action actionToMoveWith)
            throws Exception {

        GroupVariable variable = (forActorOfActionTo ? actionTo.getActorVariable() : actionTo.getTargetVariable());

        if (variable != null) {
            f("Group %s must move from %s to %s using action %s.", variable, actionFrom, actionTo, actionToMoveWith);
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
        Set<Class<? extends Action>> availableActions = RoleActionLookup.getInstance().getActions(actorRoleWhileMoving);
        for (Class<? extends Action> actionClass : availableActions) {
            try {
                // Examine whether this role action can move the actor toward the desired location.
                Action actionInstance = SemanticLibrary.getInstance().createSemanticInstance(actionClass);
                if (actionInstance != null) {
                    if (actionInstance.movesActor()) {
                        // TODO IMPROVE! We don't check the constraints on location variables when chaining them.
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

    private void resolveRolePreconditionsForMove(ActionPlan actionPlan, Action actionTo, Action actionFrom, VariableBinder binder, Action actionToMoveWith)
            throws Exception {

        Class<? extends Role> actorRequiredRole = actionToMoveWith.getActorRequiredRole();
        if (actorRequiredRole != null && !actorRequiredRole.equals(Civilian.class)) {
            resolveRolePreconditionForMove(actionPlan, actionTo, actionFrom, binder, actionToMoveWith, actorRequiredRole);
        }

        Class<? extends Role> targetRequiredRole = actionToMoveWith.getTargetRequiredRole();
        if (targetRequiredRole != null && !targetRequiredRole.equals(Civilian.class)) {
            resolveRolePreconditionForMove(actionPlan, actionTo, actionFrom, binder, actionToMoveWith, targetRequiredRole);
        }
    }

    private void resolveRolePreconditionForMove(ActionPlan actionPlan, Action actionTo, Action actionFrom, VariableBinder binder, Action actionToMoveWith, Class<? extends Role> requiredRole) throws Exception {
        if (SemanticLibrary.getInstance().isSemanticSubclass(requiredRole, actionPlan.getActorRoleDirectlyAfter(actionFrom))) {
            f("Role %s is required and provided by %s.", requiredRole, actionFrom);
            actionPlan.copyTargetDependencies(actionFrom, actionToMoveWith);
        } else if (SemanticLibrary.getInstance().isSemanticSubclass(requiredRole, actionTo.getActorRequiredRole())) {
            f("Role %s is required and action %s requires a resolved subclass for actor: %s.", requiredRole, actionTo, actionTo.getActorRequiredRole());
            actionPlan.copyTargetDependencies(actionTo, actionToMoveWith);
        } else if (SemanticLibrary.getInstance().isSemanticSubclass(requiredRole, actionTo.getTargetRequiredRole())) {
            f("Role %s is required and action %s requires a resolved subclass for target: %s.", requiredRole, actionTo, actionTo.getTargetRequiredRole());
            actionPlan.copyTargetDependencies(actionTo, actionToMoveWith);
        } else if (SemanticLibrary.getInstance().isSemanticSuperclass(requiredRole, actionTo.getActorRequiredRole())) {
            f("Role %s is required and action %s requires a superclass for actor, which is a problem: %s.", requiredRole, actionTo, actionTo.getActorRequiredRole());
            System.err.println("UH OH");
            // TODO: if the move action requires a more specific role than the action after it ...
            // we need to 1) remove the part of the plan addressed to realizing the less specific role for the
            // action after, and 2) plan for realizing the role for the move action.
        } else if (SemanticLibrary.getInstance().isSemanticSuperclass(requiredRole, actionTo.getTargetRequiredRole())) {
            f("Role %s is required and action %s requires a superclass for target, which is a problem: %s.", requiredRole, actionTo, actionTo.getTargetRequiredRole());
            System.err.println("UH OH");
            // TODO: See above.
        } else {
            // The role needs separate realization.
            f("Role %s is required and no other action in the plan realizes it, so recursion is needed.", requiredRole);
            resolveRolePreconditions(actionPlan, actionToMoveWith, binder);
        }
    }
}
