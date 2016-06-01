/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.tno.idsa.framework.behavior.planners;

import nl.tno.idsa.framework.behavior.incidents.Incident;
import nl.tno.idsa.framework.behavior.incidents.PlannedIncident;
import nl.tno.idsa.framework.world.Environment;

/**
 * Incident planner class is the entry point for creating run-time incidents.
 */
public class IncidentPlanner {
    /**
     * Create an incident plan.
     *
     * @param env      The environment the plan takes place in.
     * @param incident The incident we want to plan.
     * @return A planned incident.
     */
    public static PlannedIncident plan(Environment env, Incident incident) {
        PlannedIncident plannedIncident = IncidentActionPlanner.getInstance().planIncidentActions(env, incident);
        IncidentAgentAndLocationSampler.instantiatePlan(env, plannedIncident);
        return plannedIncident;
    }
}
