package nl.tno.idsa;

/**
 * We gathered some settings and constants here. This can be improved.
 */
public class Constants {

    /**
     * Names from which language are used?
     */
    public static final String NAMES_LANGUAGE_ID = "nl";

    /**
     * The maximum walking time in seconds from location to location used by the daily behavior generator.
     */
    public static final int MAX_WALKING_TIME_S = 1200;

    /**
     * The maximum number of agents sampled taken by the agent and location sampler to find suitable agents
     * for participating in a certain incident.
     */
    public static final int MAX_RANDOM_AGENT_SAMPLES = 10000;

    /**
     * The number of restarts the agent and location sampler can do while trying to realize a plan.
     */
    public static final int MAX_SAMPLE_ATTEMPTS = 2;

    /**
     * The number of iterations the agent and location sampler can do while trying to realize a plan.
     */
    public static final int MAX_REFINING_ITERATIONS = 20;

    /**
     * When sampling, how much closer to we want locations to be in order to be an improvement (e.g. at 50%).
     */
    public static final double CLOSER_SAMPLING_FACTOR_LOCATIONS = 0.5;

    /**
     * When sampling, how much closer to we want agents to be in order to be an improvement (e.g. at 80%).
     */
    public static final double CLOSER_SAMPLING_FACTOR_GROUPS = 0.8;

    /**
     * How many iterations does the optimization algorithm for neighborhood population take?
     */
    public static final int NEIGHBORHOOD_OPTIMIZATION_ITERATIONS = 1000000;

    /**
     * For debugging or speeding up the simulator, it might be handy to be able to have only a maximum number
     * of agents (picked randomly) with agendas, instead of all agents. Set this to Integer.MAX_VALUE if you
     * do not want to limit the number of agendas created.
     */
    public final static int MAX_AGENTS_WITH_AGENDAS = 10; //Integer.MAX_VALUE;
}
