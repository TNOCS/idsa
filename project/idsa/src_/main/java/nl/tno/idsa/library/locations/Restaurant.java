package nl.tno.idsa.library.locations;

import nl.tno.idsa.framework.semantics_impl.locations.LocationFunction;

/**
 * Created by kleina on 27-10-2015.
 */
public class Restaurant extends LocationFunction {
    @Override
    @SuppressWarnings("unchecked")
    protected Class<? extends LocationFunction>[] getSuperclassArray() {
        return new Class[]{Inside.class, Public.class};
    }
}
