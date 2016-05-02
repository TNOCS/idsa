package nl.tno.idsa.library.roles;

import nl.tno.idsa.framework.semantics_impl.roles.Role;

/**
 * Created by jongsd on 4-8-15.
 */
public final class MedicalVictim extends Role {
    @Override
    @SuppressWarnings("unchecked")
    protected Class<? extends Role>[] createSemanticSuperclassArray() {
        return new Class[]{Victim.class};
    }
}
