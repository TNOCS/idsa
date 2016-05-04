package nl.tno.idsa.viewer.eventsettings;

import nl.tno.idsa.framework.semantics_impl.variables.Variable;

import javax.swing.*;

/**
 * Created by jongsd on 3-9-15.
 */
// TODO Document class.
public abstract class NumberVariablePromptRenderer<N extends Number> implements EventParameterRendererComponent<Number> {

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
