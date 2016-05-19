package nl.tno.idsa.framework.behavior.planners;

import nl.tno.idsa.Constants;
import nl.tno.idsa.framework.agents.Agent;
import nl.tno.idsa.framework.behavior.plans.ActionPlan;
import nl.tno.idsa.framework.semantics_impl.groups.Group;
import nl.tno.idsa.framework.semantics_impl.locations.LocationAndTime;
import nl.tno.idsa.framework.semantics_impl.variables.GroupVariable;
import nl.tno.idsa.framework.semantics_impl.variables.LocationVariable;
import nl.tno.idsa.framework.semantics_impl.variables.Variable;
import nl.tno.idsa.framework.utils.RandomNumber;
import nl.tno.idsa.framework.world.*;
import nl.tno.idsa.tools.DebugPrinter;

import java.util.List;

/**
 * Find agents and locations for an action plan such that it can be executed exactly in time.
 */
public class IncidentAgentAndLocationSampler {

    public static boolean instantiatePlan(Environment environment, ActionPlan plan) {

        long currentTime = environment.getTime().getNanos();
        long requiredEndTime = plan.getGoalAction().getLocationVariable().getValue().getTimeNanos();
        long requiredDuration = requiredEndTime - currentTime;

        boolean planFound = false;
        int samplingIteration = 0;
        while (samplingIteration < Constants.INCIDENT_MAX_SAMPLE_ATTEMPTS && !planFound) {
            samplingIteration++;
            // Initial random values
            try {
                sampleValues(environment, plan);
                DebugPrinter.println("%nSAMPLE %s%nINITIAL PLAN: %n%s%n.", samplingIteration, plan);
            } catch (Exception e) {
                e.printStackTrace(); // TODO More graceful error handling.
                return false;
            }

            // Evaluate timing
            long estimatedDuration = plan.estimateDuration(environment, false); // Do not include the duration of the goal action.
            DebugPrinter.println("Initial estimated duration is %s.", Time.durationToString(estimatedDuration));

            // Reselect one of the variables, shortening trip distances.
            int refiningIteration = 0;
            if (estimatedDuration > requiredDuration) {
                while (estimatedDuration > requiredDuration && refiningIteration < Constants.INCIDENT_MAX_REFINING_ITERATIONS) {

                    // Determine a restricting variable.
                    Variable var = plan.getRestrictingVariable(environment, true);
                    DebugPrinter.println("Optimizing for variable %s.", var);

                    // Check how to optimize the selected variable.
                    if (var != null && var.getValue() != null) {

                        if (var instanceof LocationVariable) { // Then sample closer to the next location in the plan.
                            LocationVariable currentLocation = (LocationVariable) var;

                            // Target for variable currentLocation.
                            LocationVariable nextLocation = plan.getNextLocationInPlan(currentLocation);
                            DebugPrinter.println("Optimizing for location variable %s to move closer to %s.", var, nextLocation);

                            if (nextLocation != null) {
                                Point tgt = nextLocation.getValue().getLocation().getFirstPoint();
                                if (!currentLocation.isRestrictedToAgentLocations()) {
                                    DebugPrinter.println("Location variable is not restricted to locations of agents.");
                                    Vertex newLocVertex = environment.getWorld().sampleVertexCloserTo(currentLocation, tgt, Constants.INCIDENT_CLOSER_SAMPLING_FACTOR_LOCATIONS);
                                    if (newLocVertex != null) {
                                        Point newLoc = newLocVertex.getPoint();
                                        if (newLoc != null) {
                                            DebugPrinter.println("Found a location that matches the desired function and that is closer to %s, namely %s.", nextLocation, newLoc);
                                            currentLocation.setValue(new LocationAndTime(newLoc));
                                        } else {
                                            DebugPrinter.println("Could not find a location that matches the desired function and that is closer to %s.", nextLocation);
                                        }
                                    }
                                } else {
                                    DebugPrinter.println("Location variable is restricted to locations of agents.");
                                    List<Agent> agents = sampleAgentsCloserTo(environment.getAgents(), currentLocation.getAssociatedGroupVariable(), tgt, Constants.INCIDENT_CLOSER_SAMPLING_FACTOR_GROUPS);
                                    if (agents != null && agents.size() > 0) {
                                        DebugPrinter.println("Found one or more agents that is/are closer to %s, namely %s.", nextLocation, agents);
                                        Agent agent = agents.get(0);   // This is an approximate, not all agents in the group may in fact be there.
                                        Point newLoc = agent.getLocation();
                                        if (newLoc != null) {
                                            currentLocation.setValue(new LocationAndTime(newLoc));
                                            currentLocation.getAssociatedGroupVariable().setValue(new Group(agents));
                                        }
                                    } else {
                                        DebugPrinter.println("Could not find one or more agents that is/are closer to %s.", nextLocation);
                                    }
                                }
                            }
                        } else if (var instanceof GroupVariable) { //Then sample this group closer to the next location.
                            DebugPrinter.println("Optimizing group variable %s.", var);
                            GroupVariable gv = (GroupVariable) var;
                            if (!gv.areAgentsProvided()) {
                                LocationVariable firstLocation = plan.getFirstLocationInPlan(gv);
                                if (firstLocation != null) {
                                    IGeometry tgt = firstLocation.getValue().getLocation();
                                    List<Agent> newAgents = sampleAgentsCloserTo(environment.getAgents(), gv, tgt, Constants.INCIDENT_CLOSER_SAMPLING_FACTOR_GROUPS); //TODO: perhaps: we have information on the slowest agent in the group from getRestrictingVariables.

                                    // If enough agents have been found closer to the target
                                    if (newAgents.size() >= gv.getValue().size()) {
                                        gv.setValue(new Group(newAgents));
                                    } else {
                                        // TODO What do we do if we cannot find a sufficient number of agents?
                                    }
                                }
                            }
                        }
                        // Re-evaluate plan timings
                        estimatedDuration = plan.estimateDuration(environment, false); // Without the goal action.
                        DebugPrinter.println("Current estimated duration is %s.", Time.durationToString(estimatedDuration));

                        if (estimatedDuration < requiredDuration) {
                            planFound = true;
                        }
                        ++refiningIteration;
                    }
                }
            } else {
                planFound = true;
            }
        }

        // Make sure the plan starts and finishes exactly on time
        if (planFound) {
            plan.adjustPlanLength(currentTime, requiredEndTime);
        }

        // Otherwise just fix the starting time.
        else {
            plan.adjustPlanLength(currentTime);
        }

        return planFound;
    }

