package nl.tno.idsa.library.actions;

import nl.tno.idsa.framework.semantics_impl.actions.Action;
import nl.tno.idsa.framework.semantics_impl.variables.GroupVariable;
import nl.tno.idsa.framework.world.GeometryType;
import nl.tno.idsa.library.models.ModelSpawn;
import nl.tno.idsa.library.roles.Police;

/**
 * Created by jongsd on 7-8-15.
 */
@SuppressWarnings("unused")
public class SpawnPolice extends Action {
    public SpawnPolice() {
        super(null,
                // TODO See SpawnAmbulance
                new GroupVariable(), null, GeometryType.asSet(GeometryType.POINT), ProvidesAgents.YES, AllowsInsertMoveActionBefore.NO);
        setModel(new ModelSpawn(this, Police.class));
    }

    @Override
    @SuppressWarnings("unchecked") // Generic array.
    protected Class<? extends Action>[] createSemanticSuperclassArray() {
        return null;
    }
}
