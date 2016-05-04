package nl.tno.idsa.framework.behavior.planners;

import nl.tno.idsa.framework.agents.Agent;
import nl.tno.idsa.framework.behavior.activities.concrete.Activity;
import nl.tno.idsa.framework.behavior.activities.concrete.BasicMovementActivity;
import nl.tno.idsa.framework.behavior.activities.possible.PossibleActivity;
import nl.tno.idsa.framework.behavior.likelihoods.ActivityLikelihoodMap;
import nl.tno.idsa.framework.behavior.likelihoods.DayOfWeek;
import nl.tno.idsa.framework.behavior.multipliers.ISeason;
import nl.tno.idsa.framework.behavior.multipliers.ITimeOfYear;
import nl.tno.idsa.framework.messaging.ProgressNotifier;
import nl.tno.idsa.framework.semantics_base.SemanticLibrary;
import nl.tno.idsa.framework.semantics_impl.groups.Group;
import nl.tno.idsa.framework.semantics_impl.locations.LocationFunction;
import nl.tno.idsa.framework.utils.RandomNumber;
import nl.tno.idsa.framework.utils.TextUtils;
import nl.tno.idsa.framework.utils.Tuple;
import nl.tno.idsa.framework.world.Area;
import nl.tno.idsa.framework.world.Time;
import nl.tno.idsa.framework.world.Vertex;
import nl.tno.idsa.framework.world.World;
import nl.tno.idsa.tools.DebugPrinter;

import java.util.*;

/**
 * Create agendas for the entire population.
 */
public class AgendaPlanner {

    // Settings and data structures for debugging. *********************************************************************

    private final static int DEBUG_MAX_HOUSEHOLDS_TO_PROCESS = Integer.MAX_VALUE;
    private final static int DEBUG_NUM_SAMPLE_AGENDAS = 0;
    private final static boolean DEBUG_QUIT_WHEN_DONE_MAKING_AGENDAS = false;
    private static TreeMap<PossibleActivity, Tuple<Integer, Integer>> successesAndFailures = new TreeMap<>();

    private static void addSuccess(PossibleActivity pa) {
        Tuple<Integer, Integer> sf = successesAndFailures.get(pa);
        if (sf == null) {
            sf = new Tuple<>(1, 0);
            successesAndFailures.put(pa, sf);
        } else {
            successesAndFailures.put(pa, new Tuple<Integer, Integer>(sf.getFirst() + 1, sf.getSecond()));
        }
    }

    private static void addFailure(PossibleActivity pa) {
        Tuple<Integer, Integer> sf = successesAndFailures.get(pa);
        if (sf == null) {
            sf = new Tuple<>(0, 1);
            successesAndFailures.put(pa, sf);
        } else {
            successesAndFailures.put(pa, new Tuple<Integer, Integer>(sf.getFirst(), sf.getSecond() + 1));
        }
    }

    // End settings and data structures for debugging. *****************************************************************

    private final ISeason season; // TODO A better approach would be to have a list of IMultiplier implementations.
    private final ITimeOfYear timeOfTheYear;
    private final DayOfWeek dayOfWeek;
    private final World world;

    private final Map<Agent, ActivityLikelihoodMap> activityProbabilities;

    public AgendaPlanner(ISeason season, ITimeOfYear timeOfTheYear, DayOfWeek dayOfWeek, World world) {
        this.season = season;
        this.timeOfTheYear = timeOfTheYear;
        this.dayOfWeek = dayOfWeek;
        this.world = world;
        this.activityProbabilities = new HashMap<>(30000);
    }

