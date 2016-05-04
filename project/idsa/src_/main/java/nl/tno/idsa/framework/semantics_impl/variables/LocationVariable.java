package nl.tno.idsa.framework.semantics_impl.variables;

import nl.tno.idsa.framework.semantics_base.SemanticLibrary;
import nl.tno.idsa.framework.semantics_impl.locations.LocationAndTime;
import nl.tno.idsa.framework.semantics_impl.locations.LocationFunction;
import nl.tno.idsa.framework.utils.TextUtils;
import nl.tno.idsa.framework.world.GeometryType;
import nl.tno.idsa.library.locations.AtAgent;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by jongsd on 4-8-15.
 */


// TODO Document code.

public class LocationVariable extends Variable<LocationAndTime> {

    private Set<Class<? extends LocationFunction>> allowedFunctions;
    private GroupVariable associatedGroupVariable = null;

    private Set<GeometryType> allowedGeometryTypes;

    public LocationVariable(LocationAndTime value) {
        setValue(value);
        if (value.getLocation() != null) {
            this.allowedGeometryTypes = new HashSet<>(1);
            this.allowedGeometryTypes.add(value.getLocation().getGeometryType());
        } else {
            this.allowedGeometryTypes = new HashSet<>();
        }
        this.allowedFunctions = new HashSet<>();
    }

    public LocationVariable(Set<GeometryType> allowedGeometryTypes) {
        if (allowedGeometryTypes == null) throw new NullPointerException();
        this.allowedGeometryTypes = allowedGeometryTypes;
        this.allowedFunctions = new HashSet<>();
    }

    public LocationVariable(Class<? extends LocationFunction> desiredFunction) {
        allowedFunctions = new HashSet<>(1);
        allowedFunctions.add(desiredFunction);
    }

    public LocationVariable(Set<GeometryType> allowedGeometryTypes, Class<? extends LocationFunction> desiredFunction) {
        if (allowedGeometryTypes == null) throw new NullPointerException();
        this.allowedGeometryTypes = allowedGeometryTypes;
        this.allowedFunctions = new HashSet<>();
        this.allowedFunctions.add(desiredFunction);
    }

    public LocationVariable(Set<GeometryType> allowedGeometryTypes, Set<Class<? extends LocationFunction>> allowedFunctions) {
        if (allowedGeometryTypes == null) throw new NullPointerException();
        this.allowedGeometryTypes = allowedGeometryTypes;
        this.allowedFunctions = allowedFunctions;
    }

    // We mean these functions as an OR.
    public Set<Class<? extends LocationFunction>> getAllowedFunctions() {
        if (getBinder() != null) {
            return ((LocationVariable) getBinder().getMostSpecificVariable(this)).allowedFunctions;
        }
        return allowedFunctions;
    }

    public void restrictToAgentLocations(GroupVariable associatedGroupVariable) {
        if (getBinder() != null) {
            Set<Variable> allBoundVariables = getBinder().getAllBoundVariables(this);
            for (Variable v : allBoundVariables) {
                LocationVariable lv = (LocationVariable) v;
                lv.allowedFunctions = new HashSet<>(1);
                lv.allowedFunctions.add(AtAgent.class);
                lv.associatedGroupVariable = associatedGroupVariable;
            }
        } else {
            allowedFunctions = new HashSet<>(1);
            allowedFunctions.add(AtAgent.class);
            this.associatedGroupVariable = associatedGroupVariable;
        }
    }

    public boolean isRestrictedToAgentLocations() {
        return associatedGroupVariable != null;
    }

    public GroupVariable getAssociatedGroupVariable() {
        return associatedGroupVariable;
    }

    public Set<GeometryType> getAllowedGeometryTypes() {

        if (getBinder() != null) {
            return ((LocationVariable) getBinder().getMostSpecificVariable(this)).allowedGeometryTypes;
        }
        return allowedGeometryTypes;
    }

