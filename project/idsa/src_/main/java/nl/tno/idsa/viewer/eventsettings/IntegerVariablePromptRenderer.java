package nl.tno.idsa.viewer.eventsettings;

import nl.tno.idsa.framework.semantics_impl.variables.Variable;

/**
 * Created by jongsd on 17-9-15.
 */
// TODO Document class.
public class IntegerVariablePromptRenderer extends NumberVariablePromptRenderer<Integer> {

    public IntegerVariablePromptRenderer(String variableDescription, Variable<Integer> variable) {
        super(variableDescription, variable);
    }

    @Override
    public Integer getValue() {
        return Integer.parseInt(getValueString()); // If we do getUserComponent.getText(), we get an old version.
    }
}
