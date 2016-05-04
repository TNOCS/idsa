package nl.tno.idsa.framework.behavior.plans;

import nl.tno.idsa.framework.agents.Agent;
import nl.tno.idsa.framework.behavior.models.Model;
import nl.tno.idsa.framework.semantics_impl.actions.Action;
import nl.tno.idsa.framework.semantics_impl.groups.Group;
import nl.tno.idsa.framework.semantics_impl.locations.LocationAndTime;
import nl.tno.idsa.framework.semantics_impl.roles.Role;
import nl.tno.idsa.framework.semantics_impl.variables.GroupVariable;
import nl.tno.idsa.framework.semantics_impl.variables.LocationVariable;
import nl.tno.idsa.framework.semantics_impl.variables.Variable;
import nl.tno.idsa.framework.semantics_impl.variables.VariableBinder;
import nl.tno.idsa.framework.utils.RandomNumber;
import nl.tno.idsa.framework.utils.TextUtils;
import nl.tno.idsa.framework.world.Environment;
import nl.tno.idsa.framework.world.IGeometry;

import java.util.*;

/**
 * Action plan.
 */
public class ActionPlan {

    private final Environment environment;
    private final VariableBinder binder;

    private Action goalAction;

    // TODO The ActionPlan does not support plan graphs, only trees, but the data structure does. ...
    // Is that enough? If so, this structure is probably overkill. We could just use a tree.
    private HashMap<Action, ArrayList<ActionConnector>> connectorsWithActionBefore = new HashMap<>();
    private HashMap<Action, ArrayList<ActionConnector>> connectorsWithActionAfter = new HashMap<>();

    private HashMap<Action, Set<Action>> targetDependencies = new HashMap<>();

    public ActionPlan(Environment environment, VariableBinder binder) {
        this.environment = environment;
        this.binder = binder;
    }

    public Environment getEnvironment() {
        return environment;
    }

    public Action getGoalAction() {
        return goalAction;
    }

    public void setGoalAction(Action goalAction) {
        this.goalAction = goalAction;
    }

    public void addActionBefore(Action newAction, Action actionDirectlyAfter, boolean linkServesActor) {
        addLink(newAction, actionDirectlyAfter, linkServesActor);
    }

    public void addActionBetween(Action newAction, Action actionDirectlyAfter, Action actionDirectlyBefore, boolean linkAfterServesActor, boolean linkBeforeServesActor) {
        removeLink(actionDirectlyBefore, actionDirectlyAfter);
        addLink(actionDirectlyBefore, newAction, linkBeforeServesActor);
        addLink(newAction, actionDirectlyAfter, linkAfterServesActor);
    }

    public void copyTargetDependencies(Action fromAction, Action toAction) {
        targetDependencies.put(toAction, targetDependencies.get(fromAction));
    }

    public void removeTargetDependencies(Action action) {
        targetDependencies.remove(action);
    }

    public void addTargetDependency(Action action, Action mustBeExecutedFirst) {
        // Dependencies other than simple sequence requirements only occur when the target of a certain
        // action needs to execute an action before that action.
        Set<Action> actions = targetDependencies.get(action);
        if (actions == null) {
            actions = new HashSet<>();
            targetDependencies.put(action, actions);
        }
        actions.add(mustBeExecutedFirst);
    }

    public Set<Action> getTargetDependencies(Action action) {
        return targetDependencies.get(action);
    }

    public ArrayList<Action> getActionsDirectlyBefore(Action action) {
        ArrayList<ActionConnector> acsBefore = connectorsWithActionAfter.get(action);
        if (acsBefore == null) {
            return null;
        }
        ArrayList<Action> actionsDirectlyBefore = new ArrayList<Action>();
        for (ActionConnector acBefore : acsBefore) {
            actionsDirectlyBefore.add(acBefore.getActionBefore());
        }
        return actionsDirectlyBefore;
    }

    public Class<? extends Role> getActorRoleDirectlyAfter(Action action) {
        ArrayList<ActionConnector> acsBefore = connectorsWithActionBefore.get(action);
        if (acsBefore == null) {
            return null;
        }
        for (ActionConnector acBefore : acsBefore) {
            if (acBefore.doesLinkServeActor()) {
                return acBefore.getRoleOfActionBeforeActor();
            }
        }
        return null;
    }

