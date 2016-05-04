package nl.tno.idsa.library.locations;

import nl.tno.idsa.framework.semantics_impl.locations.LocationFunction;

public final class ShoppingArea extends LocationFunction {

    @Override
    @SuppressWarnings("unchecked")
    protected Class<? extends LocationFunction>[] getSuperclassArray() {
        return new Class[]{Public.class};
    }

    public ShoppingArea() {
    }

    @SuppressWarnings("unchecked")
    public ShoppingArea(int capacity) {
        super(capacity);
    }
}