    public void createAgendas(Set<Group> households) {

        // Administer progress.
        int current = 0;
        double total = 19 * households.size(); // Magic numbers that make the progress look a bit more uniform.

        // Apply multipliers.
        ProgressNotifier.notifyProgressMessage("Applying multipliers...");
        for (Group household : households) {
            for (Agent agent : household) {
                applyMultipliers(agent);
            }
            int current1 = current++;
            ProgressNotifier.notifyProgress(current1 / total);
        }

        // Give an overview of the expected number of agents that will do each activity and the available capacity.
        // TODO Move to logger.
        Map<String, Double> overviewMultipliers = createOverviewMultipliers();
        TreeMap<String, Integer> overviewCapacities = createOverviewCapacities();
        System.out.println("\n\n" + TextUtils.addSpacesToString("Activity occurrences", 30) + TextUtils.addSpacesToString("Expected", 10) + TextUtils.addSpacesToString("Capacity", 10));
        for (String paS : overviewMultipliers.keySet()) {
            Integer cap = overviewCapacities.get(paS);
            String capS;
            if (cap < 0 || cap == Integer.MAX_VALUE) {
                capS = "Unlimited";
            } else {
                capS = cap.toString();
            }
            System.out.println(TextUtils.addSpacesToString(paS, 30) + TextUtils.addSpacesToString((int) Math.round(overviewMultipliers.get(paS)), 10) + TextUtils.addSpacesToString(capS, 10));
        }

        // Plan activities.
        ProgressNotifier.notifyProgressMessage("Planning activities...");
        int i = 0;
        int h = 0;
        for (Group household : households) {
            planActivities(household);
            int current1 = current = current + 16;
            ProgressNotifier.notifyProgress(current1 / total);
            ProgressNotifier.notifyProgressMessage("Planning activities... Household " + ++h + " of " + households.size());
            if (i++ > DEBUG_MAX_HOUSEHOLDS_TO_PROCESS) {
                break;
            }
        }

        // Optimize activities.
        ProgressNotifier.notifyProgressMessage("Optimizing activities...");
        DebugPrinter.println("\n\n\n");
        i = 0;
        h = 0;
        for (Group household : households) {
            optimizeActivities(household);
            int current1 = current = current + 2;
            ProgressNotifier.notifyProgress(current1 / total);
            ProgressNotifier.notifyProgressMessage("Optimizing activities... Household " + ++h + " of " + households.size());
            if (i++ > DEBUG_MAX_HOUSEHOLDS_TO_PROCESS) {
                break;
            }
        }

        // Show a sample of a number of agendas.
        // TODO Move to logger.
        int sampleSize = DEBUG_NUM_SAMPLE_AGENDAS;
        if (sampleSize != 0) {
            System.out.println("\n\nSome sample agendas follow.\n");
            for (Group household : households) {
                for (Agent agent : household) {
                    if (RandomNumber.nextDouble() < 0.1) {
                        if (agent.getAgenda() != null && agent.getAgenda().size() > 0) {
                            // find agendas with joint activities to test them
                            Set<Agent> agentsWithCommonActivities = agent.getAgentsWithCommonActivities();
                            if (agentsWithCommonActivities.size() > 1) {
                                System.out.println("\n\nAgent " + agent + " has joint activities, showing all agendas.\n");
                                for (Agent a : agentsWithCommonActivities) {
                                    System.out.println(a + "\n---");
                                    System.out.println(createOverviewAgenda(a));
                                }
                            } else {
                                System.out.println("\n\nAgent " + agent + " has no joint activities, showing only this agent's agenda.\n---");
                                System.out.println(createOverviewAgenda(agent));
                            }
                            if (--sampleSize == 0) {
                                break;
                            }
                        }
                    }
                }
            }
        }

        // Show a summary of possible activities that have been successfully transformed into planned activities. Or not.
        // TODO Move to a logger.
        System.out.println("\n\n" +
                TextUtils.addSpacesToString("Summary of successes and failures in planning", 50) +
                TextUtils.addSpacesToString("Success", 8) +
                TextUtils.addSpacesToString("Fail", 8) +
                TextUtils.addSpacesToString("Total", 8));
        for (PossibleActivity pa : successesAndFailures.keySet()) {
            Tuple<Integer, Integer> sf = successesAndFailures.get(pa);
            int s = sf.getFirst();
            int f = sf.getSecond();
            System.out.println(
                    TextUtils.addSpacesToString(pa, 50) +
                            TextUtils.addSpacesToString(s, 8) +
                            TextUtils.addSpacesToString(f, 8) +
                            TextUtils.addSpacesToString(s + f, 8));
        }

        // Show a summary of planned activities. This includes walking activities.
        // TODO Move to a log or output.
        HashMap<String, Integer> summary = new HashMap<>();
        for (Group household : households) {
            for (Agent agent : household) {
                for (Activity activity : agent.getAgenda()) {
                    if (summary.containsKey(activity.getName())) {
                        summary.put(activity.getName(), summary.get(activity.getName()) + 1);
                    } else {
                        summary.put(activity.getName(), 1);
                    }
                }
            }
        }
        System.out.println("\n\nOccurrences for each activity:");
        ArrayList<String> sortedActivities = new ArrayList<>(summary.keySet());
        Collections.sort(sortedActivities);
        for (String activity : sortedActivities) {
            System.out.format("%s: %d%n", TextUtils.addSpacesToString(activity, 30), summary.get(activity));
        }

        // Show status: number of agents that stay at home.
        // TODO Move to a logger.
        int numStayAtHomeAgents = 0, numAgents = 0;
        for (Group household : households)
            for (Agent agent : household) {
                Vertex house = agent.getHouse();
                boolean agendaOnlyAtHome = true;
                for (Activity activity : agent.getAgenda()) {
                    if (!(activity.getStartLocation().equals(house) && activity.getEndLocation().equals(house))) {
                        agendaOnlyAtHome = false;
                        break;
                    }
                }
                if (agendaOnlyAtHome) {
                    numStayAtHomeAgents++;
                }
                numAgents++;
            }
        System.out.println("\n\nStay-at-home agents: " + numStayAtHomeAgents + " of " + numAgents);

        if (DEBUG_QUIT_WHEN_DONE_MAKING_AGENDAS) {
            System.exit(0);
        }
    }

