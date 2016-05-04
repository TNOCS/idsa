package nl.tno.idsa.library.actions;

import nl.tno.idsa.framework.semantics_impl.actions.Action;
import nl.tno.idsa.framework.semantics_impl.variables.GroupVariable;
import nl.tno.idsa.library.models.ModelStandStill;

/**
 * Created by wermeskerkenfpjv on 11-8-2015.
 */
@SuppressWarnings("unused")
public class StandStill extends Action {
    public StandStill() {
        super(new ModelStandStill(),
                new GroupVariable(), null, null, ProvidesAgents.NO, AllowsInsertMoveActionBefore.YES);
    }

    @Override
    @SuppressWarnings("unchecked") // Generic array.
    protected Class<? extends Action>[] createSemanticSuperclassArray() {
        return null;
    }
}
