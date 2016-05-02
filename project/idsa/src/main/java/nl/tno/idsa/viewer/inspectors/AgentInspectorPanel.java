package nl.tno.idsa.viewer.inspectors;

import nl.tno.idsa.framework.agents.Agent;
import nl.tno.idsa.framework.behavior.activities.concrete.Activity;
import nl.tno.idsa.framework.behavior.incidents.Incident;
import nl.tno.idsa.framework.utils.TextUtils;
import nl.tno.idsa.viewer.SelectionObserver;
import nl.tno.idsa.viewer.components.SimpleGridBagPanel;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.Observable;
import java.util.Observer;
import java.util.Stack;
import java.util.Vector;

/**
 * Inspect agents graphically.
 */
public class AgentInspectorPanel extends InspectorPanel implements Observer {

    private final SelectionObserver selectionObserver;
    private Agent agent;

    private Stack<Agent> previousAgents = new Stack<>();
    private JButton btnPreviousAgent;

    private JLabel nameLabel;
    private JLabel genderLabel;
    private JLabel ageLabel;
    private JLabel locationLabel;
    private JLabel roleLabel;
    private JLabel houseLabel;
    private JLabel householdRoleLabel;
    private JLabel householdTypeLabel;
    private JList<String> agendaItemList;
    private JList<Agent> householdMemberList;
    private JList<Agent> contactMemberList;

    public AgentInspectorPanel(SelectionObserver selectionObserver) {
        super(Side.RIGHT);
        setBorder(null);

        this.selectionObserver = selectionObserver;
        selectionObserver.addObserver(this);

        createSubComponents();
        setAgent(null);
    }

    @Override
    protected void expand() {
        super.expand();
        if (btnPreviousAgent != null) {
            btnPreviousAgent.setVisible(true);
        }
    }

    @Override
    protected void collapse() {
        super.collapse();
        if (btnPreviousAgent != null) {
            btnPreviousAgent.setVisible(false);
        }
    }

    @Override
    protected void notifyAgentSelected(Agent agent) {
        selectionObserver.setAgent(agent);
        setAgent(agent);
    }

    @Override
    protected void notifyIncidentSelected(Incident incident) {
        // Not happening.
    }

    @Override
    protected void notifyAreaSelected(nl.tno.idsa.framework.world.Area area) {
        // Nothing.
    }

    public void setAgent(final Agent agent) {
        if (this.agent != null) {
            previousAgents.push(this.agent);
        }
        this.agent = agent;

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                updateSubComponents();
                if (agent != null) {
                    expand();
                } else {
                    collapse();
                }
            }
        });
    }

    private void createSubComponents() {

        btnPreviousAgent = new JButton(new AbstractAction("< Back") {
            @Override
            public void actionPerformed(ActionEvent e) {
                setAgent(previousAgents.pop());
            }
        });
        btnPreviousAgent.setFocusPainted(false);
        getToolbar().add(btnPreviousAgent);

        JPanel main = new SimpleGridBagPanel(SimpleGridBagPanel.Orientation.ROWS);
        getMainPanel().add(main, BorderLayout.NORTH);

        JComponent[] nameRow = createRow("Name", "");
        nameLabel = (JLabel) nameRow[1];
        main.add(createRow(nameRow));

        JComponent[] genderRow = createRow("Gender", "");
        genderLabel = (JLabel) genderRow[1];
        main.add(createRow(genderRow));

        JComponent[] ageRow = createRow("Age", "");
        ageLabel = (JLabel) ageRow[1];
        main.add(createRow(ageRow));

        main.add(new JLabel(" "));

        JComponent[] locationRow = createRow("Location", "");
        locationLabel = (JLabel) locationRow[1];
        main.add(createRow(locationRow));

        main.add(new JLabel(" "));

        JComponent[] roleRow = createRow("Role", "");
        roleLabel = (JLabel) roleRow[1];
        main.add(createRow(roleRow));

        main.add(new JLabel(" "));

        JComponent[] houseRow = createRow("Lives at", "");
        houseLabel = (JLabel) houseRow[1];
        main.add(createRow(houseRow));

        householdMemberList = createClickableAgentList(2);
        JComponent[] householdMemberListRow = createRow("Household members", householdMemberList);
        main.add(createRow(householdMemberListRow));

        JComponent[] householdRoleRow = createRow("Role in household", "");
        householdRoleLabel = (JLabel) householdRoleRow[1];
        main.add(createRow(householdRoleRow));

        JComponent[] householdTypeRow = createRow("Type of household", "");
        householdTypeLabel = (JLabel) householdTypeRow[1];
        main.add(createRow(householdTypeRow));

        main.add(new JLabel(" "));

        agendaItemList = new JList<>();
        agendaItemList.setBorder(new LineBorder(SystemColor.controlDkShadow, 1));
        JComponent[] agendaRow = createRow("Agenda", agendaItemList);
        main.add(createRow(agendaRow));

        main.add(new JLabel(" "));

        contactMemberList = createClickableAgentList(2);
        JComponent[] contactListRow = createRow("Contacts", contactMemberList);
        main.add(createRow(contactListRow));
    }

    private void updateSubComponents() {

        btnPreviousAgent.setEnabled(previousAgents.size() > 0);

        if (this.agent == null) {
            nameLabel.setText("Alt+Click an agent to see details.");
        } else {
            nameLabel.setText(agent.getName());
        }

        genderLabel.setText(agent != null ? agent.getGender() + "" : "?");
        ageLabel.setText(agent != null ? (int) agent.getAge() + "" : "?");

        String locationStr = agent != null && agent.getLocation() != null ? agent.getLocation().toString() : "?";
        locationLabel.setText(locationStr);

        String roleStr = agent != null && agent.getRole() != null ? agent.getRole().getSimpleName() : "?";
        roleLabel.setText(roleStr);

        String houseStr = agent != null && agent.getHouse() != null ? agent.getHouse().toString() : "?";
        houseLabel.setText(houseStr);

        if (agent != null && agent.getHousehold() != null) {
            householdMemberList.setListData(agent.getHousehold().toArray(new Agent[]{}));
        } else {
            householdMemberList.setListData(new Agent[]{});
        }

        householdRoleLabel.setText(agent != null ? (agent.getHouseholdRole() != null ? agent.getHouseholdRole().toString() : "?") : "?");
        householdTypeLabel.setText(agent != null ? (agent.getHouseholdType() != null ? agent.getHouseholdType().toString() : "?") : "?");

        if (agent != null && agent.getContacts() != null) {
            contactMemberList.setListData(agent.getContacts().toArray(new Agent[]{}));
        } else {
            contactMemberList.setListData(new Agent[]{});
        }

        if (agent != null && agent.getAgenda() != null) {
            Vector<String> agendaItems = new Vector<>(agent.getAgenda().size());
            for (Activity a : agent.getAgenda()) {
                agendaItems.add(TextUtils.camelCaseToText(a.getName()) + " " + a.getStartTime() + "-" + a.getEndTime()); // TODO Perhaps also include the locations.
            }
            agendaItemList.setListData(agendaItems);
        } else {
            agendaItemList.setListData(new String[]{});
        }
    }

    @Override
    public void update(Observable o, Object arg) {
        if (o instanceof SelectionObserver) {
            if (arg instanceof java.util.List) {
                java.util.List list = (java.util.List) arg;
                if ((list.size() > 0 && list.get(0) instanceof Agent)) {
                    notifyAgentSelected((Agent) list.get(0));
                } else {
                    notifyAgentSelected(null);
                }
            }
        }
    }
}
