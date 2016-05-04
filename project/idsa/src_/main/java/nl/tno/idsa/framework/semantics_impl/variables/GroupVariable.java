package nl.tno.idsa.framework.semantics_impl.variables;

import nl.tno.idsa.framework.semantics_base.SemanticLibrary;
import nl.tno.idsa.framework.semantics_impl.groups.Group;
import nl.tno.idsa.framework.semantics_impl.roles.Role;

import java.util.Set;

/**
 * Created by jongsd on 4-8-15.
 */

// TODO Code documentation.

public class GroupVariable extends Variable<Group> {

    public static final int ANY_NUMBER_OF_MEMBERS = -1; // TODO Does this have to be public?

    private int numMembers;
    private Class<? extends Role> memberRole;
    private boolean agentsAreProvided = false;

    public GroupVariable() {   // We don't care about the members or role.
        this(ANY_NUMBER_OF_MEMBERS);
    }

    public GroupVariable(int numMembers) {
        this(numMembers, null);
    }

    public GroupVariable(int numMembers, Class<? extends Role> memberRole) {
        this.numMembers = numMembers;
        this.memberRole = memberRole;
    }

    public int getNumMembers() {
        if (getBinder() != null) {
            return ((GroupVariable) getBinder().getMostSpecificVariable(this)).numMembers;
        }
        return numMembers;
    }

    public Class<? extends Role> getMemberRole() {
        if (getBinder() != null) {
            GroupVariable mostSpecificVariable = (GroupVariable) getBinder().getMostSpecificVariable(this);
            return mostSpecificVariable.memberRole;
        }
        return memberRole;
    }

    public boolean areAgentsProvided() {
        return agentsAreProvided;
    }

    public void setAgentsAreProvided(boolean isSpawned) {
        this.agentsAreProvided = isSpawned;
    }

    @Override
    public Variable<Group> getConstraintIntersection(Variable<Group> otherVariable) {

        GroupVariable result = new GroupVariable(numMembers, memberRole);
        GroupVariable gv = (GroupVariable) otherVariable;

        // Members.
        if (gv.numMembers != ANY_NUMBER_OF_MEMBERS && numMembers == ANY_NUMBER_OF_MEMBERS) {
            result = gv;
        } else if (gv.numMembers == ANY_NUMBER_OF_MEMBERS && numMembers != ANY_NUMBER_OF_MEMBERS) {
            result = this;
        } else if (gv.numMembers != numMembers) {
            result = null; // Incompatible.
        }

        // Roles.
        if (result != null) {
            if (gv.memberRole == null) {
                if (memberRole != null) {
                    result.memberRole = memberRole;  // In fact it already is.
                }
            } else if (memberRole == null) {
                if (gv.memberRole != null) { // Yes, this is always true.
                    result.memberRole = gv.memberRole;
                }
            } else {
                if (SemanticLibrary.getInstance().isSemanticSubclass(result.memberRole, gv.memberRole)) {
                    result.memberRole = gv.memberRole;
                } else if (SemanticLibrary.getInstance().isSemanticSuperclass(result.memberRole, gv.memberRole)) {
                    result.memberRole = memberRole;
                } else {
                    result = null;
                }
            }
        }

        // Spawning.
        if (result != null) {
            result.setAgentsAreProvided(areAgentsProvided() || gv.areAgentsProvided());
        }

        // Make unique.
        if (result != null && result.numMembers == numMembers && result.memberRole == memberRole && result.areAgentsProvided() == areAgentsProvided()) {
            result = this;
        } else if (result != null && result.numMembers == gv.numMembers && result.memberRole == gv.memberRole && result.areAgentsProvided() == gv.areAgentsProvided()) {
            result = gv;
        }

        return result;
    }

    public void removeRoleConstraint(Class<? extends Role> memberRole) {
        if (getBinder() != null) {
            Set<Variable> allBoundVariables = getBinder().getAllBoundVariables(this);
            if (allBoundVariables != null) {
                for (Variable v : allBoundVariables) {
                    if (v instanceof GroupVariable) {
                        GroupVariable gv = (GroupVariable) v;
                        if (gv.memberRole != null) {
                            if (SemanticLibrary.getInstance().isSemanticSubclass(memberRole, gv.memberRole)) {
                                gv.memberRole = null; // TODO Civilian.class;
                            }
                        }
                    }
                }
            }
        }
        this.memberRole = null; // This may/will erase it twice, but ok.
    }

    @Override
    protected boolean checkValidity(Group value) {
        return true; // TODO Implement; check whether the group is a valid one to put in this group variable given the constraints.
    }

    @Override
    public Variable<Group> deepClone() {
        GroupVariable result = new GroupVariable(numMembers, memberRole);
        result.agentsAreProvided = agentsAreProvided;
        result.setValue(getValue());
        return result;
    }

    @Override
    public String toString() {
        if (getBinder() != null) {
            Variable<Group> mostSpecificVariable = getBinder().getMostSpecificVariable(this);
            if (mostSpecificVariable != null && mostSpecificVariable != this) {
                return mostSpecificVariable.toString(false);
            }
        }
        return toString(false);
    }

    @Override
    public String toString(boolean mostSpecific) {
        if (mostSpecific) {
            return toString();
        }
        if (getValue() != null) {
            return getValue().toString();
        }
        return "G" + getIdString() + "[" +
                (numMembers != ANY_NUMBER_OF_MEMBERS ? "" + numMembers + "*" : "") +
                (memberRole != null ? memberRole.getSimpleName() : "").trim() +
                (areAgentsProvided() ? " (PROVIDED)" : "") +
                (getValue() != null ? " Value=" + getValue() + "" : "") +
                ']';
    }
}
