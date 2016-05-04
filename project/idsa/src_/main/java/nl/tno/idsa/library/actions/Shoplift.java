package nl.tno.idsa.library.actions;

import nl.tno.idsa.framework.semantics_impl.actions.Action;
import nl.tno.idsa.framework.semantics_impl.variables.GroupVariable;
import nl.tno.idsa.framework.world.GeometryType;
import nl.tno.idsa.library.actions.abstract_actions.Crime;
import nl.tno.idsa.library.models.ModelShoplift;

/**
 * Created by jongsd on 4-8-15.
 */
@SuppressWarnings("unused")
public class Shoplift extends Action {

    public Shoplift() {
        super(new ModelShoplift(),
                new GroupVariable(), null, GeometryType.asSet(GeometryType.POINT), ProvidesAgents.NO, AllowsInsertMoveActionBefore.YES);

        // TODO We could improve the system with e.g. a precondition that the shop must be open.
    }

    @Override
    @SuppressWarnings("unchecked") // Generic array.
    protected Class<? extends Action>[] createSemanticSuperclassArray() {
        return new Class[]{Crime.class};
    }
}
