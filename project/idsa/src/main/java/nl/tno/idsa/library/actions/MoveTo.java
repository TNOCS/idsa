package nl.tno.idsa.library.actions;

import nl.tno.idsa.framework.semantics_impl.actions.Action;
import nl.tno.idsa.framework.semantics_impl.variables.GroupVariable;
import nl.tno.idsa.framework.world.GeometryType;
import nl.tno.idsa.library.models.BasicMovementModel;

/**
 * Created by jongsd on 4-8-15.
 */
@SuppressWarnings("unused")
public class MoveTo extends Action {
    public MoveTo() {
        super(new BasicMovementModel(BasicMovementModel.WALKING_SPEED_MS),
                // The model must ensure that the actor moves to the given location in an appropriate
                // manner, e.g. by regulating walking speed et cetera.
                new GroupVariable(), null, GeometryType.asSet(GeometryType.POINT), ProvidesAgents.NO, AllowsInsertMoveActionBefore.NO);
        setMovesActor(true);
    }

    @Override
    @SuppressWarnings("unchecked") // Generic array.
    protected Class<? extends Action>[] createSemanticSuperclassArray() {
        return null;
    }
}
