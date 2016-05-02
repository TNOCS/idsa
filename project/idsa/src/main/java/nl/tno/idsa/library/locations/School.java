package nl.tno.idsa.library.locations;

import nl.tno.idsa.framework.semantics_impl.locations.LocationFunction;

public final class School extends LocationFunction {

    @Override
    @SuppressWarnings("unchecked")
    protected Class<? extends LocationFunction>[] getSuperclassArray() {
        return new Class[]{Public.class, Inside.class};
    }

    public School() {
    }

    @SuppressWarnings("unchecked")
    public School(int capacity) {
        super(capacity);
    }
}
