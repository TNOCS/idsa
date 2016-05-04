package nl.tno.idsa.library.relations.action_requires_location;

import nl.tno.idsa.framework.semantics_impl.relations.LocationEnablesAction;
import nl.tno.idsa.library.actions.Shoplift;
import nl.tno.idsa.library.locations.Shop;

/**
 * Created by jongsd on 20-8-15.
 */
// TODO Add ignore unused. Document the class.
public class ShopliftIsAtShop extends LocationEnablesAction {
    public ShopliftIsAtShop() {
        super(Shop.class, Shoplift.class);
    }
}
