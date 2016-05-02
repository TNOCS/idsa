package nl.tno.idsa.library.actions;

import nl.tno.idsa.framework.semantics_impl.actions.Action;
import nl.tno.idsa.framework.semantics_impl.variables.GroupVariable;
import nl.tno.idsa.framework.world.GeometryType;
import nl.tno.idsa.library.models.BasicMovementModel;

/**
 * Created by jongsd on 4-8-15.
 */
@SuppressWarnings("unused")
public class Flee extends Action {
    public Flee() {
        super(new ModelFlee(),
                new GroupVariable(), null, GeometryType.asSet(GeometryType.POINT), ProvidesAgents.NO, AllowsInsertMoveActionBefore.NO);
        setMovesActor(true);
    }

    // Mostly for debugging.
    private static class ModelFlee extends BasicMovementModel {
        private ModelFlee() {
            super(BasicMovementModel.RUNNING_SPEED_MS);
        }

        @Override
        public boolean doStep(double durationInSeconds) {
            return super.doStep(durationInSeconds);
        }
    }

    @Override
    @SuppressWarnings("unchecked") // Generic array.
    protected Class<? extends Action>[] createSemanticSuperclassArray() {
        return null;
    }

}
