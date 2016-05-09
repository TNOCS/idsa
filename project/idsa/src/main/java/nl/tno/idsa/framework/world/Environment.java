package nl.tno.idsa.framework.world;

import nl.tno.idsa.framework.agents.Agent;
import nl.tno.idsa.framework.behavior.likelihoods.DayOfWeek;
import nl.tno.idsa.framework.behavior.multipliers.ISeason;
import nl.tno.idsa.framework.behavior.multipliers.ITimeOfYear;
import nl.tno.idsa.framework.behavior.planners.AgendaPlanner;
import nl.tno.idsa.framework.messaging.ProgressNotifier;
import nl.tno.idsa.framework.semantics_base.enumerations.RuntimeEnum;
import nl.tno.idsa.framework.semantics_impl.groups.Group;
import nl.tno.idsa.framework.simulator.ISimulatedObject;

import java.util.*;

// TODO Document class.
public class Environment {

    private World world;
    private Time time;
    private final ITimeOfYear timeOfTheYear;
    private Day day;
    private ISeason season;
    private List<ISimulatedObject> simulatedObjects;
    private List<Agent> agents; // keep in sync
    private List<IEnvironmentObserver> observers;

    public Environment(World world, Day day, Time time) {
        this(world, null, null, day, time);
    }

    public Environment(World world, ISeason season, ITimeOfYear timeOfTheYear, Day day, Time time) {
        this.world = world;
        this.season = season;
        this.timeOfTheYear = timeOfTheYear;
        this.day = day;
        this.time = time;
        this.observers = new ArrayList<>();
        this.simulatedObjects = new ArrayList<>(30000);
    }

    public void initializePopulation(ISeason season, ITimeOfYear timeOfTheYear, Day day, Time time, boolean makeAgendas) {

        ProgressNotifier.notifyShowProgress(true);
        ProgressNotifier.notifyProgress(0);
        ProgressNotifier.notifyProgressMessage("Populating environment...");

        this.season = season;
        this.day = day;
        this.time = time;

        for (Agent agent : getAgents()) {
            agent.clearModelStack();
        }

        if (makeAgendas) {
            DayOfWeek option = RuntimeEnum.getInstance(DayOfWeek.class).getOption(day.getDayOfWeek());
            if (option != null) {
                AgendaPlanner ap = new AgendaPlanner(world, option, season, timeOfTheYear);
                ap.createAgendas(getHouseholds());
                ProgressNotifier.notifyProgressMessage("Determining locations for population...");
                for (Agent agent : getAgents()) {
                    agent.setPositionAccordingToActivity(getTime(), getWorld());
                }
            }
        }

        ProgressNotifier.notifyShowProgress(false);
    }

    public void addObserver(IEnvironmentObserver o) {
        this.observers.add(o);
    }

    public World getWorld() {
        return world;
    }

    public Time getTime() {
        return time;
    }

    public ISeason getSeason() {
        return season;
    }

    public ITimeOfYear getTimeOfTheYear() {
        return timeOfTheYear;
    }

    public int getYear() {
        return day.getYear();
    }

    public void setTime(Time time) {
        ProgressNotifier.notifyShowProgress(true);
        ProgressNotifier.notifyProgressMessage("Determining locations for population...");
        this.time = time;
        double count = 0;
        for (Agent agent : agents) {
            agent.clearModelStack();
            agent.setPositionAccordingToActivity(getTime(), getWorld());
            ProgressNotifier.notifyProgress(count++ / agents.size());
        }
        ProgressNotifier.notifyShowProgress(false);
    }

    public Day getDay() {
        return day;
    }

    public String getDateString() {
        return day.toString();
    }

    public void notifyTimeUpdated() { // This can happen by e.g. getTime().increment(...).
        for (IEnvironmentObserver o : this.observers) {
            o.timeChanged(time);
        }
    }

    public List<ISimulatedObject> getSimulatedObjects() {
        return Collections.unmodifiableList(simulatedObjects);
    }

    public List<Agent> getAgents() {
        return Collections.unmodifiableList(agents);
    }

    public Set<Group> getHouseholds() {
        HashSet<Group> households = new HashSet<>();
        for (Agent agent : getAgents()) {
            households.add(agent.getHousehold());
        }
        return Collections.unmodifiableSet(households);
    }

    public void addSimulatedObject(ISimulatedObject newSimulatedObject) {
        simulatedObjects.add(newSimulatedObject);
        newSimulatedObject.setEnvironment(this);
        if (newSimulatedObject instanceof Agent) {
            this.agents.add((Agent) newSimulatedObject);
        }
        simulatedObjectAdded(newSimulatedObject);
    }

    private void simulatedObjectAdded(ISimulatedObject newSimulatedObject) {
        for (IEnvironmentObserver o : this.observers) {
            o.simulatedObjectAdded(newSimulatedObject);
        }
    }

    public void removeSimulatedObject(ISimulatedObject simulatedObject) {
        simulatedObjects.remove(simulatedObject);
        if (simulatedObject instanceof Agent) {
            this.agents.remove((Agent) simulatedObject);
        }
        simulatedObjectRemoved(simulatedObject);
    }

    private void simulatedObjectRemoved(ISimulatedObject simulatedObject) {
        for (IEnvironmentObserver o : this.observers) {
            o.simulatedObjectRemoved(simulatedObject);
        }
    }

    public List<Agent> getAgentsIn(IGeometry geometry) {
        // TODO Make more efficient than iterating over all agents.
        List<Agent> result = new ArrayList<>();
        for (Agent a : getAgents()) {
            if (geometry.contains(a.getLocation())) {
                result.add(a);
            }
        }
        return result;
    }

    // NOTE: uses Vector class for populating GUI component
    public Vector<Agent> getAgentsIn(Area area) {
        Vector<Agent> result = new Vector<>();
        for (Agent a : getAgents()) {
            if (area.equals(a.getCurrentLocation())) {
                result.add(a);
            }
        }
        return result;
    }

    public Agent getAgentClosestTo(Point point) {
        // TODO Make more efficient than iterating over all agents.
        Agent result = null;
        double minDistance = Double.MAX_VALUE;
        for (Agent a : getAgents()) {
            double distance = a.getLocation().euclideanDistanceTo(point);
            if (distance < minDistance) {
                minDistance = distance;
                result = a;
            }
        }
        return result;
    }


    public void setPopulation(List<Agent> population) {
        if (this.agents == null) {
            this.agents = new ArrayList<>(population.size());
        } else {
            this.agents.clear();
        }
        this.agents.addAll(population);
        for (Agent a : population) {
            addSimulatedObject(a);
        }
    }
}
