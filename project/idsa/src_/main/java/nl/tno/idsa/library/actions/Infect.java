package nl.tno.idsa.library.actions;

import nl.tno.idsa.framework.semantics_impl.actions.Action;
import nl.tno.idsa.framework.semantics_impl.variables.GroupVariable;
import nl.tno.idsa.framework.world.GeometryType;
import nl.tno.idsa.library.models.ModelGetSick;

/**
 * Created by jongsd on 4-8-15.
 */
@SuppressWarnings("unused")
public class Infect extends Action {

    public Infect() {
        super(new ModelGetSick(),
                new GroupVariable(), new GroupVariable(), GeometryType.asSet(GeometryType.POINT), ProvidesAgents.NO, AllowsInsertMoveActionBefore.YES);
    }

    @Override
    @SuppressWarnings("unchecked") // Generic array.
    protected Class<? extends Action>[] createSemanticSuperclassArray() {
        return null; // TODO Think about whether/how actions can be categorized in the multiple inheritance tree.
    }
}
