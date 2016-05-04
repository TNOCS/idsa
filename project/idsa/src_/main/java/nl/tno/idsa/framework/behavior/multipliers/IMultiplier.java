package nl.tno.idsa.framework.behavior.multipliers;

import nl.tno.idsa.framework.agents.Agent;
import nl.tno.idsa.framework.behavior.likelihoods.ActivityLikelihoodMap;

/**
 * Multipliers for likelihoods implement this interface.
 */
public interface IMultiplier {
    public void applyMultipliers(Agent agent, ActivityLikelihoodMap currentLikelihoods);
}
