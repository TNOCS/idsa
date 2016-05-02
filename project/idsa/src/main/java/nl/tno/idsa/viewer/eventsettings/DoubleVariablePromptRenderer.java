package nl.tno.idsa.viewer.eventsettings;

import nl.tno.idsa.framework.semantics_impl.variables.Variable;

/**
 * Created by jongsd on 17-9-15.
 */
// TODO Document class.
public class DoubleVariablePromptRenderer extends NumberVariablePromptRenderer<Double> {

    public DoubleVariablePromptRenderer(String variableDescription, Variable<Double> variable) {
        super(variableDescription, variable);
    }

    @Override
    public Double getValue() {
        return Double.parseDouble(getValueString()); // If we do getUserComponent.getText(), we get an old version.
    }
}
