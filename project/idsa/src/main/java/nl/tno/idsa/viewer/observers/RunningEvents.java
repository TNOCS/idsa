package nl.tno.idsa.viewer.observers;

import nl.tno.idsa.framework.behavior.incidents.Incident;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;

/**
 * @author jongsd
 */
// TODO Document class.
public class RunningEvents extends Observable {
    private final List<Incident> incidents;

    public RunningEvents() {
        this.incidents = new ArrayList<Incident>();
    }

    public void addEvent(Incident e) {
        this.incidents.add(e);
        setChanged();
        notifyObservers(this.incidents);
    }

    public void removeEvent(Incident e) {
        this.incidents.remove(e);
        setChanged();
        notifyObservers(this.incidents);
    }
}
