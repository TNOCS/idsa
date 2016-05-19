package nl.tno.idsa.viewer.incidentsettings;

import nl.tno.idsa.framework.semantics_impl.variables.Variable;

/**
 * Double variable prompt.
 */
public class DoubleVariablePromptRenderer extends NumberVariablePromptRenderer<Double> {

    public DoubleVariablePromptRenderer(String variableDescription, Variable<Double> variable) {
        super(variableDescription, variable);
    }

    @Override
    public Double getValue() {
        return Double.parseDouble(getValueString()); // If we do getUserComponent.getText(), we get an old version.
    }
}
