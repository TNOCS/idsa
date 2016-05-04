package nl.tno.idsa.library.locations;

import nl.tno.idsa.framework.semantics_impl.locations.LocationFunction;

/**
 * Created by jongsd on 11-8-15.
 */
public final class PoliceSpawnPoint extends LocationFunction {

    @Override
    @SuppressWarnings("unchecked")
    protected Class<? extends LocationFunction>[] getSuperclassArray() {
        return new Class[]{Private.class, Inside.class};
    }
}
