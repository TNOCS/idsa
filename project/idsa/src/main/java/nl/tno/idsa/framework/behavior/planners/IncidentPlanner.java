/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.tno.idsa.framework.behavior.planners;

import nl.tno.idsa.framework.behavior.incidents.Incident;
import nl.tno.idsa.framework.behavior.plans.ActionPlan;
import nl.tno.idsa.framework.utils.Tuple;
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
     * @return A tuple with an action plan, including agents and locations, plus a boolean indicating whether the plan realizes the incident within the time required.
     * @throws Exception
     */
    public static Tuple<ActionPlan, Boolean> plan(Environment env, Incident incident) throws Exception {
        ActionPlan result = IncidentActionPlanner.getInstance().createPlan(env, incident);
        boolean planFound = IncidentAgentAndLocationSampler.instantiatePlan(env, result);
        if (planFound) {
            result.startModels(env);
        }
        return new Tuple<>(result, planFound);
    }
}
