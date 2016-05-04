package nl.tno.idsa.viewer.eventsettings;

import nl.tno.idsa.framework.behavior.incidents.Incident;
import nl.tno.idsa.framework.semantics_base.objects.ParameterId;
import nl.tno.idsa.framework.semantics_impl.variables.LocationVariable;
import nl.tno.idsa.framework.semantics_impl.variables.Variable;
import nl.tno.idsa.framework.world.Environment;
import nl.tno.idsa.viewer.components.TimeSetterPanel;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by jongsd on 3-9-15.
 */
// TODO Document class.
public class IncidentParameterDialog extends JDialog {

    // TODO Add functionality (complex) that allows specifying the parameters of actions ...
    // when the incident's enabling action is abstract. Example: "Road block" is enabled by RoadBlock, and Arrest
    // implements RoadBlock. Now, the user can only see a default arrest. In order to specify the number of offenders
    // and arresting officers, the user must place an event "Arrest". The complexity is that parameters are specified
    // via incidents (IncidentArrest) instead of actions, while action hierarchy is an action concept. In other words, for
    // this to work, we must find an incident that has Arrest as the enabling action and plan this incident instead of
    // "Road block". This sounds like a 'cheating' way around a clear design choice.

    private IncidentParameterDialog me = this;
    private Incident selectedIncident;
    private Map<Variable, EventParameterRendererComponent> componentMap = new HashMap<>();

    public IncidentParameterDialog(final Frame parent, final Incident incident, final Environment environment) {

        super(parent);

        selectedIncident = incident;

        setTitle("Set incident parameters");
        setModal(true);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        JPanel contentPane = new JPanel(new BorderLayout(3, 3));
        contentPane.setBorder(new EmptyBorder(3, 3, 3, 3));
        setContentPane(contentPane);

        Map<ParameterId, Variable> parameters = incident.getParameters();

        JPanel mainPanel = new JPanel(new BorderLayout(3, 3));
        getContentPane().add(mainPanel, BorderLayout.CENTER);

        JPanel labelPanel = new JPanel(new GridLayout(parameters.size(), 1, 3, 3));
        mainPanel.add(labelPanel, BorderLayout.WEST);
        JPanel promptPanel = new JPanel(new GridLayout(parameters.size(), 1, 3, 3));
        mainPanel.add(promptPanel, BorderLayout.CENTER);

        for (ParameterId parameterId : parameters.keySet()) {
            if (parameterId.equals(Incident.Parameters.LOCATION_VARIABLE)) {  // Explicitly do this one. It could be included with the rest, but gets treated exceptionally anyway.
                continue;
            }
            Variable variable = parameters.get(parameterId);
            EventParameterRendererComponent renderer = VariablePromptRendererFactory.getInstance().getRenderer(parameterId + "", variable); // TODO Better string representation of parameter.
            if (renderer != null) {
                labelPanel.add(renderer.getLabelComponent());
                promptPanel.add(renderer.getUserInputComponent());
                componentMap.put(variable, renderer);
            } else {
                labelPanel.add(new JLabel(parameterId + ""));
                promptPanel.add(new JLabel("This parameter cannot be set"));
            }
        }

        labelPanel.add(new JLabel("Time of incident"));
        final TimeSetterPanel timeSetterPanel = new TimeSetterPanel(environment.getTime().incrementByMinutes(10)); // TODO Set some default in a different way than using a magic constant.
        promptPanel.add(timeSetterPanel);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 3, 3));
        getContentPane().add(buttonPanel, BorderLayout.SOUTH);

        JButton okButton = new JButton(new AbstractAction("OK") {

            @Override
            @SuppressWarnings("unchecked")
            public void actionPerformed(ActionEvent e) {

                for (Variable v : componentMap.keySet()) {
                    EventParameterRendererComponent eventParameterRendererComponent = componentMap.get(v);
                    v.setValue(eventParameterRendererComponent.getValue());
                }

                LocationVariable lv = (LocationVariable) selectedIncident.getParameters().get(Incident.Parameters.LOCATION_VARIABLE);
                lv.getValue().setTimeNanos(timeSetterPanel.getValue().getNanos());

                // Test whether the plan is feasible (time > 0).
                if (lv.getValue().getTimeNanos() <= environment.getTime().getNanos()) {
                    JOptionPane.showMessageDialog(null, String.format("The incident %s should take place in the future.", incident));
                    return;
                }

                boolean success = selectedIncident.bindParameters();
                if (success) {
                    dispose();
                } else {
                    JOptionPane.showMessageDialog(me, "Parameters are invalid.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        okButton.setFocusPainted(false);
        buttonPanel.add(okButton);

        JButton cancelButton = new JButton(new AbstractAction("Cancel") {
            @Override
            public void actionPerformed(ActionEvent e) {
                selectedIncident = null;
                dispose();
            }
        });
        buttonPanel.add(cancelButton);
        cancelButton.setFocusPainted(false);

        pack();
        setLocationRelativeTo(null);
    }

    public Incident getSelectedIncident() {
        return selectedIncident;
    }
}