    public Class<? extends Role> getTargetRoleDirectlyAfter(Action action) {
        ArrayList<ActionConnector> acsBefore = connectorsWithActionBefore.get(action);
        if (acsBefore == null) {
            return null;
        }
        for (ActionConnector acBefore : acsBefore) {
            if (!acBefore.doesLinkServeActor()) {
                return acBefore.getRoleOfActionBeforeTarget();
            }
        }
        return null;
    }

    public LocationVariable getFirstLocationInPlan(GroupVariable groupVariable) {
        Set<Action> allActionCandidates = connectorsWithActionBefore.keySet();
        for (Action actionCandidate : allActionCandidates) {
            if (!connectorsWithActionAfter.containsKey(actionCandidate)) { // Leaf action.
                if (groupVariable.isBoundTo(actionCandidate.getActorVariable())) {
                    return actionCandidate.getLocationVariable();
                }
                if (groupVariable.isBoundTo(actionCandidate.getTargetVariable())) {
                    return actionCandidate.getLocationVariable();
                }
            }
        }
        return null;
    }

    public LocationVariable getNextLocationInPlan(LocationVariable currentLocationVariable) {
        Action ta = findActionWithLocation(getGoalAction(), currentLocationVariable);
        ArrayList<ActionConnector> actionConnectors = connectorsWithActionBefore.get(ta);
        // There should be only one.
        if (actionConnectors == null || actionConnectors.size() != 1) {
            return null;
        }
        return actionConnectors.get(0).getActionAfter().getLocationVariable();
    }

    public List<Action> getActionSequence(GroupVariable groupVariable, boolean includingTargetActions) {
        ArrayList<Action> result = new ArrayList<>();
        getActionSequence(groupVariable, getGoalAction(), result, includingTargetActions);
        Collections.reverse(result);
        return result;
    }

    private void getActionSequence(GroupVariable groupVariable, Action current, ArrayList<Action> into, boolean includingTargetActions) {
        if (groupVariable.isBoundTo(current.getActorVariable())) {
            into.add(current);
        } else if (includingTargetActions && groupVariable.isBoundTo(current.getTargetVariable())) {
            into.add(current);
        } else if (includingTargetActions) {
            return;
        }
        ArrayList<ActionConnector> actionConnectors = connectorsWithActionAfter.get(current);
        if (actionConnectors == null) {
            return;
        }
        for (ActionConnector actionConnector : actionConnectors) {
            getActionSequence(groupVariable, actionConnector.getActionBefore(), into, includingTargetActions);
        }
    }

    private Action findActionWithLocation(Action root, LocationVariable locationVariable) {
        if (root.getLocationVariable().isBoundTo(locationVariable)) {
            return root;
        }
        ArrayList<ActionConnector> actionConnectors = connectorsWithActionAfter.get(root);
        if (actionConnectors == null) {
            return null;
        }
        for (ActionConnector ac : actionConnectors) {
            Action foundAction = findActionWithLocation(ac.actionBefore, locationVariable);
            if (foundAction != null) {
                return foundAction;
            }
        }
        return null;
    }

    private void addLink(Action directlyBefore, Action directlyAfter, boolean linkServesActor) {
        addAllVariablesToBinder(directlyBefore);
        addAllVariablesToBinder(directlyAfter);

        ActionConnector connector = new ActionConnector(directlyBefore, directlyAfter, linkServesActor);
        ArrayList<ActionConnector> acBefore = connectorsWithActionBefore.get(directlyBefore);
        if (acBefore == null) {
            acBefore = new ArrayList<ActionConnector>();
            connectorsWithActionBefore.put(directlyBefore, acBefore);
        }
        acBefore.add(connector);
        ArrayList<ActionConnector> acAfter = connectorsWithActionAfter.get(directlyAfter);
        if (acAfter == null) {
            acAfter = new ArrayList<ActionConnector>();
            connectorsWithActionAfter.put(directlyAfter, acAfter);
        }
        acAfter.add(connector);
    }

    private void addAllVariablesToBinder(Action action) {
        if (action == null) {
            return;
        }
        if (action.getActorVariable() != null) {
            binder.addVariable(action.getActorVariable());
        }
        if (action.getTargetVariable() != null) {
            binder.addVariable(action.getTargetVariable());
        }
        if (action.getLocationVariable() != null) {
            binder.addVariable(action.getLocationVariable());
        }
    }

