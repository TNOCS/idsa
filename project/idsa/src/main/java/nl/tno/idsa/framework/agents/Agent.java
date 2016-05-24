package nl.tno.idsa.framework.agents;

import nl.tno.idsa.Constants;
import nl.tno.idsa.framework.behavior.activities.concrete.Activity;
import nl.tno.idsa.framework.behavior.activities.concrete.TimeInterval;
import nl.tno.idsa.framework.behavior.models.Model;
import nl.tno.idsa.framework.behavior.triggers.AreaTrigger;
import nl.tno.idsa.framework.behavior.triggers.MovingAreaTrigger;
import nl.tno.idsa.framework.messaging.Messenger;
import nl.tno.idsa.framework.population.*;
import nl.tno.idsa.framework.semantics_base.SemanticLibrary;
import nl.tno.idsa.framework.semantics_base.relations.SemanticRelation;
import nl.tno.idsa.framework.semantics_impl.groups.Group;
import nl.tno.idsa.framework.semantics_impl.relations.RoleEnablesTrigger;
import nl.tno.idsa.framework.semantics_impl.roles.Role;
import nl.tno.idsa.framework.simulator.ISimulatedObject;
import nl.tno.idsa.framework.utils.RandomNumber;
import nl.tno.idsa.framework.world.*;
import nl.tno.idsa.library.locations.House;
import nl.tno.idsa.library.locations.Inside;

import java.util.*;

/**
 * All functionality for a member of the population.
 */
public class Agent implements ISimulatedObject {

    private static long ID_COUNTER = 0;
    private final long id; // Unique.

    private String[] name;
    private final double age;
    private final Gender gender;
    private final HouseholdTypes householdType;
    private HouseholdRoles householdRole;

    private Area house;
    private Area currentLocation;
    private Group household;

    private final Group contacts;

    private Class<? extends Role> role;
    private final Stack<Model> modelStack;

    private final List<Activity> agenda;

    private Point position;
    private final double normalSpeedMs;

    private Environment environment;
    private Set<MovingAreaTrigger> triggers;

    public Agent(double age, Gender gender, HouseholdTypes householdType, HouseholdRoles householdRole, int year) {
        this.id = ID_COUNTER++;
        this.currentLocation = null;

        // Attributes
        this.age = age;
        this.gender = gender;
        this.household = new Group();
        this.householdType = householdType;
        this.householdRole = householdRole;
        this.agenda = new ArrayList<>();
        this.contacts = new Group();

        // Steering
        this.position = new Point(0, 0);
        this.normalSpeedMs = WalkingSpeedData.getComfortableSpeedInMs(age, gender);

        // Name generation
        if (name == null) {
            try {
                name = NameGenerator.getInstance(Constants.POPULATION_NAMES_LANGUAGE_ID).generateName(year, gender, age);
            } catch (Exception e) {
                // e.printStackTrace();
                name = new String[]{"?", "?"};
            }
        }

        // Models
        modelStack = new Stack<>();
    }

    public boolean isInside() {
        return currentLocation != null && currentLocation.hasFunction(Inside.class);
    }

    public void forgetCurrentLocation() {
        this.currentLocation = null;
    }

    public void setCurrentLocation(Area currentLocation) {
        this.currentLocation = currentLocation;
    }

    public Area getCurrentLocation() {
        return currentLocation;
    }

    public void clearModelStack() {
        modelStack.clear();
    }

    public String getName() {
        return name[0] + " " + name[1];
    }

    public String getFirstName() {
        return name[0];
    }

    public String getLastName() {
        return name[1];
    }

