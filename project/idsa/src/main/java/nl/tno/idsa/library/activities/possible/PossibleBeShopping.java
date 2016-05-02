package nl.tno.idsa.library.activities.possible;

import nl.tno.idsa.framework.agents.Agent;
import nl.tno.idsa.framework.behavior.activities.possible.PossibleStationaryActivity;
import nl.tno.idsa.framework.behavior.activities.possible.PossibleTimeIntervals;
import nl.tno.idsa.framework.population.Gender;
import nl.tno.idsa.library.locations.ShoppingArea;

/**
 * Created by kleina on 20-10-2015.
 */
public class PossibleBeShopping extends PossibleStationaryActivity {

    public PossibleBeShopping(PossibleTimeIntervals time) {
        super(time, Fill.RandomPartOfTimeSlot, ShoppingArea.class);
    }

    @Override
    public double getPriority() {
        return 0.3;  // Quite important.
    }

    @Override
    public double getMultiplier(Agent agent) {
        double mult = 1;
        if (agent.getGender() == Gender.MALE) {
            mult *= 0.7;
        } else {
            mult *= 2; // Sexist but true.
        }
        if (agent.getAge() < 10) {
            mult *= 0;
        }
        if (agent.getAge() < 16) {
            mult *= 0.1;
        }
        return mult;
    }
}
