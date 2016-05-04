package nl.tno.idsa.library.locations;

import nl.tno.idsa.framework.semantics_impl.locations.LocationFunction;

public final class Workplace extends LocationFunction {

    @Override
    @SuppressWarnings("unchecked")
    protected Class<? extends LocationFunction>[] getSuperclassArray() {
        return new Class[]{Private.class, Inside.class};
    }

    public Workplace() {
    }

    @SuppressWarnings("unchecked")
    public Workplace(int capacity) {
        super(capacity);
    }
}