    public void adaptLastNameToHousehold() {
        // TODO It's cultural whether wife and children take the name of the father.
        // Am I a father or the man in the family?
        if (getHouseholdRole() == HouseholdRoles.FATHER || getHousehold() == null) {
            return;
        }
        if (getHouseholdRole() == HouseholdRoles.IN_RELATIONSHIP && getGender() == Gender.MALE) {
            return;
        }
        // Am I the woman in the family? Then there is a probability I take my partner's name. (For now, a probability of 0.5.)
        if (getHouseholdRole() == HouseholdRoles.IN_RELATIONSHIP && getGender() == Gender.FEMALE) {
            if (RandomNumber.nextDouble() < 0.5) {
                Agent nameGiver = null;
                for (Agent a : getHousehold()) {
                    if (a.getHouseholdRole() == HouseholdRoles.IN_RELATIONSHIP && a.getGender() == Gender.MALE) {
                        nameGiver = a;
                        nameGiver.getName();
                        break;
                    }
                }
                if (nameGiver != null) {
                    name[1] = nameGiver.getLastName();
                }
            }
            return;
        }
        // Is there a father?
        Agent nameGiver = null;
        for (Agent a : getHousehold()) {
            if (a.getHouseholdRole() == HouseholdRoles.FATHER) {
                nameGiver = a;
                nameGiver.getName();
                break;
            }
        }
        // If there is a father and I am the mother, take father's last name.
        if (nameGiver != null && getHouseholdRole() == HouseholdRoles.MOTHER) {
            name[1] = nameGiver.getLastName();
        }
        // Otherwise, is there a mother?
        if (nameGiver == null) {
            for (Agent a : getHousehold()) {
                if (a.getHouseholdRole() == HouseholdRoles.MOTHER) {
                    nameGiver = a;
                    nameGiver.getName();
                    break;
                }
            }
        }
        // Rename a child to the name giver.
        if (nameGiver != null && getHouseholdRole() == HouseholdRoles.CHILD) {
            name[1] = nameGiver.getLastName();
        }
    }

    public boolean allocateHome(Area area) {
        if (area.hasFunction(House.class) && house == null) {
            house = area;
            return true;
        }
        return false;
    }

    public void addContacts(Collection<Agent> agents) {
        contacts.addAll(agents);
    }

    public Group getContacts() {
        return contacts;
    }

    @Override
    public Point getLocation() {
        return this.position;
    }

    public void setLocation(Point location) {
        this.position = new Point(location.getX(), location.getY());

        updateCurrentLocation();
        // TODO update grid cell

        if (triggers != null) {
            //MessageBus.broadcast(this, "Attached triggers: " + triggers.size());
            for (MovingAreaTrigger trigger : triggers) {
                //MessageBus.broadcast(this, "Taking trigger with it");
                trigger.setLocation(location);
            }
        }
    }

    private void updateCurrentLocation() {
        this.currentLocation = environment == null ? null : environment.getWorld().getArea(getLocation());
    }

    public double getNormalSpeedMs() {
        return normalSpeedMs;
    }

    public double getAge() {
        return age;
    }

    public Gender getGender() {
        return gender;
    }

    public HouseholdTypes getHouseholdType() {
        return householdType;
    }

    public HouseholdRoles getHouseholdRole() {
        return householdRole;
    }

    public void setHouseholdRole(HouseholdRoles householdRole) {
        this.householdRole = householdRole;
    }

    public Vertex getHouse() {
        Vertex result = null;
        if (house != null && house.numberOfVertices() > 0) {
            result = house.getVertices().get(0);            
        }
        return result;
    }

    public Area getHouseLocation() {
        return house;
    }

    @Override
    public long getId() {
        return id;
    }

    public List<Activity> getAgenda() {
        return agenda;
    }

    public Set<Agent> getAgentsWithCommonActivities() {
        HashSet<Agent> r = new HashSet<>();
        for (Activity a : getAgenda()) {
            r.addAll(a.getParticipants());
        }
        return r;
    }

