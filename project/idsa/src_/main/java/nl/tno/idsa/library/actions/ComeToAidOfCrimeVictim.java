package nl.tno.idsa.library.actions;

import nl.tno.idsa.framework.semantics_impl.actions.Action;
import nl.tno.idsa.framework.semantics_impl.variables.GroupVariable;
import nl.tno.idsa.framework.world.GeometryType;
import nl.tno.idsa.library.actions.abstract_actions.ComeToAid;
import nl.tno.idsa.library.actions.abstract_actions.RoadBlock;
import nl.tno.idsa.library.models.ModelComeToAidOfCrimeVictim;

/**
 * Created by jongsd on 4-10-15.
 */
public class ComeToAidOfCrimeVictim extends Action {
    public ComeToAidOfCrimeVictim() {
        super(new ModelComeToAidOfCrimeVictim(),
                new GroupVariable(), new GroupVariable(), GeometryType.asSet(GeometryType.POINT), Action.ProvidesAgents.NO, Action.AllowsInsertMoveActionBefore.YES);
    }


    @Override
    @SuppressWarnings("unchecked") // Generic array.
    protected Class<? extends Action>[] createSemanticSuperclassArray() {
        return new Class[]{ComeToAid.class, RoadBlock.class};
    }

}
