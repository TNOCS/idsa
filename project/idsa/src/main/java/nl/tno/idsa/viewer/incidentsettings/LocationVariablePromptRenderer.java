package nl.tno.idsa.viewer.incidentsettings;

import nl.tno.idsa.framework.semantics_impl.locations.LocationAndTime;
import nl.tno.idsa.framework.semantics_impl.variables.LocationVariable;
import nl.tno.idsa.framework.world.IGeometry;
import nl.tno.idsa.framework.world.Time;
import nl.tno.idsa.viewer.components.PromptRenderer;
import nl.tno.idsa.viewer.components.TimeSetterPanel;

import javax.swing.*;

/**
 * Prompt for a location variable.
 */
public class LocationVariablePromptRenderer implements PromptRenderer<LocationAndTime> {

    private final String variableDescription;
    private final LocationVariable variable;
    private TimeSetterPanel timeSetterPanel;

    public LocationVariablePromptRenderer(String variableDescription, LocationVariable variable) {
        this.variableDescription = variableDescription;
        this.variable = variable;
    }

    @Override
    public JComponent getLabelComponent() {
        return new JLabel(variableDescription);
    }

    @Override
    public JComponent getUserInputComponent() {
        long t = variable.getValue() != null ? variable.getValue().getTimeNanos() : 0;
        timeSetterPanel = new TimeSetterPanel(new Time(t));
        return timeSetterPanel;
    }

    @Override
    public LocationAndTime getValue() {
        IGeometry location = variable.getValue() != null ? variable.getValue().getLocation() : null;
        return new LocationAndTime(location, timeSetterPanel.getValue().getNanos());
    }
}
