package nl.tno.idsa.viewer.eventsettings;

import nl.tno.idsa.framework.behavior.incidents.Incident;
import nl.tno.idsa.framework.semantics_base.JavaSubclassFinder;
import nl.tno.idsa.framework.utils.TextUtils;
import nl.tno.idsa.framework.world.World;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.Set;
import java.util.Vector;

/**
 * Created by jongsd on 3-9-15.
 */
// TODO Document class.
public class IncidentSelectorDialog extends JDialog {

    private final World world;

    private Incident selectedIncident;

    public IncidentSelectorDialog(Frame parent, World world) {

        super(parent);
        this.world = world;

        setTitle("Select an incident");
        setModal(true);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        JPanel contentPane = new JPanel(new BorderLayout(3, 3));
        contentPane.setBorder(new EmptyBorder(3, 3, 3, 3));
        setContentPane(contentPane);

        Vector<Incident> incidents = listEvents();
        if (incidents.size() == 0) {
            JOptionPane.showMessageDialog(parent, "No incidents found", "Error", JOptionPane.ERROR_MESSAGE);
            selectedIncident = null;
            return;
        }

        final JList<Incident> incidentList = new JList<Incident>(incidents);
        incidentList.setSelectedIndex(0);
        incidentList.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                Incident incident = (Incident) value;
                String valueStr = TextUtils.camelCaseToText(incident.getName()) + " (" + incident.getDescription() + ")";
                return super.getListCellRendererComponent(list, valueStr, index, isSelected, cellHasFocus);
            }
        });
        getContentPane().add(new JScrollPane(incidentList), BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 3, 3));
        getContentPane().add(buttonPanel, BorderLayout.SOUTH);

        JButton okButton = new JButton(new AbstractAction("Insert") {
            @Override
            public void actionPerformed(ActionEvent e) {
                selectedIncident = incidentList.getSelectedValue();
                dispose();
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

    @SuppressWarnings("unchecked")
    private Vector<Incident> listEvents() {
        Set<Class<? extends Incident>> eventClasses = JavaSubclassFinder.listSubclasses(Incident.class);
        Vector<Incident> incidents = new Vector<Incident>();
        for (Class<? extends Incident> eventClass : eventClasses) {
            Constructor<? extends Incident> constructor = null;
            Constructor[] allConstructors = eventClass.getDeclaredConstructors();
            for (Constructor ctor : allConstructors) {
                Class<?>[] pType = ctor.getParameterTypes();
                if (pType.length == 1 && pType[0].equals(World.class)) {
                    // Found valid constructor
                    constructor = ctor;
                }
            }
            if (constructor != null) {
                try {
                    Incident newIncident = constructor.newInstance(world);
                    incidents.add(newIncident);
                } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                    // Should not happen!   TODO Gracefully report a broken event.
                    ex.printStackTrace();
                }
            }
        }
        Collections.sort(incidents);
        return incidents;
    }

    public Incident getSelectedIncident() {
        return selectedIncident;
    }
}
