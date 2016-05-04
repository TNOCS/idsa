package nl.tno.idsa.framework.behavior.triggers;

import nl.tno.idsa.framework.agents.Agent;
import nl.tno.idsa.framework.behavior.models.IModelOwner;
import nl.tno.idsa.framework.behavior.models.Model;
import nl.tno.idsa.framework.semantics_impl.groups.Group;
import nl.tno.idsa.framework.semantics_impl.locations.LocationAndTime;
import nl.tno.idsa.framework.semantics_impl.roles.Role;
import nl.tno.idsa.framework.simulator.ISimulatedObject;
import nl.tno.idsa.framework.world.Environment;
import nl.tno.idsa.framework.world.IGeometry;
import nl.tno.idsa.library.roles.Civilian;

import java.util.ArrayList;
import java.util.List;

/**
 * Base class for area triggers. Note that triggers only work on agents that have no role or the Civilian role.
 */
public abstract class AreaTrigger implements ISimulatedObject, IModelOwner {

    private static long ID_COUNTER = 0;
    private long id = ID_COUNTER++;

    private IGeometry shape;
    private Environment environment;

    private final double probability;
    private long expirationTime = LocationAndTime.UNDEFINED_TIME;

    // TODO Somehow triggers keep being activated even after being destroyed. ...
    // The boolean below is a "quick fix". Better fix it properly!
    private boolean destroyed;

    public AreaTrigger(IGeometry shape, double activationProbability) {
        this.shape = shape;
        this.probability = activationProbability;
    }

    protected IGeometry getShape() {
        return shape;
    }

    @Override
    public long getId() {
        return id;
    }

    public Environment getEnvironment() {
        return environment;
    }

    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    public void destroyAfter(long expirationTime) {
        this.expirationTime = environment.getTime().getNanos() + expirationTime;
    }

    @Override
    public void instantiateModelVariables(Environment environment) {
        if (this.environment != null) {
            destroy();
        }
        this.environment = environment;
        this.environment.addSimulatedObject(this);
        Model model = getModel();
        if (model != null) {
            if (model.getActors() == null) {
                model.setActors(new Group());
            }
            model.setEnvironment(environment);
        }
    }

    public void destroy() {

        // Quick fix.
        if (destroyed) {
            return;
        }
        destroyed = true;

        // Remove the trigger from the environment so it is no longer activated.
        if (environment != null) {
            // TODO Environment.removeSimulatedObject(this) method call does not seem to work!
            environment.removeSimulatedObject(this);
        }

        // Clean up model. Not really needed because the trigger will not be called anymore.
        getModel().getActors().clear();
        if (getModel().getTargets() != null) {
            getModel().getTargets().clear();
        }
    }

    @Override
    public boolean hasNextStep() {

        // Quick fix.
        if (destroyed) {
            return false;
        }

        if (expirationTime != LocationAndTime.UNDEFINED_TIME) {
            boolean active = environment.getTime().getNanos() < expirationTime;
            if (!active) {
                destroy();
            }
            return active;
        }
        return true; // A trigger can stay active until it is destroyed explicitly.
    }

    @Override
    public boolean equals(Object other) {
        return false;
    }

    @Override
    public boolean nextStep(double durationInSeconds) {

        // Quick fix.
        if (destroyed) {
            return false;
        }

        // Get model.
        Model model = getModel();
        if (model == null) {
            return false;
        }

        // Apply only to agents that are not immune due to their role (any role except Civilian makes them immune).
        List<Agent> affectedAgents = determineAffectedAgents(probability);
        List<Agent> immuneAgents = new ArrayList<>();
        for (Agent agent : affectedAgents) {
            Class<? extends Role> role = agent.getRole();
            if (role != null && role != Civilian.class) {
                immuneAgents.add(agent);
            }
        }
        for (Agent immuneAgent : immuneAgents) {
            affectedAgents.remove(immuneAgent);
        }

        // Who is no longer affected?
        List<Agent> agentsNoLongerAffected = new ArrayList<>();
        for (Agent agent : model.getActors()) {
            if (!affectedAgents.contains(agent) || model.isFinished(agent)) {
                agent.popModel();
                agentsNoLongerAffected.add(agent);
                //MessageBus.broadcast(agent, "will no longer be affected by trigger " + this.getClass().getSimpleName());
            }
        }
        for (Agent agent : agentsNoLongerAffected) {
            model.getActors().remove(agent);
        }

        // Who is newly affected?
        for (Agent agent : affectedAgents) {
            if (!model.getActors().contains(agent)) {
                if (!agent.isRunning(model)) {
                    agent.pushModel(model);
                }
                model.getActors().add(agent);
                //MessageBus.broadcast(agent, "will be affected by trigger " + this.getClass().getSimpleName());
            }
        }

        // Execute the model as long as the trigger exists. New agents may come into the trigger area!
        return model.nextStep(durationInSeconds);
    }

    /**
     * Each trigger must determine which agents it will affect every time step.
     */
    protected abstract List<Agent> determineAffectedAgents(double probability);
}