    private Map<String, Double> createOverviewMultipliers() {
        Map<String, Double> overview = new TreeMap<>();
        for (Agent agent : activityProbabilities.keySet()) {
            ActivityLikelihoodMap activityLikelihoodMap = activityProbabilities.get(agent);
            Set<PossibleActivity> possibleActivities = activityLikelihoodMap.getPossibleActivities();
            for (PossibleActivity possibleActivity : possibleActivities) {
                double likelihood = activityLikelihoodMap.getLikelihood(possibleActivity);
                String paS = possibleActivity.toString();
                Double multiplier = overview.get(paS);
                if (multiplier == null) {
                    multiplier = likelihood;
                } else {
                    multiplier = multiplier + likelihood;
                }
                overview.put(paS, multiplier);
            }
        }
        return overview;
    }

    @SuppressWarnings("unchecked")
    private TreeMap<String, Integer> createOverviewCapacities() {
        HashMap<Class<? extends LocationFunction>, Integer> count = new HashMap<>();
        for (Area a : world.getAreas()) {
            List<LocationFunction> functions = a.getFunctions();
            for (LocationFunction function : functions) {
                int capacity = function.getCapacity();
                if (capacity > 0) {
                    Class<? extends LocationFunction> fClass = function.getClass();
                    createOverviewCapacities_update(count, capacity, fClass);
                    // Also count the capacity for all the superclasses.
                    Set<Class<? extends LocationFunction>> superclasses = SemanticLibrary.getInstance().listSemanticSuperclasses(fClass);
                    for (Class<? extends LocationFunction> superclass : superclasses) {
                        createOverviewCapacities_update(count, capacity, superclass);
                    }
                }
            }

        }
        TreeMap<String, Integer> overview = new TreeMap<>();
        Set<PossibleActivity> allActivities = dayOfWeek.keySet();
        for (PossibleActivity activity : allActivities) {
            Class<? extends LocationFunction>[] possibleLocationFunctions = activity.getPossibleLocationFunctions();
            int countFS = 0;
            for (Class<? extends LocationFunction> possibleLocationFunction : possibleLocationFunctions) {
                Integer countF = count.get(possibleLocationFunction);
                if (countF != null) {
                    if (countF == Integer.MAX_VALUE || countFS == Integer.MAX_VALUE) {
                        countFS = Integer.MAX_VALUE;
                    } else {
                        countFS += countF;
                    }
                }
            }
            overview.put(activity.toString(), countFS);
        }
        return overview;
    }