    private ActionConnector removeLink(Action directlyBefore, Action directlyAfter) {
        ArrayList<ActionConnector> acBefore = connectorsWithActionBefore.get(directlyBefore);
        if (acBefore == null) {
            return null;
        }
        ActionConnector markDelete = null;
        for (ActionConnector connector : acBefore) {
            if (connector.getActionAfter().equals(directlyAfter)) {
                markDelete = connector;
                break;
            }
        }
        if (markDelete != null) {
            acBefore.remove(markDelete);
            ArrayList<ActionConnector> acAfter = connectorsWithActionAfter.get(directlyAfter);
            acAfter.remove(markDelete);
        }
        return markDelete;
    }

    public Set<Variable> getVariables() {
        return binder.getUniqueVariables();
    }

    public Set<GroupVariable> getGroupVariables() {
        Set<Variable> variables = getVariables();
        HashSet<GroupVariable> result = new HashSet<>();
        for (Variable v : variables) {
            if (v instanceof GroupVariable) {
                result.add((GroupVariable) v);
            }
        }
        return result;
    }


    public Set<LocationVariable> getLocationVariables() {
        Set<Variable> variables = getVariables();
        HashSet<LocationVariable> result = new HashSet<>();
        for (Variable v : variables) {
            if (v instanceof LocationVariable) {
                result.add((LocationVariable) v);
            }
        }
        return result;
    }

    public Variable getRestrictingVariable(Environment environment, boolean pickMostRestricting) {

        HashMap<Action, Long> actionCumulativeDuration = new HashMap<>();
        HashMap<Action, Long> actionToEndTime = new HashMap<>();
        HashMap<Action, Long> actionToStartTime = new HashMap<>();
        HashMap<Action, Long> actionToDuration = new HashMap<>();
        HashMap<Action, Agent> actionToAgent = new HashMap<>();

        // TODO The second parameter (now: false) may be promoted to a parameter in getBottleneckVariables.

        estimateDuration(environment, false, getGoalAction(), actionCumulativeDuration, actionToEndTime, actionToStartTime, actionToDuration, actionToAgent, 0);

        HashMap<Action, ArrayList<Action>> upwardDependencies = new HashMap<>();
        setUpwardDependencies(goalAction, upwardDependencies);

        //delete all the upward dependencies that are not tight.
        for (Action keyAction : upwardDependencies.keySet()) {
            if (upwardDependencies.get(keyAction) != null) {
                ArrayList<Action> toRemoveFromKeyActionDependency = new ArrayList<>();
                for (Action mapAction : upwardDependencies.get(keyAction)) {
                    if (mapAction != null) {
                        if (!(actionToEndTime.get(mapAction).equals(actionToStartTime.get(keyAction)))) { //ac1 and action are not tight.
                            toRemoveFromKeyActionDependency.add(mapAction);
                        }
                    }
                }
                for (Action act1 : toRemoveFromKeyActionDependency) {
                    upwardDependencies.get(keyAction).remove(act1);
                }
            }
        }

        ArrayList<Action> blockingActions = new ArrayList<>();
        Action currentAction = goalAction;
        while (upwardDependencies.get(currentAction) != null) {
            blockingActions.add(upwardDependencies.get(currentAction).get(0));
            currentAction = upwardDependencies.get(currentAction).get(0);
        }

        //pick variable to optimize.
        Variable var;
        if (pickMostRestricting) {
            //obtain optimizing action.
            ArrayList<Long> durationsBlockingActions = new ArrayList<>();
            for (Action ac : blockingActions) {
                durationsBlockingActions.add(actionToDuration.get(ac));
            }
            Action optAction = blockingActions.get(RandomNumber.drawFromLong(durationsBlockingActions));
            if (connectorsWithActionAfter.get(optAction) != null) {
                var = connectorsWithActionAfter.get(optAction).get(0).actionBefore.getLocationVariable(); //any of the preceding actions suffice.
            } else {
                var = optAction.getActorVariable();
            }
        } else {
            //find all variables connected to the restricting actions.
            Set<Variable> vars = new HashSet<>();
            Set<Variable> uniqueVariables = binder.getUniqueVariables();
            for (Variable variable : uniqueVariables) {
                for (Action action : blockingActions) {
                    if (variable instanceof LocationVariable) {
                        LocationVariable lv = (LocationVariable) variable;
                        if (lv.isBoundTo(action.getLocationVariable())) {
                            if (!(lv.isBoundTo(goalAction.getLocationVariable()))) { //we do not want to add the location of the goalAction.
                                vars.add(lv);
                            }
                        }
                    } else if (variable instanceof GroupVariable) {
                        GroupVariable gv = (GroupVariable) variable;
                        if (gv.isBoundTo(action.getActorVariable()) || gv.isBoundTo(action.getTargetVariable())) {
                            if (!gv.areAgentsProvided()) { //do not add spawned groups to vars.
                                vars.add(gv);
                            }
                        }
                    }
                }
            }

            //pick a random one.
            int varIndex = RandomNumber.nextInt(vars.size());
            var = (Variable) vars.toArray()[varIndex];
        }

        return var;
    }

