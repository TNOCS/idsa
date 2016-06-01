package nl.tno.idsa.viewer.inspectors;

import nl.tno.idsa.framework.agents.Agent;
import nl.tno.idsa.framework.behavior.incidents.PlannedIncident;
import nl.tno.idsa.framework.world.Time;
import nl.tno.idsa.viewer.components.CollapsePanel;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Created by jongsd on 27-10-15.
 */
// TODO Document class.
public abstract class InspectorPanel extends CollapsePanel {

    public InspectorPanel(Side side) {
        super(side);
    }

    protected static JComponent[] createRow(String label, String value) {
        JLabel vLabel = new JLabel(value);
        vLabel.setHorizontalAlignment(SwingConstants.LEFT);
        vLabel.setVerticalAlignment(SwingConstants.TOP);
        return IncidentInspectorPanel.createRow(new JLabel(label), vLabel);
    }

    protected static JComponent[] createRow(String label, JComponent value) {
        return IncidentInspectorPanel.createRow(new JLabel(label), value);
    }

    protected static JComponent[] createRow(JLabel label, JComponent value) {
        label.setHorizontalAlignment(SwingConstants.LEFT);
        label.setVerticalAlignment(SwingConstants.TOP);
        label.setFont(new Font("Dialog", Font.BOLD, 12));
        return new JComponent[]{label, value};
    }

    protected static JPanel createRow(JComponent[] row) {
        JPanel rowP = new JPanel(new BorderLayout(3, 3));
        rowP.setBorder(new EmptyBorder(0, 0, 5, 0));
        rowP.add(row[0], BorderLayout.NORTH);
        rowP.add(row[1], BorderLayout.CENTER);
        return rowP;
    }

    protected JList<Agent> createClickableAgentList(final int nClicks) {
        final JList<Agent> agentList = new JList<>();
        agentList.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                Agent a = (Agent) value;
                return super.getListCellRendererComponent(list, a.getName() + " (" + (int) a.getAge() + ")", index, isSelected, cellHasFocus);
            }
        });
        agentList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() != nClicks) {
                    return;
                }
                Agent agent = agentList.getSelectedValue();
                notifyAgentSelected(agent);
            }
        });
        agentList.setBorder(new LineBorder(SystemColor.controlDkShadow, 1));
        return agentList;
    }

    protected abstract void notifyAgentSelected(Agent agent);

    protected abstract void notifyAreaSelected(nl.tno.idsa.framework.world.Area area);

    protected JList<PlannedIncident> createClickableIncidentList() {
        final JList<PlannedIncident> incidentList = new JList<PlannedIncident>();
        incidentList.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                PlannedIncident plannedIncident = (PlannedIncident) value;
                Time time = new Time(plannedIncident.getIncident().getEnablingAction().getLocationVariable().getValue().getTimeNanos());
                String descr = String.format("[%s] %s", time, plannedIncident.getIncident().getName());
                return super.getListCellRendererComponent(list, descr, index, isSelected, cellHasFocus);
            }
        });
        incidentList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() != 1) {
                    return;
                }
                PlannedIncident incident = incidentList.getSelectedValue();
                notifyIncidentSelected(incident);
            }
        });
        incidentList.setBorder(new LineBorder(SystemColor.controlDkShadow, 1));
        return incidentList;
    }

    protected abstract void notifyIncidentSelected(PlannedIncident incident);
}