    private void createOverviewCapacities_update(HashMap<Class<? extends LocationFunction>, Integer> count, int capacity, Class<? extends LocationFunction> fClass) {
        Integer countF = count.get(fClass);
        if (countF == null) {
            countF = capacity;
        } else if (countF == Integer.MAX_VALUE || capacity == Integer.MAX_VALUE) {
            countF = Integer.MAX_VALUE;
        } else {
            countF += capacity;
        }
        count.put(fClass, countF);
    }

    private void applyMultipliers(Agent agent) {
        ActivityLikelihoodMap agentProbabilities = dayOfWeek.initializeLikelihoods(agent);
        if (season != null) {
            season.applyMultipliers(agent, agentProbabilities);
        }
        if (timeOfTheYear != null) {
            timeOfTheYear.applyMultipliers(agent, agentProbabilities);
        }
        activityProbabilities.put(agent, agentProbabilities);
    }

    private void planActivities(Group household) {

        // Create the priority maps.
        // priority -> #participants -> agent -> activity[]
        SortedMap<Integer, SortedMap<Integer, HashMap<Agent, List<PossibleActivity>>>> prioritizedActivities = new TreeMap<>();
        for (Agent agent : household) {
            ActivityLikelihoodMap activityLikelihoodMap = activityProbabilities.get(agent);

            DebugPrinter.println("Agent: " + agent);
            DebugPrinter.println("Possible activities: " + activityLikelihoodMap);

            for (PossibleActivity possibleActivity : activityLikelihoodMap.getPossibleActivities()) {

                // Look up priority.
                final double priority = possibleActivity.getPriority();
                int priorityInt = (int) (100 * (1 - priority)); // Round-off errors if we use doubles; reverse so standard sort works.
                SortedMap<Integer, HashMap<Agent, List<PossibleActivity>>> activitiesGivenPriority = prioritizedActivities.get(priorityInt);
                if (activitiesGivenPriority == null) {
                    activitiesGivenPriority = new TreeMap<>();
                    prioritizedActivities.put(priorityInt, activitiesGivenPriority);
                }

                // Look up number of participants.
                int numParticipants = possibleActivity.getNumberAgentsInvolved(agent);
                if (numParticipants != 0) {
                    numParticipants = Integer.MAX_VALUE - numParticipants; // Reverse so standard sort works.
                    HashMap<Agent, List<PossibleActivity>> activitiesGivenPriorityAndNumParticipants = activitiesGivenPriority.get(numParticipants);
                    if (activitiesGivenPriorityAndNumParticipants == null) {
                        activitiesGivenPriorityAndNumParticipants = new HashMap<>();
                        activitiesGivenPriority.put(numParticipants, activitiesGivenPriorityAndNumParticipants);
                    }

                    // Look up possible activities.
                    List<PossibleActivity> agentPossibleActivities = activitiesGivenPriorityAndNumParticipants.get(agent);
                    if (agentPossibleActivities == null) {
                        agentPossibleActivities = new ArrayList<>();
                        activitiesGivenPriorityAndNumParticipants.put(agent, agentPossibleActivities);
                    }
                    agentPossibleActivities.add(possibleActivity);
                }
            }

            DebugPrinter.println("Prioritized activities: " + prioritizedActivities);
        }

        // Iterate through priorities: the most important activities first.
        for (int priorityInt : prioritizedActivities.keySet()) {
            SortedMap<Integer, HashMap<Agent, List<PossibleActivity>>> activitiesGivenPriority = prioritizedActivities.get(priorityInt);

            // Iterate through number of participants: the activities with the most participants first.
            for (int numParticipants : activitiesGivenPriority.keySet()) {
                final HashMap<Agent, List<PossibleActivity>> possibleActivitiesWithPriority = activitiesGivenPriority.get(numParticipants);

                // Iterate through agents that will get an activity.
                for (Agent agent : possibleActivitiesWithPriority.keySet()) {
                    final List<PossibleActivity> possibleActivities = possibleActivitiesWithPriority.get(agent);
                    final ActivityLikelihoodMap likelihoods = activityProbabilities.get(agent);

                    DebugPrinter.println("Priority   : " + priorityInt);
                    DebugPrinter.println("#Particip  : " + (Integer.MAX_VALUE - numParticipants));
                    DebugPrinter.println("Agent      : " + agent);
                    DebugPrinter.println("Likelihoods: " + likelihoods);

                    // Iterate through possible activities.
                    for (int i = possibleActivities.size() - 1; !possibleActivities.isEmpty(); i--) {
                        int index = RandomNumber.nextInt(possibleActivities.size());
                        PossibleActivity possibleActivity = possibleActivities.remove(index);

                        DebugPrinter.println("* Planning activity: " + possibleActivity);

                        double threshold = RandomNumber.nextDouble();

                        DebugPrinter.println("  Threshold and likelihood: " + threshold + " < " + likelihoods.get(possibleActivity));

                        if (threshold < likelihoods.get(possibleActivity)) {
                            EnumMap<PossibleActivity.Index, Activity> activities = possibleActivity.createActivities(agent, world);
                            if (activities != null && activities.get(PossibleActivity.Index.ACTIVITY) != null && activities.get(PossibleActivity.Index.ACTIVITY).getParticipants().size() != 0) {

                                DebugPrinter.println("  Created activity: " + activities);
                                addSuccess(possibleActivity);

                                for (Activity a : activities.values()) {
                                    writeInAgenda(a);
                                }

                                DebugPrinter.println("  Agenda is now: \n---\n" + createOverviewAgenda(agent) + "---");

                                // Make sure none of the agents involved do this activity again today.
                                // TODO It is currently not possible to do an activity twice per day. ...
                                // If we remove this restriction, we have a side effect: e.g. if we force a parent to be present for
                                // dinner with a child, the parent may very well decide to have dinner a second time on the same day.
                                Activity plannedActivity = activities.get(PossibleActivity.Index.ACTIVITY);
                                Group participants = plannedActivity.getParticipants();
                                for (Agent participant : participants) { // For anyone involved...
                                    ActivityLikelihoodMap activityLikelihoodMap = activityProbabilities.get(participant);
                                    activityLikelihoodMap.setLikelihoodsToZero(possibleActivity.getClass()); // ... the activity is no longer possible.
                                }
                            } else {
                                addFailure(possibleActivity); // We mean failure as in wanting to do it but not being able to.
                            }
                        }
                    }
                }
            }
        }
    }

