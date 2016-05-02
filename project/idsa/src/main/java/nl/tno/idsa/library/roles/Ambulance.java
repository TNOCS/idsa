package nl.tno.idsa.library.roles;

import nl.tno.idsa.framework.semantics_impl.roles.Role;

/**
 * Created by jongsd on 4-8-15.
 */
// TODO Add ignore unused. Document the class. Same for all role classes.
@SuppressWarnings("unused")
public final class Ambulance extends Role {
    @Override
    @SuppressWarnings("unchecked") // Generic array...
    protected Class<? extends Role>[] createSemanticSuperclassArray() {
        return new Class[]{FirstResponder.class};
    }
}
