package nl.tno.idsa.framework.semantics_impl.variables;

import java.util.Set;

/**
 * Created by jongsd on 7-8-15.
 */

// TODO Document code.

public abstract class Variable<T> {

    private static int ID = 1000;
    protected int id = ID++;

    private T value;
    private Set domain; // Can be null. TODO Would love to use Set<T> but then with T being Class<? extends X>, we cannot assign Class<? extends Subclass-of-X>. Java generics are implemented in a very disappointing manner.

    private VariableBinder binder;

    protected void setBinder(VariableBinder binder) {
        this.binder = binder;
    }

    protected void removeBinder() {
        this.binder = null;
    }

    public VariableBinder getBinder() {
        return binder;
    }

    protected int getPooledId() {
        if (binder == null) {
            return id;
        } else {
            return binder.getPoolId(this);
        }
    }

    @SuppressWarnings("unchecked")
    public Set<T> getDomain() {
        return (Set<T>) domain; // Yep, unchecked.
    }

    @SuppressWarnings("unchecked")
    public void setDomain(Set domain) {
        this.domain = (Set<T>) domain; // Yep, unchecked.
    }

    public T getValue() {
        return value;
    }

    public void setValue(T value) {
        if (value != null ? !value.equals(this.value) : this.value != null) {
            if (!isValidValue(value)) {
                return;
            }
            this.value = value;
            if (binder != null) {
                binder.notifyValueUpdate(this, value);
            }
        }
    }

    public boolean isValidValue(T value) {           // Asks not only this variable but also all those this variable is bound to.
        if (domain != null && !domain.contains(value)) {
            return false;
        }
        if (!checkValidity(value)) return false;
        if (binder != null && !binder.isValidValue(this, value)) {
            return false;
        }
        return true;
    }

    public boolean isUnbound() {
        return binder == null;
    }

    public boolean isBoundTo(Variable<T> otherVariable) {
        if (binder == null) {
            return false;
        }
        return binder.areVariablesBound(this, otherVariable);
    }

    /**
     * Return null with no intersection. Return this or other if that variable is most specific.
     * Return a new variable ONLY if the intersection is more specific than any of the inputs.
     */
    public abstract Variable<T> getConstraintIntersection(Variable<T> otherVariable);

    protected abstract boolean checkValidity(T value);

    public abstract Variable<T> deepClone();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        return hashCode() == o.hashCode();
    }

    @Override
    public int hashCode() {
        return id; // Makes sure every variable is really unique.
    }

    public abstract String toString();

    public abstract String toString(boolean mostSpecific);

    protected String getIdString() {
        if (id != getPooledId()) {
            return id + "=" + getPooledId();
        }
        return id + "";
    }
}
