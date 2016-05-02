package nl.tno.idsa.library.roles;

import nl.tno.idsa.framework.semantics_impl.roles.Role;

/**
 * Created by jongsd on 4-8-15.
 */
public final class Panicker extends Role {
    @Override
    @SuppressWarnings("unchecked") // Generic array creation...
    protected Class<? extends Role>[] createSemanticSuperclassArray() {
        return new Class[]{Civilian.class};
    }
}
