package nl.tno.idsa.library.activities.multipliers;

import nl.tno.idsa.framework.agents.Agent;
import nl.tno.idsa.framework.behavior.likelihoods.ActivityLikelihoodMap;
import nl.tno.idsa.framework.behavior.multipliers.ISeason;
import nl.tno.idsa.library.activities.possible.PossibleBeAtPark;
import nl.tno.idsa.library.activities.possible.PossibleBeAtSportsField;

@SuppressWarnings("unused")
public class Winter implements ISeason {

    @Override
    public void applyMultipliers(Agent agent, ActivityLikelihoodMap agentPossibilities) {
        agentPossibilities.multiplyLikelihoods(PossibleBeAtPark.class, 0.25);
        agentPossibilities.multiplyLikelihoods(PossibleBeAtSportsField.class, 0.25);
    }

    @Override
    public int getIndex() {
        return 0;
    }
}
