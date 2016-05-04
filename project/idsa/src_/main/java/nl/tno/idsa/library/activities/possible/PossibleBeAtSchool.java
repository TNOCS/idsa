package nl.tno.idsa.library.activities.possible;

import nl.tno.idsa.framework.agents.Agent;
import nl.tno.idsa.framework.behavior.activities.possible.PossibleStationaryActivity;
import nl.tno.idsa.framework.behavior.activities.possible.PossibleTimeIntervals;
import nl.tno.idsa.framework.population.HouseholdRoles;
import nl.tno.idsa.framework.semantics_impl.groups.Group;
import nl.tno.idsa.library.locations.School;

/**
 * Created by kleina on 31-8-2015.
 */
public class PossibleBeAtSchool extends PossibleStationaryActivity {

    public PossibleBeAtSchool(PossibleTimeIntervals time) {
        super(time, Fill.EntireTimeSlot, School.class);
    }

    @Override
    public double getPriority() {
        return 0.9;  // The most important activity, plan-wise.
    }

    @Override
    public double getMultiplier(Agent agent) {
        if (agent.getHouseholdRole() == HouseholdRoles.MOTHER || agent.getHouseholdRole() == HouseholdRoles.FATHER) {
            return 0;
        }
        if (agent.getAge() < 3) {
            return 0;
        }
        if (agent.getAge() < 16) {
            return 1;
        }
        if (agent.getAge() < 23) {
            return 0.3;
        }
        if (agent.getAge() < 30) {
            return 0.1;
        }
        return 0;
    }

    @Override
    protected Group getAgentsAccompanying(Agent agent) {
        return null;
        // Default accompaniment includes a parent, but children don't take parents to school!
        // TODO: Important note. In the agenda planner, we assume that all bringers, pickers-up and accompanying agents need to be free during the entire activity. ...
        // For all activities EXCEPT school, this is true for now. For the school activity to work, where parents can leave, we need to plan it FIRST!!!
        // Then, it does not matter that parents are falsely assumed to stay during the day, because their agenda is at that moment still empty and theoretically
        // they could stay during the day. The actual activity that is planned will not include them staying at school, so this works out.
    }

    @Override
    public Group getParticipants(Agent agent) {
        Group participants = new Group(agent);
        // TODO For now, we take all eligible children to the same school if they need accompaniment.
        if (agent.getAge() <= getMaxAgeRequiringAccompaniment()) {
            for (Agent householdMember : agent.getHousehold()) {
                if (getMultiplier(householdMember) > 0 && householdMember.getAge() <= getMaxAgeRequiringAccompaniment()) {
                    participants.add(householdMember);
                }
            }
        }
        return participants;
    }
}
