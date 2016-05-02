package nl.tno.idsa.library.models;

import nl.tno.idsa.framework.behavior.models.SynchronousSingleStepModel;
import nl.tno.idsa.library.triggers.PrecipitationTrigger;

/**
 * Created by jongsd on 15-10-15.
 */
public class ModelPrecipitation extends SynchronousSingleStepModel { // TODO Not used.

    private int radius;

    public void setRadius(int radius) {
        this.radius = radius;
    }

    @Override
    protected boolean doSingleStep() {
        radius = 100;
        PrecipitationTrigger precipitationTrigger = new PrecipitationTrigger(getLocationAndEndTime().getLocation().getCenterPoint(), radius);
        precipitationTrigger.instantiateModelVariables(getEnvironment());
        return true;
    }
}
