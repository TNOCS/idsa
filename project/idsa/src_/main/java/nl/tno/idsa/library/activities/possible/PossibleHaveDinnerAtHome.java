package nl.tno.idsa.library.activities.possible;

import nl.tno.idsa.framework.agents.Agent;
import nl.tno.idsa.framework.behavior.activities.possible.PossibleStationaryActivity;
import nl.tno.idsa.framework.behavior.activities.possible.PossibleTimeIntervals;
import nl.tno.idsa.framework.world.Time;

/**
 * Created by kleina on 31-8-2015.
 */
public class PossibleHaveDinnerAtHome extends PossibleStationaryActivity {

    public PossibleHaveDinnerAtHome(PossibleTimeIntervals time) {
        super(time, Fill.RandomPartOfTimeSlot); // Omit location functions -> defaults to the agent's house.
    }

    public PossibleHaveDinnerAtHome() {
        // TODO Why is dinner at home initialized in the activity itself? All others are initialized in the days of the week.
        this(new PossibleTimeIntervals(new Time(17, 0, 0), new Time(19, 30, 0), 30, 180));
    }

    @Override
    public double getPriority() {
        return 0.25;
    }

    @Override
    public double getMultiplier(Agent agent) {
        return 1; // No age or gender influence.
    }

    // TODO People in the same household, but above 18, currently have dinner at different times.
}
