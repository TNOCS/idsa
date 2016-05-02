package nl.tno.idsa.library.actions;

import nl.tno.idsa.framework.semantics_impl.actions.Action;
import nl.tno.idsa.framework.semantics_impl.locations.LocationAndTime;
import nl.tno.idsa.framework.semantics_impl.variables.GroupVariable;
import nl.tno.idsa.framework.world.GeometryType;
import nl.tno.idsa.library.actions.abstract_actions.RoadBlock;
import nl.tno.idsa.library.models.ModelParticipateInProcession;

/**
 * Created by jongsd on 4-8-15.
 */
@SuppressWarnings("unused")
public class ParticipateInProcession extends Action {

    private LocationAndTime route;
    private double walkingSpeedKmH;
    private double delayBetweenParticipantsS;

    public ParticipateInProcession() {
        super(new ModelParticipateInProcession(),
                new GroupVariable(), null, GeometryType.asSet(GeometryType.POINT), ProvidesAgents.NO, AllowsInsertMoveActionBefore.YES);
        setMovesActor(false); // Yes, it does, but we do not want this action to be used as a move action!
    }

    @Override
    public ModelParticipateInProcession getModel() {
        return (ModelParticipateInProcession) super.getModel();
    }

    @Override
    @SuppressWarnings("unchecked") // Generic array.
    protected Class<? extends Action>[] createSemanticSuperclassArray() {
        return new Class[]{RoadBlock.class};
    }
}
