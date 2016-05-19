package nl.tno.idsa.viewer.incidentsettings;

import nl.tno.idsa.framework.semantics_impl.variables.Variable;
import nl.tno.idsa.framework.utils.TextUtils;
import nl.tno.idsa.viewer.components.PromptRenderer;

import javax.swing.*;
import java.awt.*;

/**
 * Class variable prompt.
 */
public class ClassVariablePromptRenderer implements PromptRenderer<Class> {

    private final String variableDescription;
    private final Variable<Class> variable;
    private JComboBox<Class> inputBox;

    public ClassVariablePromptRenderer(String variableDescription, Variable<Class> variable) {
        this.variableDescription = variableDescription;
        this.variable = variable;
    }

    @Override
    public JComponent getLabelComponent() {
        return new JLabel(variableDescription);
    }

    @Override
    public JComponent getUserInputComponent() {
        inputBox = new JComboBox<Class>();
        inputBox.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                Class clz = (Class) value;   // Why not make this bloody list cell renderer properly generic... sigh.
                return super.getListCellRendererComponent(list, TextUtils.camelCaseToText(clz.getSimpleName()), index, isSelected, cellHasFocus);
            }
        });
        DefaultComboBoxModel<Class> model = new DefaultComboBoxModel<>();
        for (Class clz : variable.getDomain()) {
            model.addElement(clz);
        }
        inputBox.setModel(model);
        if (variable.getValue() != null) {
            model.setSelectedItem(variable.getValue());
        }
        return inputBox;
    }

    @Override
    public Class getValue() {
        return (inputBox.getItemAt(inputBox.getSelectedIndex())); // TODO Error check.
    }
}