    private void setUpwardDependencies(Action action, HashMap<Action, ArrayList<Action>> ud) {
        ArrayList<ActionConnector> temp = connectorsWithActionAfter.get(action);
        Set<Action> temp1 = targetDependencies.get(action);

        if (temp != null && temp.size() > 0 && temp1 != null && temp1.size() > 0) {
            ArrayList<Action> toAdd = new ArrayList<>();
            for (ActionConnector aconn : temp) {
                toAdd.add(aconn.getActionBefore());
            }
            for (Action act : temp1) {
                toAdd.add(act);
            }
            ud.put(action, toAdd);

            //loop
            for (ActionConnector ac : temp) {
                setUpwardDependencies(ac.getActionBefore(), ud);
            }
        } else if (temp != null && temp.size() > 0) {
            ArrayList<Action> toAdd = new ArrayList<>();
            for (ActionConnector aconn : temp) {
                toAdd.add(aconn.getActionBefore());
            }
            ud.put(action, toAdd);

            //loop
            for (ActionConnector ac : temp) {
                setUpwardDependencies(ac.getActionBefore(), ud);
            }
        } else if (temp1 != null && temp1.size() > 0) {
            ArrayList<Action> toAdd = new ArrayList<>();
            for (Action act : temp1) {
                toAdd.add(act);
            }
            ud.put(action, toAdd);
        } else {
            ud.put(action, null);
        }
    }

    public long estimateDuration(Environment environment, boolean includeFinalActionDuration) {

        HashMap<Action, Long> actionCumulativeDuration = new HashMap<>();
        estimateDuration(environment, includeFinalActionDuration,
                getGoalAction(), actionCumulativeDuration, new HashMap<Action, Long>(),
                new HashMap<Action, Long>(), new HashMap<Action, Long>(), new HashMap<Action, Agent>(), 0);

        for (Action action : actionCumulativeDuration.keySet()) {
            long cumulativeDuration = actionCumulativeDuration.get(action);
            action.getLocationVariable().getValue().setTimeNanos(cumulativeDuration);
        }

        return actionCumulativeDuration.get(getGoalAction());
    }

