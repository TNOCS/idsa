package nl.tno.idsa.viewer.inspectors;

import nl.tno.idsa.framework.agents.Agent;
import nl.tno.idsa.framework.behavior.incidents.PlannedIncident;
import nl.tno.idsa.framework.behavior.plans.ActionPlan;
import nl.tno.idsa.framework.semantics_impl.actions.Action;
import nl.tno.idsa.framework.world.Time;
import nl.tno.idsa.viewer.components.SimpleGridBagPanel;
import nl.tno.idsa.viewer.observers.RunningIncidentsObserver;
import nl.tno.idsa.viewer.observers.SelectionObserver;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.util.*;
import java.util.List;

/**
 * Created by jongsd on 27-10-15.
 */
// TODO Document class.
public class IncidentInspectorPanel extends InspectorPanel implements Observer {

    private final RunningIncidentsObserver runningIncidentsObserver;
    private final SelectionObserver selectionObserver;

    private JList<PlannedIncident> incidentList;
    private JList<Agent> agentList;
    private JList<String> actionList;

    private List<PlannedIncident> incidents;
    private Vector<Agent> incidentAgents = new Vector<>();

    public IncidentInspectorPanel(RunningIncidentsObserver runningIncidentsObserver, SelectionObserver selectionObserver) {
        super(Side.LEFT);

        this.runningIncidentsObserver = runningIncidentsObserver;
        this.runningIncidentsObserver.addObserver(this);

        this.selectionObserver = selectionObserver;
        this.selectionObserver.addObserver(this);

        createSubComponents();
        setIncidents(null);
    }

    private void createSubComponents() {
        JPanel main = new SimpleGridBagPanel(SimpleGridBagPanel.GRID_ROWS);
        incidentList = createClickableIncidentList();
        JComponent[] eventListRow = createRow("Active incidents", incidentList);
        main.add(createRow(eventListRow));
        agentList = createClickableAgentList(1);
        agentList.setListData(incidentAgents);
        JComponent[] agentListRow = createRow("Agents involved in selected incident", agentList);
        main.add(createRow(agentListRow));
        getMainPanel().add(main, BorderLayout.NORTH);
        actionList = new JList<>();
        actionList.setBorder(new LineBorder(SystemColor.controlDkShadow, 1));
        JComponent[] actionListRow = createRow("Actions of selected agent", actionList);
        main.add(createRow(actionListRow));
    }

    private void updateSubComponents() {
        if (incidents == null || incidents.size() == 0) {
            incidentList.setListData(new PlannedIncident[]{});
            notifyIncidentSelected(null);
            collapse();
        } else {
            incidentList.setListData(new Vector<>(incidents));
            notifyIncidentSelected(incidents.get(0));
            expand();
        }
    }

    @Override
    protected void notifyIncidentSelected(PlannedIncident incident) {
        incidentList.setSelectedValue(incident, true);
        selectionObserver.setIncident(incident);
        if (incident == null) {
            incidentAgents.clear();
            agentList.clearSelection();
            notifyAgentSelected(null);
        } else {
            Set<Agent> agentsInvolved = incident.getActionPlan().getAgentsInvolved();
            incidentAgents.clear();
            incidentAgents.addAll(agentsInvolved);
            agentList.setListData(incidentAgents);
            notifyAgentSelected(incidentAgents.get(0));
        }
    }

    @Override
    protected void notifyAgentSelected(Agent agent) {
        Agent oldSelectedValue = agentList.getSelectedIndex() != -1 ? agentList.getSelectedValue() : null;
        agentList.setSelectedValue(agent, true); // This works; if the agent is not currently shown, we deselect.
        if (agentList.getSelectedValue() == null) { // We don't know this agent. Restore old selection.
            agentList.setSelectedValue(oldSelectedValue, true);
        }
        selectionObserver.setAgent(agent);
        if (agent == null) {
            actionList.setListData(new String[]{});
        } else {
            PlannedIncident selectedIncident = incidentList.getSelectedValue();
            if (selectedIncident != null) {
                ActionPlan actionPlan = selectedIncident.getActionPlan();
                List<Action> actionSequence = actionPlan.getActionSequence(agent);
                if (actionSequence != null) {
                    String[] actionDescr = new String[actionSequence.size()];
                    for (int i = 0; i < actionSequence.size(); i++) {
                        Action action = actionSequence.get(i);
                        actionDescr[i] = String.format("[%s] %s", new Time(action.getLocationVariable().getValue().getTimeNanos()), action.getVerb());
                    }
                    actionList.setListData(actionDescr);
                } else {
                    actionList.setListData(new String[]{});
                }
            } else {
                actionList.setListData(new String[]{});
            }
        }
    }

    @Override
    protected void notifyAreaSelected(nl.tno.idsa.framework.world.Area area) {
        // DO NOTHING
    }

    private void setIncidents(List<PlannedIncident> incidents) {
        this.incidents = incidents;
        updateSubComponents();
    }

    @Override
    @SuppressWarnings("unchecked")
    public void update(Observable o, Object arg) {
        if (o instanceof SelectionObserver) {
            if (arg instanceof PlannedIncident) {
                List<PlannedIncident> incidents = new ArrayList<>(1);
                incidents.add((PlannedIncident) arg);
                setIncidents(incidents);
            }
        } else if (o instanceof RunningIncidentsObserver) {
            if (arg instanceof List) {
                List list = (List) arg;
                if ((list.size() > 0 && list.get(0) instanceof PlannedIncident)) {
                    setIncidents((List<PlannedIncident>) list); // Unchecked cast due to the wonderful world of Java and generics.
                } else {
                    setIncidents(null);
                }
            }
        }
    }
}
