package nl.tno.idsa.library.actions;

import nl.tno.idsa.framework.behavior.models.Model;
import nl.tno.idsa.framework.semantics_impl.actions.Action;
import nl.tno.idsa.framework.semantics_impl.variables.GroupVariable;
import nl.tno.idsa.framework.world.GeometryType;
import nl.tno.idsa.library.models.IdleModel;

import java.util.Set;

/**
 * Created by jongsd on 22-9-15.
 */
// TODO REMOVE THIS AND IDLEMODEL, REFACTOR ACTION TO HAVE ONE MODEL ONLY.
public abstract class LastMinuteAction extends Action {

    public LastMinuteAction(Model model, GroupVariable actorVariable, GroupVariable targetVariable, Set<GeometryType> allowedLocationTypes, ProvidesAgents providesAgents, AllowsInsertMoveActionBefore allowsInsertMoveActionBefore) {
        super(model, actorVariable, targetVariable, allowedLocationTypes, providesAgents, allowsInsertMoveActionBefore);

        // Force the model to wait until the last moment; add a secondary model.    TODO This is the only action requiring more than one model.
        IdleModel idleModel = new IdleModel();
        getModel().addModelToWaitFor(idleModel);
        getModels().add(idleModel);
    }
}
