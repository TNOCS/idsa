package nl.tno.idsa.framework.world;

import nl.tno.idsa.framework.simulator.ISimulatedObject;

// TODO split this up into multiple types of observers
// TODO Document class.
public interface IEnvironmentObserver {

    void timeChanged(Time newTime);

    void simulatedObjectAdded(ISimulatedObject newSimulatedObject);

    void simulatedObjectRemoved(ISimulatedObject newSimulatedObject);

    // Etc.
}