    @Override
    public Variable<LocationAndTime> getConstraintIntersection(Variable<LocationAndTime> otherVariable) {
        LocationVariable result = new LocationVariable(allowedGeometryTypes, allowedFunctions);
        LocationVariable lv = (LocationVariable) otherVariable;

        if (lv.allowedGeometryTypes == null) {
            result.allowedGeometryTypes = allowedGeometryTypes; // already the case.
        } else if (allowedGeometryTypes != null) { // And the other one is also not null.
            if (allowedGeometryTypes.size() == 0) {
                result.allowedGeometryTypes = lv.allowedGeometryTypes;
            } else if (lv.allowedGeometryTypes.size() == 0) {
                result.allowedGeometryTypes = allowedGeometryTypes;
            } else {
                HashSet<GeometryType> newTypes = new HashSet<>(allowedGeometryTypes);
                newTypes.retainAll(lv.allowedGeometryTypes);
                if (newTypes.size() == 0) {
                    return null;
                }
                result.allowedGeometryTypes = newTypes;
            }

        } else { // Other not null, ours is.
            result.allowedGeometryTypes = lv.allowedGeometryTypes;
        }

        if (allowedFunctions == null || allowedFunctions.size() == 0) {
            result.allowedFunctions = lv.allowedFunctions;
        } else if (lv.allowedFunctions == null || lv.allowedFunctions.size() == 0) {
            result.allowedFunctions = allowedFunctions; // it was already.
        } else {
            result.allowedFunctions = new HashSet<>();
            if (allowedFunctions.size() == 0 && lv.allowedFunctions.size() == 0) {
                return result; // There are no constraints.
            }
            for (Class<? extends LocationFunction> myFunction : allowedFunctions) {
                for (Class<? extends LocationFunction> otherFunction : allowedFunctions) {
                    if (SemanticLibrary.getInstance().isSemanticSubclass(myFunction, otherFunction)) {
                        result.allowedFunctions.add(otherFunction);
                    } else if (SemanticLibrary.getInstance().isSemanticSuperclass(myFunction, otherFunction)) {
                        result.allowedFunctions.add(myFunction); // It was already but ok.
                    }
                }
            }
            if (result.allowedFunctions.size() == 0) {
                // No viable intersection was found.
                return null;
            }
        }

        // Make unique.
        if (result.allowedFunctions.equals(allowedFunctions) && result.allowedGeometryTypes.equals(allowedGeometryTypes)) {
            return this;
        } else if (result.allowedFunctions.equals(lv.allowedFunctions) && result.allowedGeometryTypes.equals(lv.allowedGeometryTypes)) {
            return lv;
        } else {
            return result;
        }
    }

    @Override
    protected boolean checkValidity(LocationAndTime value) {
        return true; // TODO Implement; check whether the provided location is valid given the constraints. ...
        // I would not be surprised if this requires a look-up in the game world. For example,
        // with a constraint SHOPPING, (100,100) may be valid if this is in a shop, but invalid
        // otherwise. Similar issue arises in the action planner by the way, check the TODOs there.
    }

    @Override
    public Variable<LocationAndTime> deepClone() {
        LocationVariable result = new LocationVariable(allowedGeometryTypes, allowedFunctions);
        result.setValue(getValue());
        return result;
    }

    @Override
    public String toString() {
        if (getBinder() != null) {
            Variable<LocationAndTime> mostSpecificVariable = getBinder().getMostSpecificVariable(this);
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
        String detail = TextUtils.classNamesToString(allowedFunctions);
        detail = ((allowedFunctions != null && allowedFunctions.size() != 0) ? detail : "").trim(); // +
        if (getValue() != null) {
            detail += " Value=" + getValue() + "";
        }
        detail = detail.trim();
        return "L" + getIdString() + (detail.length() > 0 ? "[" + // id + (pooledId != id ? "=" + pooledId : "") +
                detail + "]" : "");
    }
}
