package nl.tno.idsa.viewer.inspectors;

import nl.tno.idsa.framework.agents.Agent;
import nl.tno.idsa.framework.behavior.incidents.Incident;
import nl.tno.idsa.framework.semantics_base.objects.ParameterId;
import nl.tno.idsa.framework.semantics_impl.locations.LocationFunction;
import nl.tno.idsa.framework.world.Environment;
import nl.tno.idsa.viewer.components.SimpleGridBagPanel;
import nl.tno.idsa.viewer.observers.SelectionObserver;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.Observable;
import java.util.Observer;
import java.util.Stack;
import java.util.Vector;

/**
 * Created by jongsd on 23-9-15.
 */
// TODO Document class.
public class AreaInspectorPanel extends InspectorPanel implements Observer {

    private final SelectionObserver selectionObserver;
    private Environment environment;
    private nl.tno.idsa.framework.world.Area area;

    private Stack<nl.tno.idsa.framework.world.Area> previousAreas = new Stack<>();
    private JButton btnPreviousArea;

    private JLabel areaType;
    private JList<LocationFunction> locationFunctionList;
    private JList<Agent> agentsInAreaList;

    public AreaInspectorPanel(SelectionObserver selectionObserver, Environment environment) {
        super(Side.RIGHT);
        this.environment = environment;
        setBorder(null);

        this.selectionObserver = selectionObserver;
        selectionObserver.addObserver(this);

        createSubComponents();
        setArea(null);
    }

    @Override
    protected void expand() {
        super.expand();
        if (btnPreviousArea != null) {
            btnPreviousArea.setVisible(true);
        }
    }

    @Override
    protected void collapse() {
        super.collapse();
        if (btnPreviousArea != null) {
            btnPreviousArea.setVisible(false);
        }
    }

    @Override
    protected void notifyAgentSelected(Agent agent) {
        selectionObserver.setAgent(agent);
    }

    @Override
    protected void notifyEventSelected(Incident incident) {
        // Not happening.
    }

    @Override
    protected void notifyAreaSelected(nl.tno.idsa.framework.world.Area area) {
        selectionObserver.setArea(area);
        setArea(area);
    }

    public void setArea(final nl.tno.idsa.framework.world.Area area) {
        if (this.area != null) {
            previousAreas.push(this.area);
        }
        this.area = area;

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                updateSubComponents();
                if (area != null) {
                    expand();
                } else {
                    collapse();
                }
            }
        });
    }

    private void createSubComponents() {

        btnPreviousArea = new JButton(new AbstractAction("< Back") {
            @Override
            public void actionPerformed(ActionEvent e) {
                setArea(previousAreas.pop());
            }
        });
        btnPreviousArea.setFocusPainted(false);
        getToolbar().add(btnPreviousArea);

        JPanel main = new SimpleGridBagPanel(SimpleGridBagPanel.GRID_ROWS);
        getMainPanel().add(main, BorderLayout.NORTH);

        JComponent[] nameRow = createRow("Area type", "");
        areaType = (JLabel) nameRow[1];
        main.add(createRow(nameRow));

        // TODO This list could look a lot cleaner.
        locationFunctionList = new JList<>();
        locationFunctionList.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList list, Object value,
                                                          int index, boolean isSelected, boolean cellHasFocus) {
                StringBuilder s = new StringBuilder();
                LocationFunction locationFunction = (LocationFunction) value;
                s.append(locationFunction.getClass().getSimpleName()).append(" [");
                for (ParameterId parameterId : locationFunction.getParameters().keySet()) {
                    s.append(parameterId).append(": ").append(locationFunction.getParameters().get(parameterId).getValue()).append(", ");
                }
                if (s.charAt(s.length() - 1) == ' ') {
                    s.delete(s.length() - 2, s.length());
                }
                s.append("]");
                return super.getListCellRendererComponent(list, s.toString(), index, isSelected,
                        cellHasFocus);
            }
        });
        locationFunctionList.setBorder(new LineBorder(SystemColor.controlDkShadow, 1));
        JComponent[] lflRow = createRow("Location functions", locationFunctionList);
        main.add(createRow(lflRow));

        agentsInAreaList = createClickableAgentList(2);
        JComponent[] agentsInAreaListRow = createRow("Agents in area", agentsInAreaList);
        main.add(createRow(agentsInAreaListRow));
    }

    private void updateSubComponents() {

        btnPreviousArea.setEnabled(previousAreas.size() > 0);

        if (this.area == null) {
            areaType.setText("Ctlr+Alt+Click an area to see details.");
        } else {
            areaType.setText(area.getType());
        }

        if (area != null) {
            java.util.Vector<Agent> agents = environment.getAgentsIn(area);
            agentsInAreaList.setListData(agents);
        } else {
            agentsInAreaList.setListData(new Agent[]{});
        }

        if (this.area != null && area.getFunctions() != null) {
            locationFunctionList.setListData(new Vector<LocationFunction>(area.getFunctions()));
        } else {
            locationFunctionList.setListData(new LocationFunction[]{});
        }
    }

    @Override
    public void update(Observable o, Object arg) {
        if (o instanceof SelectionObserver) {
            if (arg instanceof nl.tno.idsa.framework.world.Area) {
                notifyAreaSelected((nl.tno.idsa.framework.world.Area) arg);
            }
        }
    }
}
