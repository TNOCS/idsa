package nl.tno.idsa.library.locations;

import nl.tno.idsa.framework.semantics_impl.locations.LocationFunction;

/**
 * Created by jongsd on 15-10-15.
 */
public final class Private extends LocationFunction {
    @Override
    @SuppressWarnings("unchecked")
    protected Class<? extends LocationFunction>[] getSuperclassArray() {
        return new Class[]{Anywhere.class};
    }
}
