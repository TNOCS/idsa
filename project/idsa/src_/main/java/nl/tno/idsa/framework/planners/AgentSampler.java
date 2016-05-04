package nl.tno.idsa.framework.planners;

import nl.tno.idsa.framework.agents.Agent;
import nl.tno.idsa.framework.semantics_impl.roles.Role;
import nl.tno.idsa.framework.world.Point;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

// TODO Document class.

public class AgentSampler {

    private static final int MAX_RANDOM_AGENT_SAMPLES = 10000;          // TODO Magic number.

    private final List<Agent> agents;
    private final int numAgents;
    private final AgentFinder agentFinder;

    public AgentSampler(List<Agent> agents, int numAgents, Class<? extends Role> memberRole, double maxDistanceTo, Point target, double improvementBound) {
        this.agents = agents;
        this.numAgents = Math.max(1, numAgents);
        this.agentFinder = new AgentFinder(agents, memberRole, target, maxDistanceTo, improvementBound);
    }

    // TODO add generic distance constraints
    public List<Agent> sampleAgents() {
        List<Agent> result = new ArrayList<>(numAgents);
        int[] selectedAgents = new int[numAgents];
        Arrays.fill(selectedAgents, 0, selectedAgents.length, -1);
        int numRandomSamples = 0;
        int linearAgentIndex = -1;
        for (int i = 0; i < numAgents; ++i) {
            // Random search
            if (numRandomSamples < MAX_RANDOM_AGENT_SAMPLES) {
                boolean found = false;
                while (!found && numRandomSamples < MAX_RANDOM_AGENT_SAMPLES) {
                    int randomAgentIndex = agentFinder.findSuitableAgentUsingRandomSearch(selectedAgents, numRandomSamples, MAX_RANDOM_AGENT_SAMPLES);
                    if (randomAgentIndex >= 0) {
                        selectedAgents[i] = randomAgentIndex;
                        result.add(agents.get(randomAgentIndex));
                        found = true;
                    }
                    ++numRandomSamples;
                }
            } else {
                // Linear search, starting from previously found agent index
                linearAgentIndex = agentFinder.findSuitableAgentUsingLinearSearch(selectedAgents, linearAgentIndex + 1);
                if (linearAgentIndex >= 0) {
                    selectedAgents[i] = linearAgentIndex;
                    result.add(agents.get(linearAgentIndex));
                }
            }
        }
        return result;
    }
}