    public List<TimeInterval> getFreeTimeSlots() {
        ArrayList<TimeInterval> freeSlots = new ArrayList<>(getAgenda().size() + 1);
        for (int i = 0; i < getAgenda().size() + 1; i++) {
            Time slotStart, slotEnd;
            if (i == 0) {
                slotStart = Time.MIN_TIME;
            } else {
                slotStart = getAgenda().get(i - 1).getEndTime().incrementByMinutes(0); // Clone
            }
            if (i == getAgenda().size()) {
                slotEnd = Time.MAX_TIME;
            } else {
                slotEnd = getAgenda().get(i).getStartTime().incrementByMinutes(0); // Clone
            }
            if (slotEnd.compareTo(slotStart) > 0) {
                freeSlots.add(new TimeInterval(slotStart, slotEnd));
            }
        }
        return freeSlots;
    }


    public Group getHousehold() {
        return household;
    }

    public void setHousehold(Group household) {
        this.household = household;
    }

    @Override
    public Environment getEnvironment() {
        return environment;
    }

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
        // Check current position
        updateCurrentLocation();
    }

    public boolean supportsRole(Class<? extends Role> role) {
        boolean result = true;
        if (role != null) {            
            try {
                Role roleInstance = SemanticLibrary.getInstance().getSemanticPrototypeInstance(role);
                result = roleInstance != null && roleInstance.isAgentSuitable(this);
            } catch (InstantiationException e) {
                result = false;
            }
        }
        return result;
    }

    public Class<? extends Role> getRole() {
        return role;
    }

    public void setRole(Class<? extends Role> role) {
        Messenger.broadcast(this, "getting new role: %s", role.getSimpleName());
        this.role = role;
        Set<SemanticRelation<Role, AreaTrigger>> triggers = SemanticLibrary.getInstance().getSemanticRelationsFrom(role, RoleEnablesTrigger.class);
        if (this.triggers != null) {
            for (AreaTrigger trigger : this.triggers) {
                Messenger.broadcast(this, "destroyed existing trigger: %s.", trigger.getClass().getSimpleName());
                trigger.destroy();
            }
        }
        if (triggers.size() > 0) {
            this.triggers = new HashSet<>();
            for (SemanticRelation relation : triggers) {
                RoleEnablesTrigger triggerRelation = (RoleEnablesTrigger) relation;
                MovingAreaTrigger trigger = triggerRelation.createTrigger();
                trigger.setLocation(getLocation());
                trigger.instantiateModelVariables(environment);
                this.triggers.add(trigger);
                Messenger.broadcast(this, "created new trigger: %s.", trigger.getClass().getSimpleName());
            }
        } else {
            this.triggers = null;
            // MessageBus.broadcast(this, "removed all triggers.");
        }
    }

    public Model getCurrentModel() {
        synchronized (modelStack) {
            if (modelStack.size() > 0) {
                return modelStack.peek();
            } else {
                return null;
            }
        }
    }

    public void pushModel(Model model) {
        Model runningModel = null;
        if (modelStack.empty() || !(runningModel = modelStack.peek()).equals(model)) {
            if (runningModel != null) {
                runningModel.notifyInterrupted(this);
            }
            modelStack.add(model);
        }
    }

    public Model popModel() {
        if (modelStack.size() > 0) {
            return modelStack.pop();
        } else {
            return null;
        }
    }

    public void setPositionAccordingToActivity(Time currentTime, World world) {
        for (int i = 0; i < agenda.size(); i++) {
            Activity activity = agenda.get(i);

            // Before the first activity of the day?
            if (i == 0 && activity.getStartTime().compareTo(currentTime) >= 0) {
                setLocation(activity.getStartLocation().getPoint());
                break;
            }

            // After the last activity of the day?
            else if (i == agenda.size() - 1 && activity.getEndTime().compareTo(currentTime) <= 0) {
                setLocation(activity.getEndLocation().getPoint());
                break;
            }

            // In the middle of an activity?
            else if (activity.getStartTime().compareTo(currentTime) <= 0 && activity.getEndTime().compareTo(currentTime) >= 0) {
                Point location = getLocationOnPath(activity.getStartLocation().getPoint(), activity.getEndLocation().getPoint(), activity.getEndTime(), currentTime, world);
                setLocation(location);
                break;
            }

            // Between activities?
            else if (i > 0) {
                Activity previousActivity = agenda.get(i - 1);
                if (previousActivity.getEndTime().compareTo(currentTime) <= 0 && activity.getStartTime().compareTo(currentTime) >= 0) {
                    setLocation(activity.getStartLocation().getPoint());
                    break;
                }
            }
        }
    }

    private Point getLocationOnPath(Point start, Point end, Time endTime, Time currentTime, World world) {
        Path path = world.getPath(start, end);
        double length = path.lengthInM();
        if (length == 0) {
            return start;
        }
        // TODO Get walking speed of agent from current activity, rather than from age and gender only.
        int durationInMinutes = (int) (((WalkingSpeedData.getComfortableSpeedInMs(getAge(), getGender()) / 3.6) * length) / 60);
        Time startTime = endTime.decrementByMinutes(durationInMinutes);
        if (currentTime.compareTo(startTime) <= 0) {
            return start;
        }
        int difference = startTime.howMuchLaterInMinutesIs(currentTime);
        double ratio = Math.min(1.0d, (double) difference / durationInMinutes);
        double distanceWalked = length * ratio;
        Point result = null;
        if (distanceWalked <= path.lengthInM()) {
            result = path.moveAlong(path.get(0), distanceWalked);
        }
        return result;
    }

    public Activity searchNextActivity(Time time) {
        Activity result = null;
        int index = searchNextActivityIndex(time);
        if (index >= 0 && index < agenda.size()) {
            result = agenda.get(index);
        }
        return result;
    }

    public int searchNextActivityIndex(Time time) {
        int imin = 0;
        int imax = agenda.size() - 1;
        try {
            while (imin <= imax) {
                int imid = imin + (imax - imin) / 2;
                if (agenda.get(imid).getStartTime().compareTo(time) == 0) {
                    return imid;
                } else if (agenda.get(imid).getStartTime().compareTo(time) < 0) {
                    imin = imid + 1;
                } else {
                    imax = imid - 1;
                }
            }
        } catch (IndexOutOfBoundsException e) {
            //past last activity in agenda.
            return -1;
        }
        if (imin >= agenda.size()) {
            return -1;
        }
        return imin;
    }

    @Override
    public boolean hasNextStep() {
        // Remove finished models.
        boolean modelStackChanged = true;
        while (modelStackChanged) {
            modelStackChanged = false;
            Model currentModel = getCurrentModel();
            if (currentModel != null && currentModel.isFinished(this)) {
                modelStack.pop();
                modelStackChanged = true;
            }
        }
        // Check agenda for next activity.
        if (modelStack.isEmpty()) {
            Activity next = searchNextActivity(environment.getTime());
            if (next != null) {
                pushModel(next.getModel());
            }
        }
        // Return whether there is a next step.
        return getCurrentModel() != null;
    }

    public boolean isRunning(Model model) {
        return model.equals(getCurrentModel());
    }

    public boolean isPartOfEvent() {
        boolean ret = false;
        Model currentModel = getCurrentModel();
        if (currentModel != null) {
            ret = currentModel.isPartOfIncident();
        }
        return ret;
    }

    @Override
    public boolean nextStep(double durationInSeconds) {
        Model currentModel = getCurrentModel(); // Should not be null, hasNextStep will return false then.
        if (currentModel.agentsCanWaitBeforeStarting() && currentModel.shouldWaitForAgents()) {
            currentModel.nextWaitingStep(this, durationInSeconds);
        } else {
            currentModel.nextStep(durationInSeconds);
        }

        // TODO Perhaps sometimes, the model that is below the topmost model should be able to take over. ...
        // For example, we should be part of a procession before we reach EXACTLY the starting point.

        return true;
    }

    @Override
    public String toString() {
        return getFirstName() + " (" + (int) getAge() + ")";
    }
}
