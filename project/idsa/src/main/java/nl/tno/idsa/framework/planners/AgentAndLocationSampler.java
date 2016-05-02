package nl.tno.idsa.framework.planners;

import nl.tno.idsa.framework.agents.Agent;
import nl.tno.idsa.framework.semantics_impl.groups.Group;
import nl.tno.idsa.framework.semantics_impl.locations.LocationAndTime;
import nl.tno.idsa.framework.semantics_impl.variables.GroupVariable;
import nl.tno.idsa.framework.semantics_impl.variables.LocationVariable;
import nl.tno.idsa.framework.semantics_impl.variables.Variable;
import nl.tno.idsa.framework.utils.RandomNumber;
import nl.tno.idsa.framework.world.*;

import java.util.List;

// TODO Document class.

public class AgentAndLocationSampler {

    public static final int MAX_SAMPLE_ATTEMPTS = 2; // TODO Magic constants.
    public static final int MAX_REFINING_ITERATIONS = 20;
    public static final double CLOSER_SAMPLING_FACTOR_LOCATIONS = 0.5;
    public static final double CLOSER_SAMPLING_FACTOR_GROUPS = 0.8;

    private static final boolean PRINT_DEBUG = false;  // TODO Replace by proper logging.

    private static void f(String s, Object... vars) {  // TODO Replace by proper logging.
        for (int i = 0; i < vars.length; i++) {
            if (vars[i] instanceof Class) {
                vars[i] = ((Class) vars[i]).getSimpleName();
            }
        }
        if (PRINT_DEBUG) {
            System.out.println(String.format(s, vars));
        }
    }

    public static boolean instantiatePlan(Environment environment, ActionPlan plan) {

        long currentTime = environment.getTime().getNanos();
        long requiredEndTime = plan.getGoalAction().getLocationVariable().getValue().getTimeNanos();
        long requiredDuration = requiredEndTime - currentTime;

        boolean planFound = false;
        int samplingIteration = 0;
        while (samplingIteration < MAX_SAMPLE_ATTEMPTS && !planFound) {
            samplingIteration++;
            // Initial random values
            try {
                sampleValues(environment, plan);

                f("%nSAMPLE %s%nINITIAL PLAN: %n%s%n.", samplingIteration, plan);

            } catch (Exception e) {
                e.printStackTrace(); // TODO More graceful error handling.
                return false;
            }

            // Evaluate timing
            long estimatedDuration = plan.estimateDuration(environment, false); // Do not include the duration of the goal action.

            f("Initial estimated duration is %s.", Time.durationToString(estimatedDuration));

            //temp comment below
            //System.out.println("Sample " + samplingIteration + " Refinement " + 0 + " Estimated duration: " + Time.durationToString(estimatedDuration) + " Required duration: " + Time.durationToString(requiredDuration)); // TODO REMOVE PRINT

            //Reselect one of the variables, shortening trip distances.
            int refiningIteration = 0;
            if (estimatedDuration > requiredDuration) {
                while (estimatedDuration > requiredDuration && refiningIteration < MAX_REFINING_ITERATIONS) {

                    //Determine a restricting variable.
                    //Variable var = plan.getRandomVariable(rnd);
                    Variable var = plan.getRestrictingVariable(environment, true, false);

                    f("Optimizing for variable %s.", var);

                    // Check how to optimize the selected var
                    if (var != null && var.getValue() != null) {

                        if (var instanceof LocationVariable) { //Then sample closer to the next location in the plan.
                            LocationVariable currentLocation = (LocationVariable) var;

                            // Target for var currentLocation
                            LocationVariable nextLocation = plan.getNextLocationInPlan(currentLocation);

                            f("Optimizing for location variable %s to move closer to %s.", var, nextLocation);

                            if (nextLocation != null) {
                                Point tgt = nextLocation.getValue().getLocation().getFirstPoint();
                                if (!currentLocation.isRestrictedToAgentLocations()) {
                                    f("Location variable is not restricted to locations of agents.");
                                    Vertex newLocVertex = environment.getWorld().sampleVertexCloserTo(currentLocation, tgt, CLOSER_SAMPLING_FACTOR_LOCATIONS);
                                    if (newLocVertex != null) {
                                        Point newLoc = newLocVertex.getPoint();
                                        if (newLoc != null) {
                                            f("Found a location that matches the desired function and that is closer to %s, namely %s.", nextLocation, newLoc);
                                            currentLocation.setValue(new LocationAndTime(newLoc));
                                        } else {
                                            f("Could not find a location that matches the desired function and that is closer to %s.", nextLocation);
                                        }
                                    }
                                } else {
                                    f("Location variable is restricted to locations of agents.");
                                    List<Agent> agents = sampleAgentsCloserTo(environment.getAgents(), currentLocation.getAssociatedGroupVariable(), tgt, CLOSER_SAMPLING_FACTOR_GROUPS);
                                    if (agents != null && agents.size() > 0) {
                                        f("Found one or more agents that is/are closer to %s, namely %s.", nextLocation, agents);
                                        Agent agent = agents.get(0);   // This is an approximate, not all agents in the group may in fact be there.
                                        Point newLoc = agent.getLocation();
                                        if (newLoc != null) {
                                            currentLocation.setValue(new LocationAndTime(newLoc));
                                            currentLocation.getAssociatedGroupVariable().setValue(new Group(agents));
                                        }
                                    } else {
                                        f("Could not find one or more agents that is/are closer to %s.", nextLocation);
                                    }
                                }
                            }
                        } else if (var instanceof GroupVariable) { //Then sample this group closer to the next location.
                            f("Optimizing group variable %s.", var); // TODO More prints?
                            GroupVariable gv = (GroupVariable) var;
                            if (!gv.areAgentsProvided()) {
                                LocationVariable firstLocation = plan.getFirstLocationInPlan(gv);
                                if (firstLocation != null) {
                                    IGeometry tgt = firstLocation.getValue().getLocation();
                                    List<Agent> newAgents = sampleAgentsCloserTo(environment.getAgents(), gv, tgt, CLOSER_SAMPLING_FACTOR_GROUPS); //TODO: perhaps: we have information on the slowest agent in the group from getRestrictingVariabbles.

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
                        f("Current estimated duration is %s.", Time.durationToString(estimatedDuration));

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
                            numMembers = 1; // TODO Magic number.
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
