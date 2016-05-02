/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.tno.idsa.framework.planners;

import nl.tno.idsa.framework.behavior.incidents.Incident;
import nl.tno.idsa.framework.utils.Tuple;
import nl.tno.idsa.framework.world.Environment;

/**
 * @author smelikrm
 */

// TODO This is a very simple utility class. Document it in a way that shows it is an entry point.
public class IncidentPlanner {
    public static Tuple<ActionPlan, Boolean> plan(Environment env, Incident incident) throws Exception {
        ActionPlan result = ActionPlanner.getInstance().createPlan(env, incident);
        boolean planFound = AgentAndLocationSampler.instantiatePlan(env, result);
        if (planFound) {
            result.startModels(env);
        }
        return new Tuple<>(result, planFound);
    }
}
