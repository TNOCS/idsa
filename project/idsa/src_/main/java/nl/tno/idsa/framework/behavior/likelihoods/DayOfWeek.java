package nl.tno.idsa.framework.behavior.likelihoods;

import nl.tno.idsa.framework.semantics_base.enumerations.IRuntimeEnumElement;
import nl.tno.idsa.framework.world.Day;

/**
 * Days of the week form the basis for our activity likelihood definitions.
 */
public abstract class DayOfWeek extends ActivityLikelihoodMap implements IRuntimeEnumElement, Cloneable {

    public DayOfWeek() {
        super();
        makeActivities();
    }

    /**
     * Get an example day (e.g. March 2nd 2016 for a Wednesday). This prevents incompatibilities
     * with Calendar implementations, as we can be sure a Wednesday is a Wednesday, instead of relying on e.g. a
     * numeric system where Monday is a 0, or a 2, or whatever.
     */
    public abstract Day getPrototypeDay();

    /**
     * Construct the lists of activities and their likelihoods.
     */
    protected abstract void makeActivities();
}
