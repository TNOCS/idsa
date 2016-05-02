package nl.tno.idsa.library.roles;

import nl.tno.idsa.framework.semantics_impl.roles.Role;

import java.awt.*;

/**
 * Created by jongsd on 4-8-15.
 */
public final class Victim extends Role {
    @Override
    @SuppressWarnings("unchecked")
    protected Class<? extends Role>[] createSemanticSuperclassArray() {
        return new Class[]{Civilian.class};
    }


    @Override
    protected Color getTypicalColor() {
        return new Color(255, 127, 127);
    }
}
