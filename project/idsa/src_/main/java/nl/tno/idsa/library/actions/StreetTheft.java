package nl.tno.idsa.library.actions;

import nl.tno.idsa.framework.semantics_impl.actions.Action;
import nl.tno.idsa.framework.semantics_impl.variables.GroupVariable;
import nl.tno.idsa.framework.world.GeometryType;
import nl.tno.idsa.library.actions.abstract_actions.Crime;
import nl.tno.idsa.library.models.ModelStreetTheft;

/**
 * Created by jongsd on 4-8-15.
 */
@SuppressWarnings("unused")
public class StreetTheft extends Action {

    public StreetTheft() {
        super(new ModelStreetTheft(),
                new GroupVariable(), new GroupVariable(), GeometryType.asSet(GeometryType.POINT), ProvidesAgents.NO, AllowsInsertMoveActionBefore.YES);
    }

    @Override
    @SuppressWarnings("unchecked") // Generic array.
    protected Class<? extends Action>[] createSemanticSuperclassArray() {
        return new Class[]{Crime.class};
    }
}