    private void estimateDuration(Environment environment, boolean includeFinalActionDuration,
                                  Action action, HashMap<Action, Long> actionCumulativeDuration,
                                  HashMap<Action, Long> actionToEndTime, HashMap<Action, Long> actionToStartTime,
                                  HashMap<Action, Long> actionToDuration, HashMap<Action, Agent> actionToAgent, int depth) {

        // Quick return in case of a revisit.
        if (actionCumulativeDuration.containsKey(action)) {
            return;
        }

        // This is going to reflect how long the action sequence will take until this action.
        long maxDurationBefore = 0;

        // 1. Handle actions that come directly before.
        ArrayList<ActionConnector> connectorsToAction = connectorsWithActionAfter.get(action);
        if (connectorsToAction != null) {
            for (ActionConnector actionConnector : connectorsToAction) {
                Action actionBefore = actionConnector.getActionBefore(); //TODO NOTE not all dependencies caught! [SdJ -- what does this mean?]
                estimateDuration(environment, includeFinalActionDuration, actionBefore, actionCumulativeDuration, actionToEndTime,
                        actionToStartTime, actionToDuration, actionToAgent, depth + 1);

                long cumulativeDurationOfActionBefore = actionCumulativeDuration.get(actionBefore);
                maxDurationBefore = Math.max(maxDurationBefore, cumulativeDurationOfActionBefore);
                if (actionToStartTime.get(action) == null) {
                    actionToStartTime.put(action, actionToEndTime.get(actionBefore));
                } else if (actionToStartTime.get(action) < actionToEndTime.get(actionBefore)) {
                    actionToStartTime.put(action, actionToEndTime.get(actionBefore));
                }
            }
        }

        // Wait for target dependencies.
        Set<Action> targetDependencies = this.targetDependencies.get(action);
        if (targetDependencies != null) {
            for (Action targetDependency : targetDependencies) {
                estimateDuration(environment, includeFinalActionDuration, targetDependency, actionCumulativeDuration, actionToEndTime,
                        actionToStartTime, actionToDuration, actionToAgent, depth + 1);

                long cumulativeDurationOfIndirectDependency = actionCumulativeDuration.get(targetDependency);
                maxDurationBefore = Math.max(maxDurationBefore, cumulativeDurationOfIndirectDependency);
                if (actionToStartTime.get(action) == null) {
                    actionToStartTime.put(action, actionToEndTime.get(targetDependency));
                } else if (actionToStartTime.get(action) < actionToEndTime.get(targetDependency)) {
                    actionToStartTime.put(action, actionToEndTime.get(targetDependency));
                }
            }
        }

        // Include the action duration for this action.
        long ownDuration = 0;
        if (action == getGoalAction() && !includeFinalActionDuration) {
            // Don't include the duration of the goal action.
        }
        if (action.getModel() != null && (action != getGoalAction() || includeFinalActionDuration)) {
            action.instantiateModelVariables(environment);
            if (connectorsToAction != null && connectorsToAction.size() > 0) {
                long maxDurationForModel = 0;
                for (ActionConnector actionConnector : connectorsToAction) {
                    Action actionBefore = actionConnector.getActionBefore();
                    LocationVariable locationVariable = actionBefore.getLocationVariable();
                    long timeForAction;
                    if (!locationVariable.isRestrictedToAgentLocations()) {
                        IGeometry locationBefore = locationVariable.getValue().getLocation();
                        timeForAction = action.getModel().estimateMinimumTimeToExecute(locationBefore);
                    } else {
                        long maxTimeForActionAgent = -1;
                        for (Agent agent : locationVariable.getAssociatedGroupVariable().getValue()) {
                            IGeometry locationBefore = agent.getLocation();
                            long timeForAgent = action.getModel().estimateMinimumTimeToExecute(locationBefore);
                            maxTimeForActionAgent = Math.max(maxTimeForActionAgent, timeForAgent);
                        }
                        timeForAction = maxTimeForActionAgent;
                    }
                    maxDurationForModel = Math.max(maxDurationForModel, timeForAction);
                }
                ownDuration = maxDurationForModel;
            } else {
                long timeForFirstMove = determineTimeForFirstMove(action, true, actionToEndTime, actionToStartTime, actionToDuration, actionToAgent);
                if (action.getTargetVariable() != null) {
                    long timeForFirstTargetMove = determineTimeForFirstMove(action, false, actionToEndTime, actionToStartTime, actionToDuration, actionToAgent);
                    timeForFirstMove = Math.max(timeForFirstMove, timeForFirstTargetMove);
                }
                ownDuration = timeForFirstMove;
            }
        }

        // Remember this.
        actionToDuration.put(action, ownDuration);
        if (actionToStartTime.containsKey(action)) { // For the goal action, this is not the case. Only happens when the goal action is the ONLY action.
            actionToEndTime.put(action, actionToStartTime.get(action) + ownDuration);
        }
        long cumulativeDuration = maxDurationBefore + ownDuration;
        actionCumulativeDuration.put(action, cumulativeDuration);
    }

    private long determineTimeForFirstMove(Action action, boolean forActor, HashMap<Action, Long> actionToEndTime, HashMap<Action, Long> actionToStartTime,
                                           HashMap<Action, Long> actionToDuration, HashMap<Action, Agent> actionToAgent) {

        action.instantiateModelVariables(environment);

        Model model = action.getModel();
        GroupVariable whoIsMoving = (forActor) ? action.getActorVariable() : action.getTargetVariable();

        long timeForFirstMove = 0;
        actionToStartTime.put(action, (long) 0);
        if (whoIsMoving.areAgentsProvided()) {
            timeForFirstMove = model.estimateMinimumTimeToExecute(action.getLocationVariable().getValue().getLocation());
        } else {
            Group movingGroup = whoIsMoving.getValue();
            for (Agent movingAgent : movingGroup) {
                long timeForAgent = model.estimateMinimumTimeToExecute(movingAgent.getLocation());
                if (timeForAgent > timeForFirstMove) {
                    timeForFirstMove = timeForAgent;
                    actionToAgent.put(action, movingAgent); //keep track of slowest agent for this action.
                }
            }
        }
        actionToEndTime.put(action, timeForFirstMove);
        actionToDuration.put(action, timeForFirstMove);

        return timeForFirstMove;
    }

