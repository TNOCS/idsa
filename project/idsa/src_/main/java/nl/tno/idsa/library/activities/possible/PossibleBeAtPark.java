package nl.tno.idsa.library.activities.possible;

import nl.tno.idsa.framework.agents.Agent;
import nl.tno.idsa.framework.behavior.activities.possible.PossibleStationaryActivity;
import nl.tno.idsa.framework.behavior.activities.possible.PossibleTimeIntervals;
import nl.tno.idsa.library.locations.Park;

public class PossibleBeAtPark extends PossibleStationaryActivity {

    public PossibleBeAtPark(PossibleTimeIntervals time) {
        super(time, Fill.RandomPartOfTimeSlot, Park.class);
    }

    @Override
    public double getPriority() {
        return 0.15;
    }

    @Override
    public double getMultiplier(Agent agent) {
        return 1; // No age or gender influence.
    }
}
