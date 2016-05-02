package nl.tno.idsa.library.actions;

import nl.tno.idsa.framework.semantics_impl.actions.Action;
import nl.tno.idsa.framework.semantics_impl.variables.GroupVariable;
import nl.tno.idsa.framework.world.GeometryType;
import nl.tno.idsa.library.actions.abstract_actions.RoadBlock;
import nl.tno.idsa.library.models.ModelArrest;

/**
 * Created by jongsd on 4-8-15.
 */

// TODO General comment for all library actions: document them and describe them, including the relations that are...
// applicable, or provide a good semantic library inspector tool that generates HTML to add to the documentation.


// TODO Although semantically correct, the fact that relations on preconditions etc. are not defined in action classes is confusing. ...
// Think about a way to make it less confusing for new users that relations must be defined separately.

@SuppressWarnings("unused")
public class Arrest extends Action {

    public Arrest() {
        super(new ModelArrest(),
                new GroupVariable(), new GroupVariable(), GeometryType.asSet(GeometryType.POINT), ProvidesAgents.NO, AllowsInsertMoveActionBefore.YES);
    }

    @Override
    @SuppressWarnings("unchecked") // Generic array.
    protected Class<? extends Action>[] createSemanticSuperclassArray() {
        return new Class[]{RoadBlock.class};
    }
}
