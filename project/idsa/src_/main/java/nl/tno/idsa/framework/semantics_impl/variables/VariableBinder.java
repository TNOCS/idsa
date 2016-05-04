package nl.tno.idsa.framework.semantics_impl.variables;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by jongsd on 7-8-15.
 */

// TODO Document code.

public class VariableBinder {
    private HashMap<Variable, PoolPlusId> pools = new HashMap<Variable, PoolPlusId>();

    public void addVariable(Variable variable) {
        if (pools.containsKey(variable)) {
            return;
        }
        HashSet<Variable> newPool = new HashSet<Variable>();
        newPool.add(variable);
        variable.setBinder(this);
        pools.put(variable, new PoolPlusId(newPool, variable.id));
    }

    public <T> void bind(Variable<T> variable1, Variable<T> variable2) {
        if (pools.containsKey(variable1)) {
            addVariable(variable1, variable2);
        } else if (pools.containsKey(variable2)) {
            addVariable(variable2, variable1);
        } else {
            addVariable(variable1);
            addVariable(variable1, variable2); // Now var1 exists.
        }
    }

    private <T> void addVariable(Variable<T> existingVariable, Variable<T> newVariable) {
        PoolPlusId poolPlusId = pools.get(existingVariable);
        HashSet<Variable> pool = poolPlusId.getPool();
        if (!pool.contains(newVariable)) {
            pool.add(newVariable);
            newVariable.setBinder(this);
            pools.put(newVariable, poolPlusId); // Reference, so it's the same pool.
            if (newVariable.getValue() != null) {
                existingVariable.setValue(newVariable.getValue()); // Make sure we copy values immediately to other variables.
            } else if (existingVariable.getValue() != null) {
                newVariable.setValue(existingVariable.getValue()); // Make sure we copy values immediately to the new variable.
            }
        }
    }

    public <T> void unbind(Variable<T> variable) {
        PoolPlusId poolPlusId = pools.get(variable);
        if (poolPlusId == null) return;
        HashSet<Variable> pool = poolPlusId.getPool();
        if (pool != null) {
            for (Variable variableInPool : pool) {
                pools.remove(variableInPool);
            }
            pool = null; // Clean up.
            poolPlusId = null; // Clean up.
        }
    }

    public boolean areVariablesBound(Variable variable1, Variable variable2) {
        PoolPlusId poolPlusId = pools.get(variable1);
        if (poolPlusId == null) return false;
        HashSet<Variable> pool = poolPlusId.getPool();
        return pool.contains(variable2);
    }

    public Set<Variable> getAllBoundVariables(Variable variable) {
        PoolPlusId poolPlusId = pools.get(variable);
        if (poolPlusId != null) {
            return poolPlusId.getPool();
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    public <T> Variable<T> getMostSpecificVariable(Variable<T> variable) {

        PoolPlusId poolPlusId = pools.get(variable);
        if (poolPlusId == null) return null;

        Variable<T> result = variable;
        HashSet<Variable> pool = poolPlusId.getPool();
        for (Variable v : pool) {
            Variable<T> intersection = result.getConstraintIntersection(v);
            if (intersection == null) {
                return null;
            }
            result = intersection;
        }

        bind(result, variable);
        pool.add(result); // Make sure the most specific variable is in the pool as well. (It's a set, so this works.)

        return result;
    }

    @SuppressWarnings("unchecked")
    public Set<Variable> getUniqueVariables() {
        Set<Variable> variablesToDo = new HashSet<Variable>(pools.keySet());
        HashSet<Variable> uniqueVariables = new HashSet<Variable>();
        boolean done = false;
        while (!done) {
            done = true;
            HashSet<Variable> variablesToRemove = new HashSet<Variable>();
            if (variablesToDo.iterator().hasNext()) {
                Variable v = variablesToDo.iterator().next();
                Variable mostSpecific = getMostSpecificVariable(v);
                uniqueVariables.add(mostSpecific);
                variablesToRemove = pools.get(v).getPool();
            }
            if (variablesToRemove.size() > 0) {
                done = false;
                for (Variable v : variablesToRemove) {
                    variablesToDo.remove(v);
                }
            }
        }
        return uniqueVariables;
    }

    @SuppressWarnings("unchecked")
    protected <T> boolean isValidValue(Variable<T> variable, T value) {
        PoolPlusId poolPlusId = pools.get(variable);
        HashSet<Variable> pool = poolPlusId.getPool();
        if (pool == null) return variable.checkValidity(value);

        for (Variable variableInPool : pool) {
            if (!variableInPool.checkValidity(value)) {
                return false;
            }
        }

        return true;
    }

    protected int getPoolId(Variable variable) {
        PoolPlusId poolPlusId = pools.get(variable);
        if (poolPlusId == null) {
            return variable.id;
        }
        HashSet<Variable> pool = poolPlusId.getPool();
        if (pool == null) {
            return variable.id;
        }
        Integer id = poolPlusId.getId();
        if (id == null) {
            return variable.id;
        }
        return id;
    }

    @SuppressWarnings("unchecked")  // This is pretty certainly safe.
    protected <T> void notifyValueUpdate(Variable<T> variable, T value) {
        PoolPlusId poolPlusId = pools.get(variable);
        HashSet<Variable> pool = poolPlusId.getPool();
        if (pool != null) {
            for (Variable variableInPool : pool) {
                variableInPool.setValue(value); // Checks whether value is already set. So no endless loop.
            }
        }
    }

    private class PoolPlusId {
        private HashSet<Variable> pool;
        private Integer id;

        private PoolPlusId(HashSet<Variable> pool, Integer id) {
            this.pool = pool;
            this.id = id;
        }

        public HashSet<Variable> getPool() {
            return pool;
        }

        public Integer getId() {
            return id;
        }
    }
}
