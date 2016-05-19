package nl.tno.idsa.viewer.incidentsettings;

import nl.tno.idsa.framework.semantics_impl.variables.Variable;

/**
 * Prompt for integer variables.
 */
public class IntegerVariablePromptRenderer extends NumberVariablePromptRenderer<Integer> {

    public IntegerVariablePromptRenderer(String variableDescription, Variable<Integer> variable) {
        super(variableDescription, variable);
    }

    @Override
    public Integer getValue() {
        return Integer.parseInt(getValueString()); // If we do getUserComponent.getText(), we get an old version.
    }
}