    private void optimizeActivities(Group household) {
        for (Agent agent : household) {

            // First, optimize pairs of non-moving as well as pairs of moving activities.
            List<Activity> currentAgenda = agent.getAgenda();
            DebugPrinter.println("\n\nBEFORE TUPLE OPTIMIZING:\n" + createOverviewAgenda(agent) + "\n");
            for (int i = 0; i < currentAgenda.size() - 1; i++) {
                optimizeActivityPair(currentAgenda, i);
            }
            List<Activity> newAgenda = new ArrayList<>();
            for (Activity activity : currentAgenda) {
                if (activity != null) {
                    newAgenda.add(activity);
                }
            }
            agent.getAgenda().clear();
            agent.getAgenda().addAll(newAgenda);

            // Second, optimize triplets with a move in the middle to time the move such that the gap is smallest.
            currentAgenda = agent.getAgenda();
            DebugPrinter.println("\n\nBEFORE TRIPLE OPTIMIZING:\n" + createOverviewAgenda(agent) + "\n");
            for (int i = 1; i < currentAgenda.size() - 1; i++) {
                optimizeActivityTriple(currentAgenda.get(i - 1), currentAgenda.get(i), currentAgenda.get(i + 1));
            }
            DebugPrinter.println("\n\nOPTIMIZED AGENDA:\n" + createOverviewAgenda(agent) + "\n");
        }
    }

