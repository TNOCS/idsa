package nl.tno.idsa.framework.behavior.planners;

import nl.tno.idsa.framework.agents.Agent;
import nl.tno.idsa.framework.semantics_impl.roles.Role;
import nl.tno.idsa.framework.utils.RandomNumber;
import nl.tno.idsa.framework.world.Point;

import java.util.List;

/**
 * Find an agent within a certain range that can fulfill a certain role.
 */
public class AgentFinder {

    private final List<Agent> agents;
    private final Class<? extends Role> memberRole;
    private final double maxDistanceTo;
    private final Point target;
    private final double improvementBound;

    public AgentFinder(List<Agent> agents, Class<? extends Role> memberRole, Point target, double maxDistanceTo, double improvementBound) {
        this.agents = agents;
        this.memberRole = memberRole;
        this.maxDistanceTo = maxDistanceTo;
        this.target = target;
        this.improvementBound = improvementBound;
    }

    public int findSuitableAgentUsingRandomSearch(final int[] selectedAgents, final int numSamplesSoFar, final int numSamplesMax) {
        int result = -1;
        int agentIndex = RandomNumber.nextInt(agents.size());
        if (!containsIndex(selectedAgents, agentIndex) && agents.get(agentIndex).supportsRole(memberRole)) {
            Agent a = agents.get(agentIndex);
            double requiredMaxDistance = getRequiredMaxDistance(maxDistanceTo, numSamplesSoFar, numSamplesMax, improvementBound);
            if (target == null || a.getLocation().euclideanDistanceTo(target) < requiredMaxDistance) {
                result = agentIndex;
            }
        }
        return result;
    }

    public int findSuitableAgentUsingLinearSearch(final int[] selectedAgents, final int startAgentIndex) {
        int result = -1;
        boolean found = false;
        for (int j = startAgentIndex; !found && j < agents.size(); ++j) {
            if (!containsIndex(selectedAgents, j) && agents.get(j).supportsRole(memberRole)) {
                Agent a = agents.get(j);
                if (target == null || a.getLocation().euclideanDistanceTo(target) < maxDistanceTo) {
                    result = j;
                    found = true;
                }
            }
        }
        return result;
    }

    private static double getRequiredMaxDistance(double baseMaxDistance, int numSamples, int maxSamples, double improvementBound) {
        double result = baseMaxDistance;
        if (numSamples > (3 * maxSamples / 4)) {
            result *= 1 - ((1 - improvementBound) / 4);
        } else if (numSamples > (maxSamples / 2)) {
            result *= 1 - ((1 - improvementBound) / 2);
        } else {
            result *= improvementBound;
        }
        return result;
    }

    // TODO This method can move to a utility class.
    private static boolean containsIndex(int[] indices, int index) {
        boolean result = false;
        for (int i = 0; !result && i < indices.length; ++i) {
            result = index == indices[i];
        }
        return result;
    }
}