    public void adjustPlanLength(long startTime) {
        double currentDuration = getGoalAction().getLocationVariable().getValue().getTimeNanos(); // From 0.
        adjustPlanLength(startTime, (long) (startTime + currentDuration));
    }

    public void adjustPlanLength(long startTime, long endTime) {
        double currentDuration = getGoalAction().getLocationVariable().getValue().getTimeNanos(); // From 0.
        double factor = (endTime - startTime) / currentDuration;

        if (factor == Double.POSITIVE_INFINITY) {    // Which is when the plan is instant.
            for (LocationVariable lv : getLocationVariables()) {
                lv.getValue().setTimeNanos(endTime);
            }
        } else {
            for (LocationVariable lv : getLocationVariables()) {
                LocationAndTime currentValue = lv.getValue();
                long currentTime = currentValue.getTimeNanos();

                //temp disabled print
                //System.out.println("VARIABLE " + lv + " SET TIME TO " + startTime + " + " + currentTime + " * " + factor + " = " + (long) (startTime + currentTime * factor));

                currentTime = (long) (startTime + currentTime * factor);
                currentValue.setTimeNanos(currentTime);
            }
        }
    }

    public void startModels(Environment environment) {

        System.out.println("\nConnecting agents and action models.");  // TODO Replace by logger.

        for (GroupVariable gv : getGroupVariables()) {
            List<Action> actionSequence = getActionSequence(gv, false);

            System.out.println("\nPushing action models to agent(s) in group: " + gv);  // TODO Replace by logger.
            System.out.println("Action sequence for agent(s): " + TextUtils.classNamesToString(actionSequence)); // TODO Replace by logger.

            int start = 0;
            if (gv.areAgentsProvided()) {
                Action providingAction = actionSequence.get(0);
                start = 1; // Skip this model.
                Model spawningModel = providingAction.getModel();
                spawningModel.setPartOfIncident(true);
                spawningModel.nextStep(0);
                if (gv.getValue() == null) {
                    throw new Error("Illegal plan; agents in group " + gv + " need to be spawned, but " + providingAction + " does not spawn them.");
                }
            }

            for (int i = actionSequence.size() - 1; i >= start; --i) {
                Action action = actionSequence.get(i);
                action.instantiateModelVariables(environment);
                Model model = action.getModel();
                model.setPartOfIncident(true);
                Set<Action> dependencies = getTargetDependencies(action);
                if (dependencies != null) {
                    for (Action depAction : dependencies) {
                        Model depModel = depAction.getModel();
                        model.addModelToWaitFor(depModel);
                    }
                }
                for (Agent a : gv.getValue()) {
                    a.pushModel(model);
                }
            }
        }
    }

    public Set<Agent> getAgentsInvolved() {
        Set<Agent> result = new HashSet<Agent>();
        for (Variable v : getVariables()) {
            if (v.getValue() != null) {
                if (v instanceof GroupVariable) {
                    GroupVariable gv = (GroupVariable) v;
                    Group group = gv.getValue();
                    if (group != null) {
                        for (Agent a : group) {
                            result.add(a);
                        }
                    }
                }
            }
        }
        return result;
    }

    public List<Action> getActionSequence(Agent agent) {
        Set<GroupVariable> groupVariables = getGroupVariables();
        for (GroupVariable groupVariable : groupVariables) {
            Group group = groupVariable.getValue();
            if (group != null && group.contains(agent)) {
                return getActionSequence(groupVariable, false);
            }
        }
        return null;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        createPlanToString(sb, goalAction, 0);
        return sb.toString().trim();
    }

    private void createPlanToString(StringBuilder into, Action currentAction, int level) {
        for (int i = 0; i < level; i++) {
            into.append("^  ");
        }
        into.append(currentAction);
        Set<Action> targetDependencies = this.targetDependencies.get(currentAction);
        if (targetDependencies != null && targetDependencies.size() > 0) {
            into.append(" After: [");
            for (Action dep : targetDependencies) {
                into.append(dep.getVerb()).append(",");
            }
            into.deleteCharAt(into.length() - 1);
            into.append("]");
        }
        into.append('\n');
        ArrayList<ActionConnector> acsBefore = connectorsWithActionAfter.get(currentAction);
        if (acsBefore == null) {
            return;
        }
        for (ActionConnector acBefore : acsBefore) {
            createPlanToString(into, acBefore.actionBefore, level + 1);
        }
    }