    private void optimizeActivityPair(List<Activity> agenda, int firstIndex) {

        Activity activity1 = agenda.get(firstIndex);
        Activity activity2 = agenda.get(firstIndex + 1);
        if (activity1 == null || activity2 == null) {
            return;
        }

        DebugPrinter.println("\nActivities before optimizing: - " + activity1);
        DebugPrinter.println("                              - " + activity2);

        // Only modify activities with one participant; otherwise,
        // we start snowballing across agenda's of different agents.
        boolean firstActivityCanBeModified = activity1.getParticipants().size() == 1;
        boolean secondActivityCanBeModified = activity2.getParticipants().size() == 1;

        // Don't modify anything that has more participants because of snowballing.
        if (!firstActivityCanBeModified && !secondActivityCanBeModified) {
            // Nothing needed here.
        }

        // Two non-moving activities can perhaps be planned closer together.
        else if (!activity1.isMoving() && !activity2.isMoving()) {
            if (activity1.getEndTime().compareTo(activity2.getStartTime()) < 0) {

                DebugPrinter.println("We can try to move two static activities closer together");

                optimizeActivityGap(activity1, activity2, 0, firstActivityCanBeModified, secondActivityCanBeModified);

                DebugPrinter.println("Activities after  optimizing: - " + activity1);
                DebugPrinter.println("                              - " + activity2);
            }
        }

        // Two moving activities can be combined into one.
        else if (activity1.isMoving() && activity2.isMoving()) {
            if (firstActivityCanBeModified && secondActivityCanBeModified) {

                DebugPrinter.println("We can merge two moving activities");

                // Rather cumbersome way of correctly estimating the new end time for the activity.
                double a1pathLengthInM = world.getPathLengthInM(activity1.getStartLocation().getPoint(), activity1.getEndLocation().getPoint());
                long a1durationInNs = activity1.getEndTime().getNanos() - activity1.getStartTime().getNanos();
                double newPathLengthInM = world.getPathLengthInM(activity1.getStartLocation().getPoint(), activity2.getEndLocation().getPoint());
                long newDurationInNs = (long) ((newPathLengthInM / a1pathLengthInM) * a1durationInNs);
                Time newEndTime = activity1.getStartTime().incrementByMinutes(0);
                newEndTime.increment(newDurationInNs);
                // Create a new movement activity, delete the two old ones.
                agenda.set(firstIndex, new BasicMovementActivity(activity1.getPossibleActivity(), activity1.getStartLocation(), activity1.getStartTime(), activity2.getEndLocation(), newEndTime, activity1.getParticipants(), false));
                agenda.set(firstIndex + 1, null);

                DebugPrinter.println("Activity   after  optimizing: - " + activity1);
            }
        }
    }

