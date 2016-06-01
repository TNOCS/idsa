package nl.tno.idsa.viewer.observers;

import nl.tno.idsa.framework.agents.Agent;
import nl.tno.idsa.framework.behavior.incidents.PlannedIncident;
import nl.tno.idsa.framework.world.Area;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;

/**
 * Observer for selections in the GUI.
 */
public class SelectionObserver extends Observable {

    private final List<Agent> agents;
    private PlannedIncident incident;
    private Area area;

    public SelectionObserver() {
        this.agents = new ArrayList<Agent>();
        this.incident = null;
    }

    public void setIncident(PlannedIncident e) {
        boolean eventChanged = e == null ? this.incident != null : !e.equals(this.incident);
        if (eventChanged) {
            setChanged();
            this.incident = e;
            notifyObservers(this.incident);
        }
        boolean agentsChanged = false;
        if (agents.size() > 0) {
            this.agents.clear(); // When you select an event, this means you deselect all agents
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
//            // TODO REMOVE
//            System.out.println("AGENT @ " + a.getLocation() + " CURRENT MODEL: ");
//            Model currentModel = a.getCurrentModel();
//            if (currentModel != null) {
//                Group actors = currentModel.getActors();
//                Group targets = currentModel.getTargets();
//                LocationAndTime locationAndEndTime = currentModel.getLocationAndEndTime();
//                System.out.println(currentModel + " Actors:" + actors + " Targets:" + targets + " Location/time:" + locationAndEndTime);
//            }
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

    public PlannedIncident getIncident() {
        return incident;
    }

    public boolean containsAgent(Agent a) {
        return this.agents == null ? false : this.agents.contains(a);
    }
}