    public static List<Agent> sampleAgentsCloserTo(List<Agent> agents, GroupVariable gv, IGeometry tgt, double improvementBound) {
        return new AgentSampler(agents, gv.getNumMembers(), gv.getMemberRole(), maxDistanceTo(gv.getValue(), tgt), tgt.getFirstPoint(), improvementBound).sampleAgents();
    }

    private static double maxDistanceTo(Group agents, IGeometry tgt) {
        double result = Double.NEGATIVE_INFINITY;
        for (Agent a : agents) {
            double distance;
            if ((distance = a.getLocation().euclideanDistanceTo(tgt.getFirstPoint())) > result) {
                result = distance;
            }
        }
        return result;
    }

    private static void sampleValues(Environment environment, ActionPlan plan)
            throws Exception {

        // Find random instances for all open variables
        for (Variable v : plan.getVariables()) {
            if (v.getValue() == null) {

                // Select a suitable location for this variable if this is needed.
                if (v instanceof LocationVariable) {
                    LocationVariable lv = (LocationVariable) v;

                    // Choose an agent if the action needs to happen with an agent.
                    if (lv.isRestrictedToAgentLocations()) {
                        List<Agent> agents = environment.getAgents();
                        Agent sampledAgent = RandomNumber.randomElement(agents);
                        lv.setValue(new LocationAndTime(sampledAgent.getLocation()));
                        lv.getAssociatedGroupVariable().setValue(new Group(sampledAgent));
                    }

                    // Choose a location otherwise.
                    else {
                        List<Vertex> suitable = environment.getWorld().getVertices(lv.getAllowedFunctions(), true); // Any function.
                        if (suitable.size() > 0) {
                            int index = RandomNumber.nextInt(suitable.size());
                            lv.setValue(new LocationAndTime(suitable.get(index).getPoint()));
                        } else {
                            throw new Exception("No suitable location found for " + lv + ".");
                        }
                    }
                }

                // Select a suitable agent/group of agents for this variable if this is needed.
                else if (v instanceof GroupVariable) {

                    // Select a suitable group of agents for this variable.
                    GroupVariable gv = (GroupVariable) v;

                    // To spawn?
                    if (!gv.areAgentsProvided()) {
                        int numMembers = gv.getNumMembers();
                        if (numMembers == GroupVariable.ANY_NUMBER_OF_MEMBERS) {
                            numMembers = 1; // TODO If we don't specify how many members are needed, we always assume we need one.
                        }
                        List<Agent> agents = new AgentSampler(environment.getAgents(), numMembers, gv.getMemberRole(), Double.MAX_VALUE, null, 1).sampleAgents();
                        Group group = new Group(agents);
                        gv.setValue(group);
                    } else {
                        // Ignore, as there will be an appropriate spawn location in the location variables
                    }
                }
            }
        }
    }
}
