package nl.tno.idsa.framework.semantics_impl.groups;

import nl.tno.idsa.framework.agents.Agent;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by jongsd on 4-8-15.
 */

// TODO Document class. It extends a list but is a set.

public class Group extends ArrayList<Agent> {

    private static long ID = 0;
    private long id = ID++;

    public Group() {
    }

    public Group(Collection<Agent> agents) {
        addAll(agents);
    }

    public Group(Agent agent) {
        add(agent);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        Group agents = (Group) o;
        return id == agents.id;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (int) (id ^ (id >>> 32));
        return result;
    }

    @Override
    public boolean add(Agent e) {
        boolean result = !contains(e);
        return result && super.add(e); // The super.add is skipped if result is false.
    }
}
