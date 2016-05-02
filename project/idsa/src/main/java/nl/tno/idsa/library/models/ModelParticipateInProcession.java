package nl.tno.idsa.library.models;

import nl.tno.idsa.framework.agents.Agent;
import nl.tno.idsa.framework.behavior.models.AsynchronousSingleStepModel;
import nl.tno.idsa.framework.behavior.models.SynchronousSingleStepModel;
import nl.tno.idsa.framework.messaging.Messenger;
import nl.tno.idsa.framework.semantics_impl.groups.Group;
import nl.tno.idsa.framework.semantics_impl.locations.LocationAndTime;
import nl.tno.idsa.framework.world.IGeometry;
import nl.tno.idsa.framework.world.Path;
import nl.tno.idsa.framework.world.PolyLine;
import nl.tno.idsa.framework.world.Time;
import nl.tno.idsa.library.roles.Civilian;
import nl.tno.idsa.library.roles.Reveller;
import nl.tno.idsa.library.triggers.StaticAttractingTrigger;

/**
 * Model that makes agents walk in a procession.
 * This model works by pushing a new movement model onto each agent.
 */
public class ModelParticipateInProcession extends AsynchronousSingleStepModel {

    private double desiredDistanceM = 100;
    private double walkingSpeedKmH = 3;
    private double delayBetweenParticipantsS = 3;

    public void setDesiredDistance(double desiredDistanceM) {
        this.desiredDistanceM = desiredDistanceM;
    }

    public void setWalkingSpeed(double walkingSpeedKmH) {
        this.walkingSpeedKmH = walkingSpeedKmH;
    }

    public void setDelayBetweenParticipants(double delayBetweenParticipantsS) {
        this.delayBetweenParticipantsS = delayBetweenParticipantsS;
    }

    @Override
    protected boolean doSingleStep() {

        // Handle the procession location and path.
        IGeometry location = getLocationAndEndTime().getLocation();

        // Take the provided path if there is one.
        Path processionPath;
        if (location instanceof PolyLine) {
            processionPath = new Path((PolyLine) location);
        }

        // Create a path for the procession if there was no path yet.
        else {
            Double desiredDistance = desiredDistanceM;
            Path sampledPath = null;
            int wideningAttempts = 0;
            while (sampledPath == null && wideningAttempts < 5) {
                sampledPath = getEnvironment().getWorld().samplePathWithLength(location.getLastPoint(), desiredDistance);
                if (sampledPath == null) {
                    desiredDistance *= 1.2; // Try to find a vertex further away.
                }
            }
            if (sampledPath == null) {
                Messenger.broadcast("Procession cannot happen with a distance of " + desiredDistance + "m.");
                return false;
            }
            processionPath = sampledPath;
        }

        // Attract viewers.
        final StaticAttractingTrigger staticAttractingTrigger = new StaticAttractingTrigger(processionPath.getPolyLine(), 100, 0.25); // TODO Magic constants, 20 meters and 20% chance.
        staticAttractingTrigger.instantiateModelVariables(getEnvironment());
        // getEnvironment().addSimulatedObject(staticAttractingTrigger); // already happens in instantiate

        // Create a path for each individual agent.
        long startTime = getLocationAndEndTime().getTimeNanos();
        for (int i = 0; i < getActors().size(); i++) {

            Agent agent = getActors().get(i);
            agent.setRole(Reveller.class);   // Make sure other agents come and watch :)

            BasicMovementModel movement = new BasicMovementModel(walkingSpeedKmH / 3.6);

            movement.setActors(new Group(agent));
            movement.setPartOfIncident(true); // Make sure the agents are not interrupted.
            Path noisyPath = processionPath.generateCustomPath(0.1, 3); // TODO Spread is hardcoded. Also it seems 0 instead of 0.1 is crashing the lot.

            movement.setLocationAndEndTime(new LocationAndTime(noisyPath.getPolyLine(), LocationAndTime.UNDEFINED_TIME));
            movement.setEnvironment(getEnvironment());
            double agentDelay = Time.NANO_SECOND * delayBetweenParticipantsS * i;
            long desiredEndTime = getLocationAndEndTime().getTimeNanos();
            if (desiredEndTime != LocationAndTime.UNDEFINED_TIME) {
                movement.getLocationAndEndTime().setTimeNanos((long) (desiredEndTime + agentDelay));
            }
            movement.setForcedStartTime((long) (startTime + agentDelay));

            // Once the procession finishes, there should be no more viewers.
            // Make a model that removes the trigger for the last agent in the row.
            if (i == getActors().size() - 1) {
                SynchronousSingleStepModel removeTriggerModel = new SynchronousSingleStepModel() {
                    @Override
                    protected boolean doSingleStep() {
                        staticAttractingTrigger.destroy();
                        return true;
                    }
                };
                removeTriggerModel.setEnvironment(getEnvironment());
                removeTriggerModel.setActors(new Group(agent));
                agent.pushModel(removeTriggerModel);
            }

            // Once the procession finishes, the agent stops being a reveller.
            SetRoleModel setRoleModel = new SetRoleModel(Civilian.class, SetRoleModel.Who.ACTORS);
            setRoleModel.setEnvironment(getEnvironment());
            setRoleModel.setActors(new Group(agent));
            agent.pushModel(setRoleModel);

            // Move.
            agent.pushModel(movement);
        }

        // Yay, great success.
        return true;
    }
}
