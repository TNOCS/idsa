package nl.tno.idsa.library.actions;

import nl.tno.idsa.framework.semantics_impl.actions.Action;
import nl.tno.idsa.framework.semantics_impl.variables.GroupVariable;
import nl.tno.idsa.framework.world.GeometryType;
import nl.tno.idsa.library.models.ModelGetSick;

/**
 * Created by jongsd on 4-8-15.
 */
@SuppressWarnings("unused")
public class GetSick extends Action {

    public GetSick() {
        super(new ModelGetSick(),
                new GroupVariable(), null, GeometryType.asSet(GeometryType.POINT), ProvidesAgents.NO, AllowsInsertMoveActionBefore.NO);
    }

    @Override
    @SuppressWarnings("unchecked") // Generic array.
    protected Class<? extends Action>[] createSemanticSuperclassArray() {
        return null;
    }
}