    private class ActionConnector {

        private Action actionBefore;
        private Action actionAfter;
        private boolean linkServesActor; // or target

        private Class<? extends Role> roleOfActionBeforeActor;
        private Class<? extends Role> roleOfActionBeforeTarget;

        private ActionConnector(Action actionBefore, Action actionAfter, boolean linkServesActor) {
            this.actionBefore = actionBefore;
            this.actionAfter = actionAfter;
            if (actionAfter != null && actionBefore != null) {
                boolean actionAfterIsSynced = !actionAfter.getModel().agentsCanWaitBeforeStarting();
                actionBefore.getModel().setAgentsMustArriveSimultaneously(actionBefore.getModel().agentsMustArriveSimultaneously() || actionAfterIsSynced);
            }
            this.linkServesActor = linkServesActor;
            determineRoleOfActionBeforeActor(); // TODO Added, correct?
            determineRoleOfActionBeforeTarget();
        }

        public Action getActionBefore() {
            return actionBefore;
        }

        public Action getActionAfter() {
            return actionAfter;
        }

        public boolean doesLinkServeActor() {
            return linkServesActor;
        }

        public void determineRoleOfActionBeforeActor() {
            Class<? extends Role> newActorRole = actionBefore.getActorRoleAfter();
            if (newActorRole != null) {
                setRoleOfActionBeforeActor(newActorRole);
            }
        }

        private void setRoleOfActionBeforeActor(Class<? extends Role> roleOfActionBeforeActor) {
            if (this.roleOfActionBeforeActor == null) {
                this.roleOfActionBeforeActor = roleOfActionBeforeActor;
            }
            if (actionAfter != null) {
                ArrayList<ActionConnector> connectorsFromActionAfter = connectorsWithActionBefore.get(actionAfter);
                if (connectorsFromActionAfter == null || connectorsFromActionAfter.size() == 0) {
                    return;
                }
                for (ActionConnector connectorFromActionAfter : connectorsFromActionAfter) {
                    if (connectorFromActionAfter.doesLinkServeActor()) {
                        connectorFromActionAfter.setRoleOfActionBeforeActor(roleOfActionBeforeActor); // Propagate the role.
                    }
                }
            }
        }

        public Class<? extends Role> getRoleOfActionBeforeActor() {
            return roleOfActionBeforeActor;
        }

        public void determineRoleOfActionBeforeTarget() {
            Class<? extends Role> newTargetRole = actionBefore.getTargetRoleAfter();
            if (newTargetRole != null) {
                setRoleOfActionBeforeTarget(newTargetRole);
            }
        }

        private void setRoleOfActionBeforeTarget(Class<? extends Role> roleOfActionBeforeTarget) {
            if (this.roleOfActionBeforeTarget == null) {
                this.roleOfActionBeforeTarget = roleOfActionBeforeTarget;
            }
            if (actionAfter != null) {
                ArrayList<ActionConnector> connectorsFromActionAfter = connectorsWithActionBefore.get(actionAfter);
                if (connectorsFromActionAfter == null || connectorsFromActionAfter.size() == 0) {
                    return;
                }
                for (ActionConnector connectorFromActionAfter : connectorsFromActionAfter) {
                    if (!connectorFromActionAfter.doesLinkServeActor()) {
                        connectorFromActionAfter.setRoleOfActionBeforeActor(roleOfActionBeforeTarget); // Propagate the role.
                    }
                }
            }
        }

        public Class<? extends Role> getRoleOfActionBeforeTarget() {
            return roleOfActionBeforeTarget;
        }

        private String c(Object o) {
            return o != null ? o instanceof Class ? ((Class) o).getSimpleName() : o.getClass().getSimpleName() : "null";
        }

        @Override
        public String toString() {
            return "ActionConnector{" +
                    "actionBefore=" + c(actionBefore) +
                    ", actionAfter=" + c(actionAfter) +
                    ", linkServesActor=" + linkServesActor +
                    ", roleOfActionBeforeActor=" + c(roleOfActionBeforeActor) +
                    ", roleOfActionBeforeTarget=" + c(roleOfActionBeforeTarget) +
                    '}';
        }
    }
}
