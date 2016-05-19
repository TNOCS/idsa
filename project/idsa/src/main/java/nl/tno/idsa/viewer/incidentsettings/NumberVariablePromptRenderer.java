package nl.tno.idsa.viewer.incidentsettings;

import nl.tno.idsa.framework.semantics_impl.variables.Variable;
import nl.tno.idsa.viewer.components.PromptRenderer;

import javax.swing.*;

/**
 * Superclass for prompt renderers for numbers.
 */
public abstract class NumberVariablePromptRenderer<N extends Number> implements PromptRenderer<Number> {

    private final String variableDescription;
    private final Variable<N> variable;
    private JFormattedTextField inputField;

    public NumberVariablePromptRenderer(String variableDescription, Variable<N> variable) {
        this.variableDescription = variableDescription;
        this.variable = variable;
    }

    @Override
    public JComponent getLabelComponent() {
        return new JLabel(variableDescription);
    }

    @Override
    public JTextField getUserInputComponent() {
        inputField = new JFormattedTextField();
        if (variable.getValue() != null) {
            inputField.setText("" + variable.getValue());
        }
        return inputField;
    }

    @Override
    public abstract N getValue();

    protected final String getValueString() {
        if (inputField.isEditValid()) try {
            inputField.commitEdit();
        } catch (Exception e) {
        }
        return inputField.getText();
    }
}
