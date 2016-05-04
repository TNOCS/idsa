package nl.tno.idsa.framework.simulator;

import nl.tno.idsa.framework.world.Environment;
import nl.tno.idsa.framework.world.Point;

// TODO Document code.

public interface ISimulatedObject {

    public Point getLocation();

    long getId();

    Environment getEnvironment();

    void setEnvironment(Environment environment);

    public boolean hasNextStep();

    public boolean nextStep(double durationInSeconds);

    @Override
    public boolean equals(Object other);
}
