package nl.tno.idsa.library.activities.possible;

import nl.tno.idsa.framework.agents.Agent;
import nl.tno.idsa.framework.behavior.activities.possible.PossibleStationaryActivity;
import nl.tno.idsa.framework.behavior.activities.possible.PossibleTimeIntervals;
import nl.tno.idsa.library.locations.Workplace;

/**
 * Created by kleina on 31-8-2015.
 */
public class PossibleBeAtWork extends PossibleStationaryActivity {

    public PossibleBeAtWork(PossibleTimeIntervals time) {
        super(time, Fill.RandomPartOfTimeSlot, Workplace.class);
    }

    @Override
    public double getPriority() {
        return 0.75;  // Very important.
    }

    @Override
    public double getMultiplier(Agent agent) {
        if (agent.getAge() < 12) {
            return 0;
        }
        if (agent.getAge() < 18) {
            return 0.2;
        }
        if (agent.getAge() > 65) {
            double relativeAge = agent.getAge() - 64;
            return 0.2 / relativeAge;
        }
        return 1;
    }
}
