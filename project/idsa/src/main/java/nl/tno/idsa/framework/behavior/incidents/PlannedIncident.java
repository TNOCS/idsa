package nl.tno.idsa.framework.behavior.incidents;

import nl.tno.idsa.framework.behavior.plans.ActionPlan;

/**
 * Composite of an incident, the plan belonging to the incident, plus an indication whether the plan is
 * instantiated with concrete agents and locations, and whether it is realized within desired time constraints.
 */
public class PlannedIncident {
    private Incident incident;
    private ActionPlan actionPlan;

    public enum Status {UNINSTANTIATED, INSTANTIATED_WITHIN_TIME_CONSTRAINTS, INSTANTIATED_WITHOUT_TIME_CONSTRAINTS}

    private Status status;

    public PlannedIncident(Incident incident, ActionPlan actionPlan) {
        this.incident = incident;
        this.actionPlan = actionPlan;
        this.status = Status.UNINSTANTIATED;
    }

    public Incident getIncident() {
        return incident;
    }

    public ActionPlan getActionPlan() {
        return actionPlan;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }
}
