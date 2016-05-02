package nl.tno.idsa.framework.behavior.models;

import nl.tno.idsa.framework.world.Environment;

/**
 * Interface for all objects that have a model. (E.g. actions, triggers.)
 */
public interface IModelOwner {
    Model getModel();

    void instantiateModelVariables(Environment environment);
}