    private void optimizeActivityTriple(Activity activity1, Activity activity2, Activity activity3) {

        DebugPrinter.println("\nActivities before optimizing: - " + activity1);
        DebugPrinter.println("                              - " + activity2);
        DebugPrinter.println("                              - " + activity3);

        // Only modify activities with one participant; otherwise,
        // we start snowballing across agenda's of different agents.
        boolean firstActivityCanBeModified = activity1.getParticipants().size() == 1;
        boolean secondActivityCanBeModified = activity2.getParticipants().size() == 1;
        boolean thirdActivityCanBeModified = activity3.getParticipants().size() == 1;

        // We now know that it is also the SAME participant.
        Agent agent = activity1.getParticipants().get(0);

        // Don't modify anything that has more participants because of snowballing.
        if (!firstActivityCanBeModified && !secondActivityCanBeModified && !thirdActivityCanBeModified) {
            // Nothing needed here.
        }

        // Only optimize activities when a move is between two stationary activities.
        else if (activity1.isMoving() || !activity2.isMoving() || activity3.isMoving()) {
            // Nothing needed here.
        }

        // There is something to optimize! Try to move the moving activity right in between the stationary ones.
        else if (secondActivityCanBeModified) {

            DebugPrinter.println("We can move a movement activity around.");

            // First, move the stationary activities closer together.
            long activity2DurationNs = activity2.getEndTime().getNanos() - activity2.getStartTime().getNanos();
            optimizeActivityGap(activity1, activity3, activity2DurationNs, firstActivityCanBeModified, thirdActivityCanBeModified);

            // Fit the movement activity in between.
            long newGapSizeNs = activity3.getStartTime().getNanos() - activity1.getEndTime().getNanos();

            // Stay at home as long as possible, if possible. (We don't want people standing on the street doing nothing.)
            boolean moveAfterPrevious = RandomNumber.nextDouble() > 0.5; // Move immediately after activity 1, or immediately before 2?
            if (activity2.startsAtHomeFor(agent)) {
                moveAfterPrevious = false;
                DebugPrinter.println("The agent starts moving at home. Stay at home as long as possible.");
            } else if (activity2.endsAtHomeFor(agent)) {
                moveAfterPrevious = true;
                DebugPrinter.println("The agent ends up moving at home. Go home as soon as possible.");
            } else {
                DebugPrinter.println("The agent moves from a location that is not at home to another such location.");
            }
            DebugPrinter.println("Planning the movement activity directly " + (moveAfterPrevious ? "after the first" : "before the third") + " activity.");
            long delayMovementNs = moveAfterPrevious ? 0 : newGapSizeNs - activity2DurationNs;
            activity2.setStartTime(new Time(activity1.getEndTime().getNanos() + delayMovementNs));
            activity2.setEndTime(new Time(activity1.getEndTime().getNanos() + delayMovementNs + activity2DurationNs));

            DebugPrinter.println("Activities after  optimizing: - " + activity1);
            DebugPrinter.println("                              - " + activity2);
            DebugPrinter.println("                              - " + activity3);
        }

        // There is something to optimize, but the moving activity must remain where it is.
        else {

            DebugPrinter.println("We can adjust gaps around a movement activity");

            optimizeActivityGap(activity1, activity2, 0, firstActivityCanBeModified, secondActivityCanBeModified);
            optimizeActivityGap(activity2, activity3, 0, secondActivityCanBeModified, thirdActivityCanBeModified);

            DebugPrinter.println("Activities after  optimizing: - " + activity1);
            DebugPrinter.println("                              - " + activity2);
            DebugPrinter.println("                              - " + activity3);
        }
    }

