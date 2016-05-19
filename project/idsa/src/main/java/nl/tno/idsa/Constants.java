package nl.tno.idsa;

/**
 * We gathered some settings and constants here. This can be improved.
 */
public class Constants {

    /**
     * The maximum number of agents sampled taken by the agent and location sampler to find suitable agents
     * for participating in a certain incident.
     */
    public static final int INCIDENT_MAX_RANDOM_AGENT_SAMPLES = 10000;

    /**
     * The number of restarts the agent and location sampler can do while trying to realize a plan.
     */
    public static final int INCIDENT_MAX_SAMPLE_ATTEMPTS = 2;

    /**
     * The number of iterations the agent and location sampler can do while trying to realize a plan.
     */
    public static final int INCIDENT_MAX_REFINING_ITERATIONS = 20;

    /**
     * When sampling, how much closer to we want locations to be in order to be an improvement (e.g. at 50%).
     */
    public static final double INCIDENT_CLOSER_SAMPLING_FACTOR_LOCATIONS = 0.5;

    /**
     * When sampling, how much closer to we want agents to be in order to be an improvement (e.g. at 80%).
     */
    public static final double INCIDENT_CLOSER_SAMPLING_FACTOR_GROUPS = 0.8;

    /**
     * Names from which language are used?
     */
    public static final String POPULATION_NAMES_LANGUAGE_ID = "nl";

    /**
     * How many iterations does the optimization algorithm for neighborhood population take?
     */
    public static final int POPULATION_NEIGHBORHOOD_OPTIMIZATION_ITERATIONS = 1000000;

    /**
     * The maximum walking time in seconds from location to location used by the daily behavior generator.
     */
    public static final int AGENDA_MAX_WALKING_TIME_S = 1200;

    /**
     * For debugging or speeding up the simulator, it might be handy to be able to completely disable making
     * agendas for the population.
     */
    public static final boolean AGENDA_ENABLED = true;

    /**
     * For debugging or speeding up the simulator, it might be handy to be able to have only a maximum number
     * of agents (picked randomly) with agendas, instead of all agents. Set this to Integer.MAX_VALUE if you
     * do not want to limit the number of agendas created.
     */
    public final static int AGENDA_MAX_AGENTS_WITH_AGENDAS = Integer.MAX_VALUE;

    /** Whether the map view visually declutters agents. */
    public static final boolean GUI_DECLUTTER_AGENTS = false;

    /** Size in pixels at 100% zoom level of agents in the map view.*/
    public static final double GUI_AGENT_ICON_SIZE = 4.0;
}
