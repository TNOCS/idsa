package nl.tno.idsa.library.activities.possible;

import nl.tno.idsa.framework.agents.Agent;
import nl.tno.idsa.framework.behavior.activities.possible.PossibleStationaryActivity;
import nl.tno.idsa.framework.behavior.activities.possible.PossibleTimeIntervals;
import nl.tno.idsa.framework.semantics_impl.groups.Group;
import nl.tno.idsa.library.locations.Square;

public class PossibleHangAroundOnSquare extends PossibleStationaryActivity {

    public PossibleHangAroundOnSquare(PossibleTimeIntervals time) {
        super(time, Fill.RandomPartOfTimeSlot, Square.class);
    }

    @Override
    public double getPriority() {
        return 0.1;
    }

    @Override
    public double getMultiplier(Agent agent) {
        if (agent.getAge() < 14 || agent.getAge() > 22) {
            return 0;
        }
        return 1;
    }

    @Override
    public Group getParticipants(Agent agent) {
        // TODO People generally do not do this alone, but are not accompanied by an adult.
        return new Group(agent);
    }
}
