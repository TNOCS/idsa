package nl.tno.idsa.library.activities.multipliers;

import nl.tno.idsa.framework.agents.Agent;
import nl.tno.idsa.framework.behavior.likelihoods.ActivityLikelihoodMap;
import nl.tno.idsa.framework.behavior.multipliers.ITimeOfYear;
import nl.tno.idsa.library.activities.possible.PossibleBeShopping;

/**
 * Created by kleina on 27-10-2015.
 */
@SuppressWarnings("unused")

// TODO Time of year is hardly (ever?) used.

public class PreChristmas implements ITimeOfYear {

    @Override
    public int getIndex() {
        return 10; // Whatever :)
    }

    @Override
    public void applyMultipliers(Agent agent, ActivityLikelihoodMap agentPossibilities) {

        //higher chance to go shopping
        agentPossibilities.multiplyLikelihoods(PossibleBeShopping.class, 2.5);
    }
}
