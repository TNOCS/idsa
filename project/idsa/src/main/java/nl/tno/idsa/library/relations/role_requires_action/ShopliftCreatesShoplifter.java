package nl.tno.idsa.library.relations.role_requires_action;

import nl.tno.idsa.framework.semantics_impl.relations.ActionEnablesRole;
import nl.tno.idsa.library.actions.Shoplift;
import nl.tno.idsa.library.roles.Shoplifter;

/**
 * Created by jongsd on 7-8-15.
 */
// TODO Add ignore unused. Document the class.
public class ShopliftCreatesShoplifter extends ActionEnablesRole {
    public ShopliftCreatesShoplifter() {
        super(Shoplift.class, Shoplifter.class, true);
    }
}
