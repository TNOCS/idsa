package nl.tno.idsa.viewer.eventsettings;

import nl.tno.idsa.framework.semantics_impl.variables.Variable;
import nl.tno.idsa.framework.utils.TextUtils;

import javax.swing.*;
import java.awt.*;

/**
 * Created by jongsd on 3-9-15.
 */
// TODO Document class.
public class ClassVariablePromptRenderer implements EventParameterRendererComponent<Class> {

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
