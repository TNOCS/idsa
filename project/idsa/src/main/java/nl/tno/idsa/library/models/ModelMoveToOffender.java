package nl.tno.idsa.library.models;

/**
 * Created by jongsd on 18-8-15.
 */
@SuppressWarnings("unused")
public class ModelMoveToOffender extends BasicMovementModel {

    // private boolean firstStep = true;
    // private HashMap<Agent, Agent> chasingActors;

    public ModelMoveToOffender() {
        super(RUNNING_SPEED_MS);
    }

    @Override
    public boolean doStep(double durationInSeconds) {
        return super.doStep(durationInSeconds);
        // TODO The find and chase code below does not work.
//        if (firstStep) {
//            Reporter.getInstance().broadcast(String.format("%s start(s) moving to offender(s) %s, will end up at %s%n", getActors(), getTargets(), getLocationAndEndTime()));
//            firstStep = false;
//            chasingActors = new HashMap<>();
//        }
//        boolean moved = false;
//        for (Agent actor : getActors()) {
//            if (chasingActors.containsKey(actor)) {
//                Agent target = chasingActors.get(actor);
//                if (! (target.getLocation().equals(getLocationAndEndTime().getLocation()) && actor.getLocation().equals(target.getLocation()))) {
//                    List<Edge> actorPath = getEnvironment().getWorld().getPath(actor.getLocation(), target.getLocation()); // Keep re-planning this (short) path.
//                    paths.put(actor, actorPath); // Superclass will execute this path.
//                }
//            }
//            else {
//                for (Agent target : getTargets()) {
//                    if (getEnvironment().getWorld().getPathLengthInM(actor.getLocation(), target.getLocation()) < 50) { // TODO Magic number.
//                        chasingActors.put(actor, target);
//                        Reporter.getInstance().broadcast(String.format("%n%s starts chasing %s towards %s%n", actor, target, getLocationAndEndTime()));
//                    }
//                }
//            }
//        }
//        return super.doStep(durationInSeconds);
    }
}
