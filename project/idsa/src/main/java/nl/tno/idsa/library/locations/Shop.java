package nl.tno.idsa.library.locations;

import nl.tno.idsa.framework.semantics_impl.locations.LocationFunction;

public final class Shop extends LocationFunction {

    @Override
    @SuppressWarnings("unchecked")
    protected Class<? extends LocationFunction>[] getSuperclassArray() {
        return new Class[]{Inside.class, ShoppingArea.class};
    }

    public Shop() {
    }

    public Shop(int capacity) {
        super(capacity);
    }
}
