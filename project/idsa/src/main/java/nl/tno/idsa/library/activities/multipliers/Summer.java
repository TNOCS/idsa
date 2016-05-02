package nl.tno.idsa.library.activities.multipliers;

import nl.tno.idsa.framework.agents.Agent;
import nl.tno.idsa.framework.behavior.likelihoods.ActivityLikelihoodMap;
import nl.tno.idsa.framework.behavior.multipliers.ISeason;
import nl.tno.idsa.library.activities.possible.PossibleBeAtPark;
import nl.tno.idsa.library.activities.possible.PossibleBeAtSchool;
import nl.tno.idsa.library.activities.possible.PossibleBeAtWork;

/**
 * Created by kleina on 27-10-2015.
 */
@SuppressWarnings("unused")
public class Summer implements ISeason {

    @Override
    public void applyMultipliers(Agent agent, ActivityLikelihoodMap agentPossibilities) {
        //no school
        agentPossibilities.setLikelihoods(PossibleBeAtSchool.class, 0d);

        //less likely to work
        agentPossibilities.multiplyLikelihoods(PossibleBeAtWork.class, 0.8);

        //More likely to do outside activities
        agentPossibilities.multiplyLikelihoods(PossibleBeAtPark.class, 3d);
    }

    @Override
    public int getIndex() {
        return 2;
    }
}
