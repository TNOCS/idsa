package nl.tno.idsa.library.roles;

import nl.tno.idsa.framework.semantics_impl.roles.Role;

import java.awt.*;

/**
 * Created by jongsd on 4-8-15.
 */
public final class Police extends Role {
    @Override
    @SuppressWarnings("unchecked") // Generic array creation...
    protected Class<? extends Role>[] createSemanticSuperclassArray() {
        return new Class[]{FirstResponder.class};
    }

    @Override
    protected Color getTypicalColor() {
        return new Color(0, 0, 255);
    }
}
