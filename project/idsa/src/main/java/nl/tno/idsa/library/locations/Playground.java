package nl.tno.idsa.library.locations;

import nl.tno.idsa.framework.semantics_impl.locations.LocationFunction;

public class Playground extends LocationFunction {

    @Override
    @SuppressWarnings("unchecked")
    protected Class<? extends LocationFunction>[] getSuperclassArray() {
        return new Class[]{Outside.class, Public.class};
    }
    
    public Playground() {
    }

    public Playground(int capacity) {
        super(capacity);
    }    
}
