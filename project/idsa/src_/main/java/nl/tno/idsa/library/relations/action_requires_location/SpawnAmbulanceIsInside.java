package nl.tno.idsa.library.relations.action_requires_location;

import nl.tno.idsa.framework.semantics_impl.relations.LocationEnablesAction;
import nl.tno.idsa.library.actions.SpawnAmbulance;
import nl.tno.idsa.library.locations.Inside;

/**
 * Created by jongsd on 20-8-15.
 */
// TODO Add ignore unused. Document the class. Add specific medic posts.
public class SpawnAmbulanceIsInside extends LocationEnablesAction {
    public SpawnAmbulanceIsInside() {
        super(Inside.class, SpawnAmbulance.class);
    }
}
