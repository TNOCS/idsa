package nl.tno.idsa.tools;

import nl.tno.idsa.framework.behavior.incidents.Incident;
import nl.tno.idsa.framework.behavior.planners.IncidentActionPlanner;
import nl.tno.idsa.framework.behavior.planners.IncidentAgentAndLocationSampler;
import nl.tno.idsa.framework.behavior.plans.ActionPlan;
import nl.tno.idsa.framework.messaging.Messenger;
import nl.tno.idsa.framework.population.PopulationGenerator;
import nl.tno.idsa.framework.semantics_base.objects.ParameterId;
import nl.tno.idsa.framework.semantics_impl.locations.LocationAndTime;
import nl.tno.idsa.framework.semantics_impl.roles.Role;
import nl.tno.idsa.framework.semantics_impl.variables.IntegerVariable;
import nl.tno.idsa.framework.semantics_impl.variables.LocationVariable;
import nl.tno.idsa.framework.semantics_impl.variables.RoleVariable;
import nl.tno.idsa.framework.semantics_impl.variables.Variable;
import nl.tno.idsa.framework.simulator.Sim;
import nl.tno.idsa.framework.utils.DataSourceFinder;
import nl.tno.idsa.framework.utils.RandomNumber;
import nl.tno.idsa.framework.world.*;
import nl.tno.idsa.library.incidents.IncidentArrestAfterOffense;
import nl.tno.idsa.library.incidents.IncidentComeToAid;
import nl.tno.idsa.library.incidents.IncidentProcession;
import nl.tno.idsa.library.locations.PoliceSpawnPoint;
import nl.tno.idsa.library.population.PopulationDataNL;
import nl.tno.idsa.library.roles.Shoplifter;
import nl.tno.idsa.library.roles.StreetThief;
import nl.tno.idsa.viewer.MainFrame;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Perform an experiment to assess the quality of the sampler.
 */
public class TestSamplerQuality {

    // VARIOUS SETTINGS ************************************************************************************************

    private static final boolean showErrors = true;
    private static final int numberOfEvents = 500;
    private static final int maxEvents = 3; // TODO This parameter related to a lot of hardcoding below.

    // ADMINISTRATION **************************************************************************************************

    private static int Successes = 0;
    private static int Fails = 0;
    private static final HashMap<Incident, Long> eventToAllowedDuration = new HashMap<>();

    private static final HashMap<Incident, String> incidentName = new HashMap<>();
    private static final HashMap<Incident, Double> eventToSamplingTime = new HashMap<>();
    private static final HashMap<Incident, Boolean> eventIsSolved = new HashMap<>();

    private static final HashMap<Incident, Long> failTimeNeededProcession = new HashMap<>();
    private static final HashMap<Incident, Long> failTimeNeededArrest = new HashMap<>();
    private static final HashMap<Incident, Long> failTimeNeededComeToAid = new HashMap<>();

    private static ArrayList<Double> timesTotal = new ArrayList<>();
    private static ArrayList<Double> timesTotalSuccess = new ArrayList<>();
    private static ArrayList<Double> timesTotalFail = new ArrayList<>();

    private static ArrayList<Double> timesProcession = new ArrayList<>();
    private static ArrayList<Double> timesProcessionSuccess = new ArrayList<>();
    private static ArrayList<Double> timesProcessionFail = new ArrayList<>();

    private static ArrayList<Double> timesArrest = new ArrayList<>();
    private static ArrayList<Double> timesArrestSuccess = new ArrayList<>();
    private static ArrayList<Double> timesArrestFail = new ArrayList<>();

    private static ArrayList<Double> timesComeToAid = new ArrayList<>();
    private static ArrayList<Double> timesComeToAidSuccess = new ArrayList<>();
    private static ArrayList<Double> timesComeToAidFail = new ArrayList<>();

