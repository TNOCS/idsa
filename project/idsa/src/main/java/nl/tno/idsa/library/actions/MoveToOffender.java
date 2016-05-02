package nl.tno.idsa.library.actions;

import nl.tno.idsa.framework.semantics_impl.actions.Action;
import nl.tno.idsa.framework.semantics_impl.variables.GroupVariable;
import nl.tno.idsa.framework.world.GeometryType;
import nl.tno.idsa.library.models.ModelMoveToOffender;

/**
 * Created by jongsd on 4-8-15.
 */
@SuppressWarnings("unused")

// TODO Doesn't this action inherit from MoveToTarget?

public class MoveToOffender extends Action {

    public MoveToOffender() {
        super(new ModelMoveToOffender(),
                new GroupVariable(),
                new GroupVariable(),
                GeometryType.asSet(GeometryType.POINT),
                ProvidesAgents.NO, AllowsInsertMoveActionBefore.NO);
        setMovesActor(true);
    }

    @Override
    @SuppressWarnings("unchecked") // Generic array.
    protected Class<? extends Action>[] createSemanticSuperclassArray() {
        return null;
    }
}
