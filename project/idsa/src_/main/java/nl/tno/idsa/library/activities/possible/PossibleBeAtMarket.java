package nl.tno.idsa.library.activities.possible;

import nl.tno.idsa.framework.agents.Agent;
import nl.tno.idsa.framework.behavior.activities.possible.PossibleStationaryActivity;
import nl.tno.idsa.framework.behavior.activities.possible.PossibleTimeIntervals;
import nl.tno.idsa.framework.population.Gender;
import nl.tno.idsa.framework.semantics_impl.groups.Group;
import nl.tno.idsa.library.locations.Square;

public class PossibleBeAtMarket extends PossibleStationaryActivity {

    public PossibleBeAtMarket(PossibleTimeIntervals time) {
        super(time, Fill.RandomPartOfTimeSlot, Square.class);
    }

    @Override
    public double getPriority() {
        return 0.2;
    }

    @Override
    public double getMultiplier(Agent agent) {
        double mult = 1;
        if (agent.getAge() > 64) {
            mult *= 5;
        }
        if (agent.getGender() == Gender.MALE) {
            mult *= 0.7; // Quite sexist, but true ;)
        }
        return mult;
    }

    @Override
    public Group getParticipants(Agent agent) {
        // TODO Elderly people often go with a friend.
        return super.getParticipants(agent);
    }
}
