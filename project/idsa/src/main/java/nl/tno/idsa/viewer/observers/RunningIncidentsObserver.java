package nl.tno.idsa.viewer.observers;

import nl.tno.idsa.framework.behavior.incidents.PlannedIncident;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;

/**
 * @author jongsd
 */
// TODO Document class.
public class RunningIncidentsObserver extends Observable {
    private final List<PlannedIncident> incidents;

    public RunningIncidentsObserver() {
        this.incidents = new ArrayList<PlannedIncident>();
    }

    public void addIncident(PlannedIncident e) {
        this.incidents.add(e);
        setChanged();
        notifyObservers(this.incidents);
    }

    public void removeIncident(PlannedIncident e) {
        this.incidents.remove(e);
        setChanged();
        notifyObservers(this.incidents);
    }
}
