package nl.tno.idsa.library.activities.possible;

import nl.tno.idsa.framework.agents.Agent;
import nl.tno.idsa.framework.behavior.activities.possible.PossibleStationaryActivity;
import nl.tno.idsa.framework.behavior.activities.possible.PossibleTimeIntervals;
import nl.tno.idsa.framework.semantics_impl.groups.Group;
import nl.tno.idsa.library.locations.SportsField;

/**
 * Created by kleina on 27-10-2015.
 */
public class PossibleBeAtSportsField extends PossibleStationaryActivity {

    public PossibleBeAtSportsField(PossibleTimeIntervals time) {
        super(time, Fill.RandomPartOfTimeSlot, SportsField.class);
    }

    @Override
    public double getPriority() {
        return 0.15;
    }

    @Override
    public double getMultiplier(Agent agent) {
        if (agent.getAge() > 65) {
            double relativeAge = agent.getAge() - 64;
            return 1 / relativeAge;
        }
        if (agent.getAge() < 4) {
            return 0;
        }
        return 1;
    }

    @Override
    public Group getParticipants(Agent agent) {
        // TODO People often go to sports with family or friends.
        return super.getParticipants(agent);
    }
}
