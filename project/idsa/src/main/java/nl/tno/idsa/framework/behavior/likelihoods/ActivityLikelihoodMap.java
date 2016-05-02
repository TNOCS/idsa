package nl.tno.idsa.framework.behavior.likelihoods;

import nl.tno.idsa.framework.agents.Agent;
import nl.tno.idsa.framework.behavior.activities.possible.PossibleActivity;

import java.util.HashMap;
import java.util.Set;

/**
 * Map between possible activities and likelihoods.
 */
public class ActivityLikelihoodMap extends HashMap<PossibleActivity, Double> {

    public Set<PossibleActivity> getPossibleActivities() {
        return keySet();
    }

    public double getLikelihood(PossibleActivity possibleActivity) {
        Double value = get(possibleActivity);
        return (value != null) ? value : 0d;
    }

    /**
     * Apply the initial multipliers of activities to
     * (1) the a priori likelihoods and
     * (2) a specific agent.
     * Then, return the new likelihoods for the agent.
     */
    public final ActivityLikelihoodMap initializeLikelihoods(Agent agent) {
        ActivityLikelihoodMap agentLikelihoods = new ActivityLikelihoodMap();
        for (PossibleActivity possibleActivity : getPossibleActivities()) {
            double initialLikelihood = getLikelihood(possibleActivity);
            double multiplier = possibleActivity.getMultiplier(agent);
            agentLikelihoods.put(possibleActivity, initialLikelihood * multiplier);
        }

        return agentLikelihoods;
    }

    /**
     * Set the likelihood of all possible activities with the given class to the given value.
     */
    public void setLikelihoods(Class<? extends PossibleActivity> possibleActivityClass, double value) {
        for (PossibleActivity possibleActivity : keySet()) {
            if (possibleActivity.getClass().equals(possibleActivityClass)) {
                put(possibleActivity, value);
            }
        }
    }

    /**
     * Multiply the likelihood of all possible activities with the given class with the given value.
     */
    public void multiplyLikelihoods(Class<? extends PossibleActivity> possibleActivityClass, double factor) {
        for (PossibleActivity possibleActivity : keySet()) {
            if (possibleActivity.getClass().equals(possibleActivityClass)) {
                multiplyLikelihood(possibleActivity, factor);
            }
        }
    }

    private void multiplyLikelihood(PossibleActivity possibleActivity, double factor) {
        put(possibleActivity, factor * getLikelihood(possibleActivity));
    }

    /**
     * Set the likelihood of all possible activities with the given class to zero.
     */
    public void setLikelihoodsToZero(Class<? extends PossibleActivity> possibleActivityClass) {
        for (PossibleActivity possibleActivity : keySet()) {
            if (possibleActivity.getClass().equals(possibleActivityClass)) {
                setLikelihoodToZero(possibleActivity);
            }
        }
    }

    private void setLikelihoodToZero(PossibleActivity possibleActivity) {
        put(possibleActivity, 0d);
    }
}