    private void optimizeActivityGap(Activity activity1, Activity activity2, long requiredGapSizeNs, boolean firstActivityCanBeModified, boolean secondActivityCanBeModified) {

        // Determine the smallest size of the gap.
        Time a1end = firstActivityCanBeModified ? activity1.getLatestPossibleEndTime() : activity1.getEndTime();
        Time a2start = secondActivityCanBeModified ? activity2.getEarliestPossibleStartTime() : activity2.getStartTime();
        long minGapSizeNs = a2start.getNanos() - a1end.getNanos();

        // Keep a gap bigger than the minimal gap required, if necessary.
        if (minGapSizeNs >= requiredGapSizeNs) {
            if (firstActivityCanBeModified && !a1end.equals(activity1.getEndTime())) {
                activity1.setEndTime(a1end);
            }
            if (secondActivityCanBeModified && !a2start.equals(activity2.getStartTime())) {
                activity2.setStartTime(a2start);
            }
        }

        // Increase the gap if the current gap is smaller than the minimal gap required.
        else if (minGapSizeNs < requiredGapSizeNs) {
            long addToGapNs = requiredGapSizeNs - minGapSizeNs;

            // Determine where to increase the gap.
            long gapSizeToAddToFirstActivityNs = 0;
            long gapSizeToAddToSecondActivityNs = 0;

            // Both activities can be modified.
            if (firstActivityCanBeModified && secondActivityCanBeModified) {
                gapSizeToAddToFirstActivityNs = (long) (RandomNumber.nextDouble() * addToGapNs); // TODO At the moment the gap is filled randomly. This may be suboptimal given maximum durations.
                gapSizeToAddToSecondActivityNs = addToGapNs - gapSizeToAddToFirstActivityNs;
            }

            // Only the first activity can be modified.
            else if (firstActivityCanBeModified) {
                gapSizeToAddToFirstActivityNs = addToGapNs;
            }

            // Only the second activity can be modified.
            else if (secondActivityCanBeModified) { // And not first.
                gapSizeToAddToSecondActivityNs = addToGapNs;
            }

            // Modify the activities if possible and needed.
            if (gapSizeToAddToFirstActivityNs != 0) {
                activity1.setEndTime(new Time(Math.min(a1end.getNanos() - gapSizeToAddToFirstActivityNs, activity1.getLatestPossibleEndTime().getNanos())));
            }
            if (gapSizeToAddToSecondActivityNs != 0) {
                activity2.setStartTime(new Time(Math.max(a2start.getNanos() + gapSizeToAddToSecondActivityNs, activity2.getEarliestPossibleStartTime().getNanos())));
            }
        }
    }

    private String createOverviewAgenda(Agent agent) {
        String s = "";
        final List<Activity> agenda = agent.getAgenda();
        for (Activity activity : agenda) {
            s = s + (TextUtils.addSpacesToString(activity.getName(), 30)) + "| ";
            s = s + (TextUtils.addSpacesToString(activity.getParticipants(), 40)) + "| ";
            s = s + (TextUtils.addSpacesToString(activity.getStartTime(), 10)) + "| ";
            s = s + (TextUtils.addSpacesToString(activity.getStartLocation().getPoint(), 20)) + "| ";
            s = s + (TextUtils.addSpacesToString(activity.getStartLocation().getArea().getFunctions(), 30)) + "| ";
            s = s + (TextUtils.addSpacesToString(activity.getEndTime(), 10)) + "| ";
            s = s + (TextUtils.addSpacesToString(activity.getEndLocation().getPoint(), 20)) + "| ";
            s = s + (TextUtils.addSpacesToString(activity.getEndLocation().getArea().getFunctions(), 30)) + "| ";
            s = s + (TextUtils.addSpacesToString(activity.getParticipants(), 30));
            s = s + "\n";
        }
        return s;
    }

    private void writeInAgenda(Activity activity) {

        // Put it in everyone's agenda.
        for (Agent agent : activity.getParticipants()) {
            List<Activity> agenda = agent.getAgenda();

            // Insert at right place in the agenda if the agenda is empty or it is the latest activity.
            if (agenda.isEmpty() || activity.compareTo(agenda.get(agenda.size() - 1)) > 0) {
                agenda.add(activity);
            }

            // Insert at right place in the agenda if not empty or somewhere in between.
            else {
                for (int i = 0; i < agenda.size(); ++i) {
                    if (activity.compareTo(agenda.get(i)) < 0) {
                        agenda.add(i, activity);
                        break;
                    }
                }
            }
        }
    }
}
