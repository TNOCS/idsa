package nl.tno.idsa.framework.planners;

import nl.tno.idsa.framework.semantics_base.SemanticLibrary;
import nl.tno.idsa.framework.semantics_base.relations.SemanticRelation;
import nl.tno.idsa.framework.semantics_impl.actions.Action;
import nl.tno.idsa.framework.semantics_impl.relations.ActionEnablesRole;
import nl.tno.idsa.framework.semantics_impl.relations.RoleEnablesAction;
import nl.tno.idsa.framework.semantics_impl.roles.Role;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by jongsd on 7-8-15.
 */

// TODO Document class. Or move this into the ActionPlanner, which is the only user of this class.

public class RoleActionLookup {

    private static RoleActionLookup instance = new RoleActionLookup();

    public static RoleActionLookup getInstance() {
        return instance;
    }

    private RoleActionLookup() {
    }

    public Set<EnablingAction> getEnablingActions(Role role) {
        return getEnablingActions(role.getClass());
    }

    public Set<EnablingAction> getEnablingActions(Class<? extends Role> roleClass) {
        Set<SemanticRelation<Action, Role>> relations = SemanticLibrary.getInstance().getSemanticRelationsTo(ActionEnablesRole.class, roleClass);
        HashSet<EnablingAction> enablingActions = new HashSet<>();
        for (SemanticRelation<Action, Role> relation : relations) {
            ActionEnablesRole actionEnablesRole = (ActionEnablesRole) relation;
            EnablingAction enablingAction = new EnablingAction(actionEnablesRole.getEnabler(), actionEnablesRole.doesActionGiveActorTheRole());
            enablingActions.add(enablingAction);
        }
        return enablingActions;
    }

    public Set<Class<? extends Action>> getActions(Class<? extends Role> roleClass) {
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
