package nl.tno.idsa.library.activities.possible;

import nl.tno.idsa.framework.agents.Agent;
import nl.tno.idsa.framework.behavior.activities.possible.PossibleStationaryActivity;
import nl.tno.idsa.framework.behavior.activities.possible.PossibleTimeIntervals;
import nl.tno.idsa.library.locations.Playground;

public class PossibleBeAtPlayground extends PossibleStationaryActivity {

    public PossibleBeAtPlayground(PossibleTimeIntervals time) {
        super(time, Fill.RandomPartOfTimeSlot, Playground.class);
    }

    @Override
    public double getPriority() {
        return 0.2;
    }

    @Override
    public double getMultiplier(Agent agent) {
        if (agent.getAge() > 12) {
            return 0.1;
        }
        if (agent.getAge() > 16) {
            return 0;
        }
        return 1;
    }

    @Override
    protected int getMaxAgeRequiringAccompaniment() {
        return 11;     // Defaults are 14 and 18, but playgrounds are for children.
    }

    @Override
    protected int getMinAgeOfAccompaniment() {
        return 12;     // Defaults are 14 and 18, but playgrounds are for children.
    }
}
