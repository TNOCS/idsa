package nl.tno.idsa.library.relations.action_requires_location;

import nl.tno.idsa.framework.semantics_impl.relations.LocationEnablesAction;
import nl.tno.idsa.library.actions.SpawnPolice;
import nl.tno.idsa.library.locations.PoliceSpawnPoint;

/**
 * Created by jongsd on 20-8-15.
 */
// TODO Add ignore unused. Document the class.
public class SpawnPoliceIsAtPoliceSpawnPoint extends LocationEnablesAction {
    public SpawnPoliceIsAtPoliceSpawnPoint() {
        super(PoliceSpawnPoint.class, SpawnPolice.class);
    }
}