    public static void main(String[] args) throws Exception {

        // Create world.
        List<DataSourceFinder.DataSource> dataSources = DataSourceFinder.listDataSources();
        if (dataSources.size() == 0) {
            System.out.println("No data files were found, exiting.");
            return;
        }
        DataSourceFinder.DataSource dataSource = dataSources.get(0); // TODO We should let the user choose. Create a dialog box to show if there are multiple options.
        String path = dataSource.getPath();

        World world = WorldGenerator.generateWorld(dataSource.getWorldModel(),

                path + "/idsa_nav_network_pedestrian.shp",
                path + "/idsa_pand_osm_a_utm31n.shp",
                path + "/idsa_public_areas_a_utm31n.shp",
                path + "/idsa_vbo_utm31n.shp",
                path + "/idsa_pand_p_utm31n.shp");

        Environment env = new Environment(world, new Day(21, 9, 2015), new Time(10, 0, 0)); //there are people on the streets at this time.
        env.setPopulation(new PopulationGenerator(env, new PopulationDataNL()).generatePopulation("../../data/nl/idsa_cbs_buurten_utm31n.shp"));
        env.initializePopulation(env.getSeason(), null, env.getDay(), env.getTime(), true);
        Sim sim = Sim.getInstance();
        sim.init(env);
        Messenger.enableMirrorToConsole(true);

        // Add some police spawn points.
        List<Vertex> vertices = world.getVertices();
        int changedVertices = 0;
        while (changedVertices < 50) {
            Vertex randomVertex = vertices.get(RandomNumber.nextInt(vertices.size()));
            if (randomVertex.getArea() != null) {
                randomVertex.getArea().addFunction(new PoliceSpawnPoint());
                changedVertices++;
            }
        }

        //Loop in making incidents.
        // TODO This uses a lot of hardcoding instead of enumerating and instantiating everything in the library.
        ArrayList<Incident> incidents = new ArrayList<>();
        for (int i = 0; i < numberOfEvents; i++) {
            int whatIncident = RandomNumber.nextInt(maxEvents);
            Incident incident;
            Map<ParameterId, Variable> parameters;

            if (whatIncident == 0) { //Procession
                incident = new IncidentProcession(env.getWorld());
                parameters = incident.getParameters();
                parameters.put(IncidentProcession.Parameters.NUMBER_OF_PARTICIPANTS, new IntegerVariable(RandomNumber.nextInt(10) + 11)); //between 10 and 20 participants.
                incidentName.put(incident, "Procession");
            } else if (whatIncident == 1) { //Arrest
                incident = new IncidentArrestAfterOffense(env.getWorld());
                parameters = incident.getParameters();
                int chooseRole = RandomNumber.nextInt(2); //2 roles for offender
                Class<? extends Role> role;
                if (chooseRole == 0) {
                    role = StreetThief.class;
                } else if (chooseRole == 1) {
                    role = Shoplifter.class;
                } else {
                    System.out.println("ERROR: role in ArrestAfterOffense not chosen correctly " + chooseRole);
                    role = null;
                }
                parameters.put(IncidentArrestAfterOffense.Parameters.DESIRED_OFFENDER_ROLE, new RoleVariable(role));
                parameters.put(IncidentArrestAfterOffense.Parameters.NUMBER_OF_OFFENDERS, new IntegerVariable(2 + RandomNumber.nextInt(2))); //between 2 and 3
                parameters.put(IncidentArrestAfterOffense.Parameters.NUMBER_OF_ARRESTING_OFFICERS, new IntegerVariable(2 + RandomNumber.nextInt(2))); //between 2 and 3
                incidentName.put(incident, "Arrest");
            } else if (whatIncident == 2) { //ComeToAid
                incident = new IncidentComeToAid(env.getWorld());
                parameters = incident.getParameters();

                parameters.put(IncidentComeToAid.Parameters.NUMBER_OF_VICTIMS, new IntegerVariable(2));
                parameters.put(IncidentComeToAid.Parameters.NUMBER_OF_FIRST_RESPONDERS, new IntegerVariable(1));
                incidentName.put(incident, "Come to aid");
            } else {
                System.out.println("ERROR: cannot choose incident " + whatIncident);
                incident = null;
                parameters = null;
            }

            if (parameters != null) {
                long desiredTime = env.getTime().getCopyWithDifference(0, RandomNumber.nextInt(4) + 3, RandomNumber.nextInt(60)).getNanos(); //between 3 and 7 minutes.
                eventToAllowedDuration.put(incident, desiredTime - env.getTime().getNanos());
                parameters.put(Incident.Parameters.LOCATION_VARIABLE, new LocationVariable(new LocationAndTime(new Point(RandomNumber.nextInt(701) + 800, RandomNumber.nextInt(1401) + 1700), desiredTime))); // [xmin, xmax, ymin, ymax] = [800, 1500, 1700, 3100]
                if (incident.bindParameters()) {
                    incidents.add(incident);
                } else {
                    System.out.println("ERROR: cannot bind parameters for incident " + whatIncident);
                }
            }
        }

        System.out.println("\nList of incidents is created.");
        for (Incident incident : incidents) {
            System.out.println(incident);
        }

        System.out.println("\nStart solving incidents:");

        long time1;
        long time2;
        int countFailwindows = 0;

        for (Incident incident : incidents) {
            //Create a plan.
            ActionPlan plan = IncidentActionPlanner.getInstance().createPlan(env, incident);

            //Find suitable values for variables in the world.
            time1 = System.nanoTime();
            boolean planFound = IncidentAgentAndLocationSampler.instantiatePlan(env, plan);
            time2 = System.nanoTime();
            eventToSamplingTime.put(incident, (((double) time2 - time1)) / ((double) Time.NANO_SECOND));
            if (planFound) {
                System.out.println("SUCCESS: sampling duration is " + eventToSamplingTime.get(incident) + " seconds.");
                Successes++;
                eventIsSolved.put(incident, true);
            } else {
                System.out.println("FAIL: sampling duration is " + eventToSamplingTime.get(incident) + " seconds.");
                Fails++;
                eventIsSolved.put(incident, false);

                System.out.println("Current time to realize start of enabling action: " + Time.durationToString(plan.estimateDuration(env, false)));
                System.out.println("Current time to realize end of enabling action: " + Time.durationToString(plan.estimateDuration(env, true)));
                System.out.println("Allowed time to realize: " + Time.durationToString(eventToAllowedDuration.get(incident)));

                if (incidentName.get(incident).equals("Procession")) {
                    failTimeNeededProcession.put(incident, plan.estimateDuration(env, true));
                } else if (incidentName.get(incident).equals("Arrest")) {
                    failTimeNeededArrest.put(incident, plan.estimateDuration(env, true));
                } else if (incidentName.get(incident).equals("Come to aid")) {
                    failTimeNeededComeToAid.put(incident, plan.estimateDuration(env, true));
                }

                if (showErrors && countFailwindows < 2) {
                    MainFrame mf = new MainFrame(sim);
                    mf.show();
                    mf.visualizePlan(plan);
                    countFailwindows++;
                }

                //System.out.println("DETAILS ON FAIL:");
                //System.out.println(event);
                //plan.getRestrictingVariable(env, true, true);
            }
        }

        System.out.println("\nStopped solving incidents.\n");

        System.out.println("Number of successes: " + Successes);
        System.out.println("Number of fails: " + Fails);

        for (Incident incident : incidents) {
            double time = eventToSamplingTime.get(incident);
            timesTotal.add(time);
            if (incidentName.get(incident).equals("Procession")) {
                timesProcession.add(time);
                if (eventIsSolved.get(incident)) {
                    timesProcessionSuccess.add(time);
                    timesTotalSuccess.add(time);
                } else {
                    timesProcessionFail.add(time);
                    timesTotalFail.add(time);
                }
            } else if (incidentName.get(incident).equals("Arrest")) {
                timesArrest.add(time);
                if (eventIsSolved.get(incident)) {
                    timesArrestSuccess.add(time);
                    timesTotalSuccess.add(time);
                } else {
                    timesArrestFail.add(time);
                    timesTotalFail.add(time);
                }
            } else if (incidentName.get(incident).equals("Come to aid")) {
                timesComeToAid.add(time);
                if (eventIsSolved.get(incident)) {
                    timesComeToAidSuccess.add(time);
                    timesTotalSuccess.add(time);
                } else {
                    timesComeToAidFail.add(time);
                    timesTotalFail.add(time);
                }
            } else {
                System.out.println("ERROR 31");
            }
        }

        ArrayList<Double> failTimeP = new ArrayList<>();
        for (Incident incident : failTimeNeededProcession.keySet()) {
            failTimeP.add((double) failTimeNeededProcession.get(incident));
        }
        ArrayList<Double> failTimeA = new ArrayList<>();
        for (Incident incident : failTimeNeededArrest.keySet()) {
            failTimeA.add((double) failTimeNeededArrest.get(incident));
        }
        ArrayList<Double> failTimeC = new ArrayList<>();
        for (Incident incident : failTimeNeededComeToAid.keySet()) {
            failTimeC.add((double) failTimeNeededComeToAid.get(incident));
        }

        System.out.println("GENERAL");
        System.out.println("Avg Total: " + avg(timesTotal));
        System.out.println("Std Total: " + std(timesTotal));
        System.out.println("Avg TotalSuccess: " + avg(timesTotalSuccess));
        System.out.println("Std TotalSuccess: " + std(timesTotalSuccess));
        System.out.println("Avg TotalFail: " + avg(timesTotalFail));
        System.out.println("Std TotalFail: " + std(timesTotalFail));
        System.out.println("-------");

        System.out.println("PROCESSION");
        System.out.println("Number of Procession incidents tested: " + timesProcession.size());
        System.out.println("Avg Procession: " + avg(timesProcession));
        System.out.println("Std Procession: " + std(timesProcession));
        System.out.println("Avg ProcessionSuccess: " + avg(timesProcessionSuccess));
        System.out.println("Std ProcessionSuccess: " + std(timesProcessionSuccess));
        System.out.println("Avg ProcessionFail: " + avg(timesProcessionFail));
        System.out.println("Std ProcessionFail: " + std(timesProcessionFail));
        System.out.println("Avg shortest found time for fails: " + Time.durationToString((long) avg(failTimeP)));
        System.out.println("Std shortest found time for fails: " + Time.durationToString((long) std(failTimeP)));
        System.out.println("Number of Procession success: " + timesProcessionSuccess.size());
        System.out.println("Number of Procession fail: " + timesProcessionFail.size());
        System.out.println("-------");

        System.out.println("ARREST");
        System.out.println("Number of Arrest incidents tested: " + timesArrest.size());
        System.out.println("Avg Arrest: " + avg(timesArrest));
        System.out.println("Std Arrest: " + std(timesArrest));
        System.out.println("Avg ArrestSuccess: " + avg(timesArrestSuccess));
        System.out.println("Std ArrestSuccess: " + std(timesArrestSuccess));
        System.out.println("Avg ArrestFail: " + avg(timesArrestFail));
        System.out.println("Std ArrestFail: " + std(timesArrestFail));
        System.out.println("Avg shortest found time for fails: " + Time.durationToString((long) avg(failTimeA)));
        System.out.println("Std shortest found time for fails: " + Time.durationToString((long) std(failTimeA)));
        System.out.println("Number of Arrest success: " + timesArrestSuccess.size());
        System.out.println("Number of Arrest fail: " + timesArrestFail.size());
        System.out.println("-------");

        System.out.println("COME TO AID");
        System.out.println("Number of ComeToAid incidents tested: " + timesComeToAid.size());
        System.out.println("Avg ComeToAid: " + avg(timesComeToAid));
        System.out.println("Std ComeToAid: " + std(timesComeToAid));
        System.out.println("Avg ComeToAidSuccess: " + avg(timesComeToAidSuccess));
        System.out.println("Std ComeToAidSuccess: " + std(timesComeToAidSuccess));
        System.out.println("Avg ComeToAidFail: " + avg(timesComeToAidFail));
        System.out.println("Std ComeToAidFail: " + std(timesComeToAidFail));
        System.out.println("Avg shortest found time for fails: " + Time.durationToString((long) avg(failTimeC)));
        System.out.println("Std shortest found time for fails: " + Time.durationToString((long) std(failTimeC)));
        System.out.println("Number of ComeToAid success: " + timesComeToAidSuccess.size());
        System.out.println("Number of ComeToAid fail: " + timesComeToAidFail.size());
        System.out.println("-------");
    }

    private static double avg(ArrayList<Double> times) {
        double average = 0;
        for (Double time : times) {
            average += time;
        }
        average = average / ((double) times.size());
        return average;
    }

    private static double std(ArrayList<Double> times) {
        //calculating avg
        double avg = avg(times);

        //calculating std
        double std = 0;
        for (Double time : times) {
            std += (time - avg) * (time - avg);
        }
        std = Math.sqrt(std / ((double) times.size()));
        return std;
    }
}