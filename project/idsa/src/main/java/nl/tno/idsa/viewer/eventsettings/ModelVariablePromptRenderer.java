package nl.tno.idsa.viewer.eventsettings;

import nl.tno.idsa.framework.behavior.models.Model;
import nl.tno.idsa.framework.semantics_impl.variables.Variable;
import nl.tno.idsa.framework.utils.TextUtils;

import javax.swing.*;
import java.awt.*;

/**
 * Created by jongsd on 3-9-15.
 */
// TODO Document class.
public class ModelVariablePromptRenderer implements EventParameterRendererComponent<Class<? extends Model>> {

    private final String variableDescription;
    private final Variable<Class<? extends Model>> variable;
    private JComboBox<Class<? extends Model>> inputBox;

    public ModelVariablePromptRenderer(String variableDescription, Variable<Class<? extends Model>> variable) {
        this.variableDescription = variableDescription;
        this.variable = variable;
    }

    @Override
    public JComponent getLabelComponent() {
        return new JLabel(variableDescription);
    }

    @Override
    public JComponent getUserInputComponent() {
        inputBox = new JComboBox<Class<? extends Model>>();
        inputBox.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                Class model = (Class) value;   // Why not make this bloody list cell renderer properly generic... sigh.
                return super.getListCellRendererComponent(list, TextUtils.camelCaseToText(model.getSimpleName()), index, isSelected, cellHasFocus);
            }
        });
        DefaultComboBoxModel<Class<? extends Model>> boxModel = new DefaultComboBoxModel<>();
        for (Class<? extends Model> model : variable.getDomain()) {
            boxModel.addElement(model);
        }
        inputBox.setModel(boxModel);
        if (variable.getValue() != null) {
            boxModel.setSelectedItem(variable.getValue());
        }
        return inputBox;
    }

    @Override
    public Class<? extends Model> getValue() {
        return (inputBox.getItemAt(inputBox.getSelectedIndex())); // TODO Error check.
    }
}
