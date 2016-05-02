package nl.tno.idsa.viewer;

import nl.tno.idsa.framework.agents.Agent;
import nl.tno.idsa.framework.behavior.incidents.Incident;
import nl.tno.idsa.framework.world.Area;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;

/**
 * Observer for selecting objects in the GUI.
 */
public class SelectionObserver extends Observable {

    private final List<Agent> agents;
    private Incident incident;
    private Area area;

    public SelectionObserver() {
        this.agents = new ArrayList<>();
        this.incident = null;
    }

    public void setIncident(Incident e) {
        boolean incidentChanged = e == null ? this.incident != null : !e.equals(this.incident);
        if (incidentChanged) {
            setChanged();
            this.incident = e;
            notifyObservers(this.incident);
        }
        boolean agentsChanged = false;
        if (agents.size() > 0) {
            this.agents.clear(); // When you select an incident, this means you deselect all agents.
            agentsChanged = true;
        }
        if (agentsChanged) {
            setChanged();
            notifyObservers(this.agents);
        }
    }

    public void setAgent(Agent a) {
        if (a == null && this.agents.size() == 0) {
            return; // no change
        }
        if (agents.size() == 1 && agents.get(0).equals(a)) {
            return; // no change
        }
        this.agents.clear();
        if (a != null) {
            this.agents.add(a);
        }

        setChanged();
        notifyObservers(this.agents);
    }

    public void setAgents(List<Agent> agents) {
        boolean agentsChanged = false;
        for (Agent agent : agents) {
            if (!this.agents.contains(agent)) {
                agentsChanged = true;
                break;
            }
        }
        if (!agentsChanged) {
            for (Agent agent : this.agents) {
                if (!agents.contains(agent)) {
                    agentsChanged = true;
                    break;
                }
            }
        }
        if (agentsChanged) {
            this.agents.clear();
            this.agents.addAll(agents);
            setChanged();
            notifyObservers(this.agents);
        }
    }

    public void setArea(Area area) {
        boolean areaChanged = area == null ? this.area != null : !area.equals(this.area);
        if (areaChanged) {
            this.area = area;
            setChanged();
            notifyObservers(this.area);
        }
    }

    public List<Agent> getAgents() {
        return agents;
    }

    public Incident getIncident() {
        return incident;
    }

    public boolean containsAgent(Agent a) {
        return this.agents.contains(a);
    }
}
