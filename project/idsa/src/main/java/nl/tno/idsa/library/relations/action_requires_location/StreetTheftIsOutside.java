package nl.tno.idsa.library.relations.action_requires_location;

import nl.tno.idsa.framework.semantics_impl.relations.LocationEnablesAction;
import nl.tno.idsa.library.actions.StreetTheft;
import nl.tno.idsa.library.locations.Outside;

/**
 * Created by jongsd on 20-8-15.
 */
// TODO Add ignore unused. Document the class.
public class StreetTheftIsOutside extends LocationEnablesAction {
    public StreetTheftIsOutside() {
        super(Outside.class, StreetTheft.class);
    }
}
