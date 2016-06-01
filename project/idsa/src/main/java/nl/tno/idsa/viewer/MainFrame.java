package nl.tno.idsa.viewer;

import nl.tno.idsa.Constants;
import nl.tno.idsa.framework.agents.Agent;
import nl.tno.idsa.framework.behavior.incidents.Incident;
import nl.tno.idsa.framework.behavior.incidents.PlannedIncident;
import nl.tno.idsa.framework.behavior.planners.IncidentPlanner;
import nl.tno.idsa.framework.behavior.plans.ActionPlan;
import nl.tno.idsa.framework.behavior.triggers.StaticAreaTrigger;
import nl.tno.idsa.framework.messaging.Messenger;
import nl.tno.idsa.framework.messaging.ProgressNotifier;
import nl.tno.idsa.framework.semantics_impl.actions.Action;
import nl.tno.idsa.framework.semantics_impl.groups.Group;
import nl.tno.idsa.framework.semantics_impl.locations.LocationAndTime;
import nl.tno.idsa.framework.semantics_impl.variables.GroupVariable;
import nl.tno.idsa.framework.semantics_impl.variables.LocationVariable;
import nl.tno.idsa.framework.semantics_impl.variables.Variable;
import nl.tno.idsa.framework.simulator.ISimulatedObject;
import nl.tno.idsa.framework.simulator.Sim;
import nl.tno.idsa.framework.utils.TextUtils;
import nl.tno.idsa.framework.world.*;
import nl.tno.idsa.framework.world.Point;
import nl.tno.idsa.library.models.BasicMovementModel;
import nl.tno.idsa.viewer.components.ProgressDialog;
import nl.tno.idsa.viewer.dialogs.MultiplierSettingDialog;
import nl.tno.idsa.viewer.dialogs.TimeSettingDialog;
import nl.tno.idsa.viewer.incidentsettings.IncidentParameterDialog;
import nl.tno.idsa.viewer.incidentsettings.IncidentSelectorDialog;
import nl.tno.idsa.viewer.inspectors.AgentInspectorPanel;
import nl.tno.idsa.viewer.inspectors.AreaInspectorPanel;
import nl.tno.idsa.viewer.inspectors.IncidentInspectorPanel;
import nl.tno.idsa.viewer.observers.RunningIncidentsObserver;
import nl.tno.idsa.viewer.observers.SelectionObserver;
import nl.tno.idsa.viewer.utils.AgentColorUtil;
import nl.tno.idsa.viewer.utils.AreaColorUtil;
import org.piccolo2d.PCanvas;
import org.piccolo2d.PLayer;
import org.piccolo2d.PNode;
import org.piccolo2d.activities.PActivity;
import org.piccolo2d.event.PBasicInputEventHandler;
import org.piccolo2d.event.PInputEvent;
import org.piccolo2d.event.PMouseWheelZoomEventHandler;
import org.piccolo2d.nodes.PArea;
import org.piccolo2d.nodes.PPath;
import org.piccolo2d.nodes.PText;
import org.piccolo2d.util.PBounds;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.util.*;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author smelikrm
 */
// TODO Document class.
public class MainFrame implements IEnvironmentObserver, Observer {

    static {
        try {
            javax.swing.UIManager.setLookAndFeel(javax.swing.UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
        }
    }

    private final Sim sim;

    private final javax.swing.JFrame mapFrame;
    private final javax.swing.JPanel contentPane;

    private final SelectionObserver selectionObserver;
    private final RunningIncidentsObserver runningIncidents;

    private final PCanvas canvas;
    private final PLayer uiLayer;
    private final PLayer edgeLayer;
    private final PLayer areaLayer;
    private final PLayer agentLayer;
    private javax.swing.JLabel timeLabel;
    private javax.swing.JLabel positionLabel;

    private static final String NODE_TYPE_AGENT = "agent";
    private static final String NODE_TYPE_STATIC_TRIGGER = "static_trigger";

    private enum Mode {INSPECT_AGENT, TEST_ROUTE, INSPECT_AREA, PLACE_EVENT}
    private Mode mode;

    public MainFrame(Sim sim) {

        this.sim = sim;

        // Shared agent/incident selection
        this.selectionObserver = new SelectionObserver();
        this.runningIncidents = new RunningIncidentsObserver();

        sim.setPause(true);

        this.mapFrame = new javax.swing.JFrame("Intent Driven Scenario Authoring");
        this.contentPane = new javax.swing.JPanel(new BorderLayout());

        mapFrame.setContentPane(contentPane);

        this.canvas = new PCanvas();
        // Create layers
        this.areaLayer = new PLayer();
        this.edgeLayer = new PLayer();
        this.uiLayer = new PLayer();
        this.agentLayer = new PLayer();
        canvas.getRoot().addChild(areaLayer);
        canvas.getRoot().addChild(edgeLayer);
        canvas.getRoot().addChild(uiLayer);
        canvas.getRoot().addChild(agentLayer);
        canvas.getCamera().addLayer(0, edgeLayer);
        canvas.getCamera().addLayer(1, areaLayer);
        canvas.getCamera().addLayer(2, uiLayer);
        canvas.getCamera().addLayer(3, agentLayer);

        initControlPanel();
        initMap(sim.getEnvironment().getWorld());
        initAgents(sim.getEnvironment().getAgents());
        initInspectors();

        ProgressDialog progressDialog = new ProgressDialog(mapFrame);
        ProgressNotifier.addObserver(progressDialog);

        mapFrame.setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        mapFrame.setSize(screenSize.width - 100, screenSize.height - 100);
        mapFrame.setLocationRelativeTo(null);
        mapFrame.setVisible(true);
        mapFrame.setExtendedState(java.awt.Frame.MAXIMIZED_BOTH);
        // Register observers
        sim.getEnvironment().addObserver(this);
        this.selectionObserver.addObserver(this);
    }

    private void initControlPanel() {

        javax.swing.JPanel controlPanel = new javax.swing.JPanel(new GridLayout(1, 0, 3, 3));

        final JButton setTimeButton = new JButton(new AbstractAction("Time...") {
            @Override
            public void actionPerformed(ActionEvent e) {
                // The multiplier selector also asks for time, but changing ONLY the time does not require recomputing the agendas.
                TimeSettingDialog timeSettingDialog = new TimeSettingDialog(mapFrame, sim.getEnvironment());   // The dialog decides whether it must be visible.
            }
        });
        setTimeButton.setFocusPainted(false);
        //setTimeButton.setEnabled(false);

        final JButton setSeasonButton = new JButton(new AbstractAction("Day/Season...") {
            @Override
            public void actionPerformed(ActionEvent e) {
                MultiplierSettingDialog multiplierSettingDialog = new MultiplierSettingDialog(mapFrame, sim.getEnvironment());  // The dialog decides whether it must be visible.
                timeChanged(multiplierSettingDialog.getSelectedTime());
            }
        });
        setSeasonButton.setFocusPainted(false);
        //setSeasonButton.setEnabled(false);

        javax.swing.JToggleButton playPauseButton = new javax.swing.JToggleButton(new AbstractAction("Play") {
            @Override
            public void actionPerformed(ActionEvent e) {
                sim.togglePause();
                setTimeButton.setEnabled(setTimeButton.isEnabled());
                setSeasonButton.setEnabled(setSeasonButton.isEnabled());
            }
        });
        playPauseButton.setFocusPainted(false);
        controlPanel.add(playPauseButton);
        controlPanel.add(setTimeButton);
        controlPanel.add(setSeasonButton);

        ButtonGroup bg = new ButtonGroup() {
            @Override
            public void setSelected(ButtonModel model, boolean selected) {
                if (selected) {
                    super.setSelected(model, true);
                } else {
                    clearSelection();
                }
            }
        };

        final JToggleButton inspectAgentButton = new JToggleButton(new AbstractAction("Inspect agent") {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (((JToggleButton) e.getSource()).isSelected()) {
                    mode = Mode.INSPECT_AGENT;
                } else {
                    mode = null;
                }
            }
        });
        inspectAgentButton.setFocusPainted(false);
        controlPanel.add(inspectAgentButton);
        bg.add(inspectAgentButton);
        final JToggleButton inspectAreaButton = new JToggleButton(new AbstractAction("Inspect area") {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (((JToggleButton) e.getSource()).isSelected()) {
                    mode = Mode.INSPECT_AREA;
                } else {
                    mode = null;
                }
            }
        });
        inspectAreaButton.setFocusPainted(false);
        controlPanel.add(inspectAreaButton);
        bg.add(inspectAreaButton);
        final JToggleButton createRouteButton = new JToggleButton(new AbstractAction("Create route") {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (((JToggleButton) e.getSource()).isSelected()) {
                    mode = Mode.TEST_ROUTE;
                } else {
                    mode = null;
                }
            }
        });
        createRouteButton.setFocusPainted(false);
        controlPanel.add(createRouteButton);
        bg.add(createRouteButton);
        final JToggleButton createIncidentButton = new JToggleButton(new AbstractAction("Inject incident") {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (((JToggleButton) e.getSource()).isSelected()) {
                    mode = Mode.PLACE_EVENT;
                } else {
                    mode = null;
                }
            }
        });
        createIncidentButton.setFocusPainted(false);
        controlPanel.add(createIncidentButton);
        bg.add(createIncidentButton);

        javax.swing.JPanel controlPanelP = new javax.swing.JPanel(new BorderLayout(3, 3));
        controlPanelP.add(controlPanel, BorderLayout.WEST);
        controlPanelP.setBorder(new CompoundBorder(new LineBorder(SystemColor.controlDkShadow, 1), new EmptyBorder(5, 5, 5, 5)));

        timeLabel = new javax.swing.JLabel("(Sim time)");
        timeLabel.setHorizontalAlignment(SwingConstants.CENTER);
        controlPanelP.add(timeLabel, BorderLayout.CENTER);

        positionLabel = new javax.swing.JLabel();
        controlPanelP.add(positionLabel, BorderLayout.EAST);

        contentPane.add(controlPanelP, BorderLayout.NORTH);
    }

    private void initInspectors() {

        JSplitPane inspectorPanel = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        inspectorPanel.setDividerLocation((int) (0.75 * Toolkit.getDefaultToolkit().getScreenSize().getHeight() - 100));
        contentPane.add(inspectorPanel, BorderLayout.EAST);

        AgentInspectorPanel agentInspector = new AgentInspectorPanel(this.selectionObserver);
        inspectorPanel.add(agentInspector);

        AreaInspectorPanel areaInspector = new AreaInspectorPanel(selectionObserver, sim.getEnvironment());
        inspectorPanel.add(areaInspector);

        IncidentInspectorPanel eventInspector = new IncidentInspectorPanel(this.runningIncidents, this.selectionObserver);
        contentPane.add(eventInspector, BorderLayout.WEST);
    }

    private void initMap(World world) {

        // Remove default zoom event handler
        canvas.removeInputEventListener(canvas.getZoomEventHandler());
        final PMouseWheelZoomEventHandler mouseWheelZoomEventHandler = new PMouseWheelZoomEventHandler();
        canvas.addInputEventListener(mouseWheelZoomEventHandler);
        canvas.addInputEventListener(new MapClickHandler());
        canvas.addInputEventListener(new RouteSelectionHandler());
        canvas.addInputEventListener(new HoverEventHandler());

        for (Area a : world.getAreas()) {
            PArea area = getDrawableFilledArea(a);
            areaLayer.addChild(area);
        }
        for (Edge e : world.getConnections()) {
            Vertex src = e.getSource();
            Vertex tgt = e.getTarget();
            PPath p = PPath.createLine(src.getX(), -src.getY(), tgt.getX(), -tgt.getY());
            p.setPaint(null);
            p.setStrokePaint(Color.LIGHT_GRAY);
            p.setStroke(new BasicStroke(1.5f));
            edgeLayer.addChild(p);
        }
        contentPane.add(canvas, BorderLayout.CENTER);
    }

    private PArea getDrawableFilledArea(Area a) {
        Path2D.Double path = new Path2D.Double();
        path.moveTo(a.get(0).getX(), -a.get(0).getY());
        for (int i = 1; i < a.numberOfVertices(); ++i) {
            path.lineTo(a.get(i).getX(), -a.get(i).getY());
        }
        path.closePath();
        PArea area = new PArea(path);
        area.setPaint(AreaColorUtil.getAreaColor(a));
        area.setStrokePaint(Color.DARK_GRAY);
        area.setStroke(new BasicStroke(0.5f));
        return area;
    }

    public void show() {
        mapFrame.setVisible(true);
        // Display map in full
        canvas.getCamera().animateViewToCenterBounds(agentLayer.getFullBounds(), true, 0);
    }

    private void addIncident(Point location) throws Exception {

        // Pause simulation
        sim.setPause(true);

        // Make sure sim is pause (all updates are complete and agents are standing still)
        while (!sim.isPaused()) {
            Thread.sleep(100);
        }

        // Create incident dialog.
        Environment environment = sim.getEnvironment();
        IncidentSelectorDialog incidentSelectorDialog = new IncidentSelectorDialog(mapFrame, environment.getWorld());
        incidentSelectorDialog.setVisible(true);
        Incident incident = incidentSelectorDialog.getSelectedIncident();
        if (incident == null) {
            sim.setPause(false);
            return;
        }

        // Create event parameters.
        LocationVariable locationVariable = (LocationVariable) incident.getParameters().get(Incident.Parameters.LOCATION_VARIABLE);
        locationVariable.setValue(new LocationAndTime(location));
        IncidentParameterDialog incidentParameterDialog = new IncidentParameterDialog(mapFrame, incident, environment);
        incidentParameterDialog.setVisible(true);
        incident = incidentParameterDialog.getSelectedIncident();
        if (incident == null) { //todo: this does not handle a cancelled incident correctly (nullpointer exception when closing the dialog).
            sim.setPause(false);
            return;
        }

        // Create a plan.
        ProgressNotifier.notifyShowProgress(true);
        ProgressNotifier.notifyUnknownProgress();
        ProgressNotifier.notifyProgressMessage("Planning incident...");

        long desiredEndTime = incident.getEnablingAction().getLocationVariable().getValue().getTimeNanos();
        PlannedIncident plannedIncident = IncidentPlanner.plan(sim.getEnvironment(), incident);
        ActionPlan plan = plannedIncident.getActionPlan();

        ProgressNotifier.notifyShowProgress(false);

        // Incident can happen precisely on time.
        if (plannedIncident.getStatus() == PlannedIncident.Status.INSTANTIATED_WITHIN_TIME_CONSTRAINTS) {
            plan.startModels(sim.getEnvironment());
            notifyIncidentStarted(plannedIncident, plan);
        }

        // Incident can happen, but later than desired.
        else if (plannedIncident.getStatus() == PlannedIncident.Status.INSTANTIATED_WITHOUT_TIME_CONSTRAINTS) {
            long achievedEndTime = plan.getGoalAction().getLocationVariable().getValue().getTimeNanos();
            int choice = javax.swing.JOptionPane.showConfirmDialog(
                    mapFrame,
                    String.format("It seems %s is too soon to be able to realize this incident.\nCan it occur at %s instead?", new Time(desiredEndTime), new Time(achievedEndTime)),
                    "Incident happens too soon", javax.swing.JOptionPane.YES_NO_OPTION, javax.swing.JOptionPane.QUESTION_MESSAGE);

            // The user wants it when s/he says it and not later...
            if (choice == javax.swing.JOptionPane.NO_OPTION) {
                javax.swing.JOptionPane.showMessageDialog(null, String.format("No valid plan found for %s.", incident));
            }

            // Otherwise, the plan will just execute according to what the sampler managed to achieve.
            else {
                // TODO This sometimes seems to yield plans with a negative time for certain steps.
                plan.startModels(environment);
                notifyIncidentStarted(plannedIncident, plan);
            }
        }

        // Incident cannot happen at all, due to some error.
        else {
            // TODO Somehow trace why the incident cannot be planned and tell the user.
            JOptionPane.showMessageDialog(mapFrame, "The incident could not be planned due to an unknown error.", "Error", JOptionPane.ERROR_MESSAGE);
        }

        // Resume simulation
        sim.setPause(false);
    }

    private void notifyIncidentStarted(final PlannedIncident incident, final ActionPlan plan) {

        runningIncidents.addIncident(incident);
        visualizePlan(plan);

        // Make sure the incident becomes deactivated. TODO Note that this only works if incidents always finish exactly when they have to.
        new Thread() {
            @Override
            public void run() {
                Environment environment = sim.getEnvironment();
                long achievedEndTime = plan.getGoalAction().getLocationVariable().getValue().getTimeNanos();
                while (environment.getTime().getNanos() < achievedEndTime) {
                    try {
                        Thread.sleep(500);
                    } catch (Exception e) {
                        e.printStackTrace();
                        break;
                    }
                }
                runningIncidents.removeIncident(incident);
            }
        }.start();
    }

    private void initAgents(List<Agent> agents) {
        for (Agent a : agents) {
            addRepresentation(a);
        }
        final PActivity a = new PActivity(-1, 200) { //Sim.getHz() * 2
            public void activityStep(final long currentTime) {
                super.activityStep(currentTime);
                updatePositions(currentTime);
            }
        };
        canvas.getRoot().addActivity(a);
    }

    private void addRepresentation(ISimulatedObject s) {
        if (s instanceof Agent) {
            PPath node = addCircle(agentLayer, s.getLocation(), Constants.GUI_AGENT_ICON_SIZE);
            node.addAttribute("type", NODE_TYPE_AGENT);
            node.addAttribute("agent", s);
            node.setPaint(Color.GRAY);
            node.setStrokePaint(Color.DARK_GRAY);
            node.setStroke(new BasicStroke(0.75f)); // TODO Increase/decrease size and stroke with zooming. Small agents are hardly visible.
            node.setVisible(true);//!((Agent) s).isInside());
        } else if (s instanceof StaticAreaTrigger) {
            StaticAreaTrigger staticAreaTrigger = (StaticAreaTrigger) s;
            IGeometry shape = staticAreaTrigger.getShape();
            if (shape instanceof PolyLine) {
                PPath[] line = addPath(edgeLayer, (PolyLine) shape);
                for (PPath node : line) {
                    setStaticTriggerNodeAttributes(staticAreaTrigger, node);
                }
//                for (Point p : shape.getPoints()) {
//                    PPath node = addCircle(agentLayer, p, staticAreaTrigger.getEffectiveDistanceFromShape());
//                    setStaticTriggerNodeAttributes(staticAreaTrigger, node);
//                }
            }
        }
        // TODO Also show dynamic area triggers.
    }

    private void setStaticTriggerNodeAttributes(StaticAreaTrigger staticAreaTrigger, PPath node) {
        node.addAttribute("type", NODE_TYPE_STATIC_TRIGGER);
        node.addAttribute("trigger", staticAreaTrigger);
        node.setStrokePaint(Color.BLUE);
        node.setStroke(new BasicStroke(1f));
    }

    private void removeRepresentation(ISimulatedObject s) {
        for (Object nodeObject : agentLayer.getAllNodes()) {
            if (nodeObject instanceof PPath) {
                PPath node = (PPath) nodeObject;
                if (node.getAttribute("type").equals(NODE_TYPE_AGENT)) {
                    Agent a = (Agent) node.getAttribute("agent");
                    if (s.equals(a)) {
                        removeNodeFromLayer(agentLayer, node);
                    }
                } else if (node.getAttribute("type").equals(NODE_TYPE_STATIC_TRIGGER)) {
                    StaticAreaTrigger st = (StaticAreaTrigger) node.getAttribute("trigger");
                    if (s.equals(st)) {
                        removeNodeFromLayer(edgeLayer, node);
                    }
                }
            }
        }
    }

    private void updatePositions(final long currentTime) {

        final Iterator i = this.agentLayer.getChildrenReference().iterator();
        while (i.hasNext()) {

            final PPath node = (PPath) i.next();  // TODO Can create a concurrent modification exception if children are added (e.g. area triggers).

            if (node.getAttribute("type").equals(NODE_TYPE_AGENT)) {
                Agent a = (Agent) node.getAttribute("agent");
                Color source = AgentColorUtil.getAgentColor(a);
                double newX = a.getLocation().getX();
                double newY = a.getLocation().getY();

                boolean moved = true;
                Point previousPosition = previousPositions.get(a);
                if (previousPosition != null) {
                    moved = !previousPosition.equals(a.getLocation());
                }

                if (moved) {
                    node.setVisible(!a.isInside());
                    // Administer position.
                    previousPositions.put(a, a.getLocation());

                    // Try to occupy a square meter that is free and as close as possible to the agent's real position.
                    if (Constants.GUI_DECLUTTER_AGENTS) {
                        int distance = 1;
                        while (getOccupation((int) newX, (int) newY) > 0 && distance < 10) {
                            for (double x = newX - distance; x < newX + distance; x++) {
                                if (getOccupation((int) x, (int) (newY - distance)) == 0) {
                                    newX = x;
                                    newY = newY - distance;
                                    break;
                                }
                                if (getOccupation((int) x, (int) (newY + distance)) == 0) {
                                    newX = x;
                                    newY = newY + distance;
                                    break;
                                }
                            }
                            for (double y = newY - distance; y < newY + distance; y++) {
                                if (getOccupation((int) (newX - distance), (int) y) == 0) {
                                    newX = newX - distance;
                                    newY = y;
                                    break;
                                }
                                if (getOccupation((int) (newX + distance), (int) y) == 0) {
                                    newX = newX + distance;
                                    newY = y;
                                    break;
                                }
                            }
                            distance++;
                        }
                    }
                }

                // Administer move in various ways.
                if (moved) {

                    // Update occupation grid.
                    increaseOccupation((int) newX, (int) newY);
                    decreaseOccupation((int) node.getX(), -1 * (int) node.getY());

                    // Update node.
                    node.setX(newX);
                    node.setY(-1 * newY);

                    // Make the agent yellow.
                    node.setPaint(new Color(source.getRed(), source.getGreen(), source.getBlue(), 255));
                    node.setStrokePaint(Color.YELLOW);
                    if (selectionObserver.containsAgent(a) && selectionObserver.getAgents().size() == 1) {
                        // Flashing:
                        double zeroToOne = (currentTime % 500) / 500.0;
                        Color destination = Color.WHITE;
                        double red = source.getRed() + zeroToOne * (destination.getRed() - source.getRed());
                        double green = source.getGreen() + zeroToOne * (destination.getGreen() - source.getGreen());
                        double blue = source.getBlue() + zeroToOne * (destination.getBlue() - source.getBlue());
                        node.setPaint(new Color(Math.min(255, (int) red), Math.min(255, (int) green), Math.min(255, (int) blue), 255));
                        final PBounds b = node.getGlobalFullBounds();
                        canvas.getCamera().globalToLocal(b);
                        canvas.getCamera().animateViewToCenterBounds(b, false, 0);
                    }
                }

                // Paint the agent normally.
                else {
                    node.setPaint(source);
                    node.setStrokePaint(Color.DARK_GRAY);
                }
            }

            // TODO Other node types?
        }
    }

    private int[][] occupationGrid = new int[1000][1000];
    private HashMap<Agent, Point> previousPositions = new HashMap<>();

    private int getOccupation(int x, int y) {
        ensureOccupationGridSize(x, y);
        return occupationGrid[x][y];
    }

    private void increaseOccupation(int x, int y) {
        ensureOccupationGridSize(x, y);
        occupationGrid[x][y]++;
    }

    private void decreaseOccupation(int x, int y) {
        ensureOccupationGridSize(x, y);
        if (occupationGrid[x][y] > 0) {
            occupationGrid[x][y]--;
        }
    }

    private void ensureOccupationGridSize(int x, int y) {
        if (occupationGrid.length <= x) {
            int[][] newOccupationGrid = new int[(int) (x * 1.2)][];
            System.arraycopy(occupationGrid, 0, newOccupationGrid, 0, occupationGrid.length);
            occupationGrid = newOccupationGrid;
        }
        if (occupationGrid[x] == null) {
            occupationGrid[x] = new int[(int) (y * 1.2)];
        } else if (occupationGrid[x].length <= y) {
            int[] newOccupationGridX = new int[(int) (y * 1.2)];
            occupationGrid[x] = newOccupationGridX;
        }
    }

    public void visualizePlan(ActionPlan plan) {
        Set<Agent> agentsInvolved = plan.getAgentsInvolved();
        for (Agent a : agentsInvolved) {
            visualizePlan(plan, a, false);
        }
        for (Variable v : plan.getVariables()) {
            if (v.getValue() != null) {
                if (v instanceof LocationVariable) {
                    LocationVariable lv = (LocationVariable) v;
                    if (lv.getValue() != null) {
                        LocationAndTime locTime = lv.getValue();
                        PPath node = addSquare(uiLayer, (Point) locTime.getLocation(), 12.0);
                        node.setPaint(Color.YELLOW);
                        if (lv.getAllowedFunctions() != null && !lv.getAllowedFunctions().isEmpty()) {
                            addText(uiLayer, (Point) lv.getValue().getLocation(), TextUtils.classNamesToSimpleString(lv.getAllowedFunctions()));
                        } else if (lv.isBoundTo(plan.getGoalAction().getLocationVariable())) {
                            addText(uiLayer, (Point) lv.getValue().getLocation(), "Target location");
                        }
                    }
                } else if (v instanceof GroupVariable) {
                    GroupVariable gv = (GroupVariable) v;
                    // To spawn?
                    if (gv.areAgentsProvided()) {
                        Group group = gv.getValue();
                        for (Agent a : group) {
//                            PPath node = addSquare(uiLayer, a.getLocation(), 10.0);
//                            node.setPaint(Color.RED);
//                            if (gv.getMemberRole() != null) {
//                                addText(uiLayer, (Point) a.getLocation(), gv.getMemberRole().getSimpleName());
//                            }
                        }
                    } else {
                        // Empty
                    }
                }
            }
        }
        zoomInOnSelection(); //todo: temp disable for TestSampler
    }

    public void visualizePlan(ActionPlan plan, Agent agent, boolean showLocations) {
        Color source = AgentColorUtil.getAgentColor(agent);
        GroupVariable theGroup = null;
        for (Variable v : plan.getVariables()) {
            if (v.getValue() != null) {
                if (v instanceof GroupVariable) {
                    GroupVariable gv = (GroupVariable) v;
                    Group group = gv.getValue();
                    if (group.contains(agent)) {
                        theGroup = gv;
                        break;
                    }
                }
            }
        }
        if (theGroup != null) {
            List<Action> actions = plan.getActionSequence(theGroup, true);
            for (int i = 0; i < actions.size(); ++i) {
                Action action = actions.get(i);
                LocationVariable lv = action.getLocationVariable();
                if (lv.getValue() != null) {
                    LocationAndTime locTime = lv.getValue();
                    // Path towards lv
                    Point lp = locTime.getLocation().getCenterPoint();
                    Path path = sim.getEnvironment().getWorld().getPath(agent.getLocation(), lp, false);
                    if (path != null && path.size() > 0) {
                        addNodeToLayer(uiLayer, getDrawablePath(path, source, 3.0f));
                    }
                    if (showLocations) {
                        PPath node = addSquare(uiLayer, (Point) locTime.getLocation(), 12.0);
                        node.setPaint(Color.YELLOW);
                        if (lv.getAllowedFunctions() != null && !lv.getAllowedFunctions().isEmpty()) {
                            addText(uiLayer, (Point) lv.getValue().getLocation(), TextUtils.classNamesToSimpleString(lv.getAllowedFunctions()));
                        } else if (lv.isBoundTo(plan.getGoalAction().getLocationVariable())) {
                            addText(uiLayer, (Point) lv.getValue().getLocation(), "Target location");
                        }
                    }
                }
            }
        }
    }

    private void zoomInOnSelection() {
        final double border = 50.0;
        PBounds bounds = uiLayer.getGlobalFullBounds();
        bounds.setSize(bounds.getWidth() + 2 * border, bounds.getHeight() + 2 * border);
        bounds.setOrigin(bounds.getMinX() - border, bounds.getMinY() - border);
        canvas.getCamera().animateViewToCenterBounds(bounds, true, 1000);
    }

    private static PPath getDrawablePath(Path path) {
        return getDrawablePath(path, Color.ORANGE);
    }

    private static PPath getDrawablePath(Path path, Color color) {
        return getDrawablePath(path, color, 1.0f);
    }

    private static PPath getDrawablePath(Path path, Color color, float width) {
        PPath result = new PPath.Double();
        result.setPaint(null);
        result.setStrokePaint(color);
        result.setStroke(new BasicStroke(width));
        Point src = path.get(0);
        result.moveTo(src.getX(), -src.getY());
        for (int i = 0; i < path.size(); ++i) {
            Point tgt = path.get(i);
            result.lineTo(tgt.getX(), -tgt.getY());
        }
        return result;
    }

    private static PText addText(PLayer layer, Point position, String txt) {
        final PText text = new PText(txt);
        text.setX(position.getX() + 8.0);
        text.setY(-position.getY() - 8.0);
        addNodeToLayer(layer, text);
        return text;
    }

    private static PPath addSquare(PLayer layer, Point position, double dim) {
        PPath node = PPath.createRectangle(0, 0, dim, dim);
        node.setX(position.getX());
        node.setY(-position.getY());
        node.setOffset(-0.5 * dim, -0.5 * dim);
        addNodeToLayer(layer, node);
        return node;
    }

    private static PPath addCircle(PLayer layer, Point position, double dim) {
        PPath node = PPath.createEllipse(0, 0, dim, dim);
        node.setX(position.getX());
        node.setY(-position.getY());
        node.setOffset(-0.5 * dim, -0.5 * dim);
        addNodeToLayer(layer, node);
        return node;
    }

    private static PPath[] addPath(PLayer layer, PolyLine path) {
        PPath[] line = new PPath[path.getPoints().length - 1];
        for (int i = 0; i < path.getPoints().length - 1; ++i) {
            line[i] = PPath.createLine(path.getPoints()[i].getX(), -path.getPoints()[i].getY(), path.getPoints()[i + 1].getX(), -path.getPoints()[i + 1].getY());
            addNodeToLayer(layer, line[i]);
        }
        return line;
    }

    private static void addNodeToLayer(final PLayer layer, final PNode node) {
        if (javax.swing.SwingUtilities.isEventDispatchThread()) {
            layer.addChild(node);
        } else {
            javax.swing.SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    layer.addChild(node);
                }
            });
        }
    }

    private static void removeNodeFromLayer(final PLayer layer, final PNode node) {
        if (javax.swing.SwingUtilities.isEventDispatchThread()) {
            layer.removeChild(node);
        } else {
            javax.swing.SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    layer.removeChild(node);
                }
            });
        }
    }

    @Override
    public void simulatedObjectAdded(ISimulatedObject newSimulatedObject) {
        addRepresentation(newSimulatedObject);
    }

    @Override
    public void simulatedObjectRemoved(ISimulatedObject simulatedObject) {
        removeRepresentation(simulatedObject);
    }

    long lastRefreshTimeMs = 0;

    @Override
    public void timeChanged(Time newTime) {
        long currentTimeMillis = System.currentTimeMillis();
        if (currentTimeMillis - lastRefreshTimeMs < 100) {
            return;
        }
        lastRefreshTimeMs = currentTimeMillis;
        if (timeLabel != null) {
            timeLabel.setText(String.format("%s %s [%.2f x real-time]", sim.getEnvironment().getDateString(), newTime.toString(), sim.getActualXRealTime()));
        }
    }

    @Override
    public void update(Observable o, Object arg) {
        clearSelectionLayer();
        if (arg != null) {
            if (arg instanceof List) {
                List list = (List) arg;
                if (list.size() > 0) {
                    if (list.get(0) instanceof Agent) {
                        Set<Agent> eventAgents = null;
                        if (selectionObserver.getIncident() != null && selectionObserver.getIncident().getActionPlan() != null) {
                            eventAgents = selectionObserver.getIncident().getActionPlan().getAgentsInvolved();
                        }
                        // for each
                        for (Object obj : list) {
                            Agent a = (Agent) obj;
//                            PPath node = addSquare(this.uiLayer, a.getLocation(), 20.0);
//                            node.setPaint(null);
//                            node.setStrokePaint(Color.BLACK);
//                            node.setStroke(new BasicStroke(0.75f));
                            if (eventAgents != null && eventAgents.contains(a)) {
                                visualizePlan(selectionObserver.getIncident().getActionPlan(), a, true);
                            }
                        }
                        if (list.size() > 0) {
                            //zoomInOnSelection();
                        }
                    }
                } else {
                    if (selectionObserver.getIncident() != null && selectionObserver.getIncident().getActionPlan() != null) {
                        visualizePlan(selectionObserver.getIncident().getActionPlan());
                    }
                }
            } else if (arg instanceof PlannedIncident) {
                PlannedIncident plannedIncident = (PlannedIncident) arg;
                if (plannedIncident.getActionPlan() != null) {
                    visualizePlan(plannedIncident.getActionPlan());
                }
            } else if (arg instanceof Area) {
                PArea drawableFilledArea = getDrawableFilledArea((Area) arg);
                drawableFilledArea.setPaint(Color.BLUE);
                uiLayer.addChild(drawableFilledArea);
            }
        } else {
            // Clear
        }
    }

    private void clearSelectionLayer() {
        this.uiLayer.removeAllChildren();
    }

    private class HoverEventHandler extends PBasicInputEventHandler {
        @Override
        public void mouseMoved(PInputEvent event) {
            Point2D p = event.getPosition();
            Point converted = new Point(p.getX(), -p.getY());
            positionLabel.setText(converted.toString() + "  ");
        }
    }

    private class MapClickHandler extends PBasicInputEventHandler {
        @Override
        public void mouseReleased(PInputEvent event) {
            super.mouseReleased(event);
            if (mode == Mode.INSPECT_AREA || (event.isControlDown() && event.isAltDown())) {
                Point2D p = event.getPosition();
                Point converted = new Point(p.getX(), -p.getY());
                Area selected = sim.getEnvironment().getWorld().getArea(converted);
                if (selected != null) {
                    Messenger.broadcast(String.format("Display info on area %s", selected));
                    selectionObserver.setArea(selected);
                }
            } else if (mode == Mode.INSPECT_AGENT || event.isAltDown()) {
                Point2D p = event.getPosition();
                Point converted = new Point(p.getX(), -p.getY());
                Agent a = sim.getEnvironment().getAgentClosestTo(converted);
                // Notify info panel that an agent has been selected
                Messenger.broadcast(String.format("Display info on agent %s", a));
                // ((AgentInspectorPanel) agentInspector).setAgent(a); // Not needed
                selectionObserver.setAgent(a);
            } else if (mode == Mode.PLACE_EVENT || event.isControlDown()) {
                Point2D p = event.getPosition();
                Point converted = new Point(p.getX(), -p.getY());
                try {
                    addIncident(converted);
                } catch (Exception ex) {
                    Logger.getLogger(MainFrame.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }

    private class RouteSelectionHandler extends PBasicInputEventHandler {  // TODO Merge with click handler.
        private Point prev;

        public RouteSelectionHandler() {
            super();
            this.prev = null;
        }

        @Override
        public void mouseReleased(PInputEvent event) {
            super.mouseReleased(event);
            if (mode == Mode.TEST_ROUTE || event.isShiftDown()) {
                Point2D p = event.getPosition();
                Point converted = new Point(p.getX(), -p.getY());
                if (prev == null) {
                    prev = converted;
                } else {
                    Agent a1 = sim.getEnvironment().getAgentClosestTo(prev);
                    Point v1 = prev;
                    Point v2 = converted;
                    BasicMovementModel model = new BasicMovementModel(15d / 3.6);
                    model.setLocationAndEndTime(new LocationAndTime(v2));
                    Group group = new Group(a1);
                    model.setActors(group);
                    model.setEnvironment(sim.getEnvironment());
                    a1.pushModel(model);
                    // NOTE: ugly hack to get path initialized!
                    a1.hasNextStep();
                    a1.nextStep(0.0);
                    Path path = model.getPath(a1);
                    clearSelectionLayer();
                    if (path != null && path.size() > 0) {
                        uiLayer.addChild(getDrawablePath(path));
                    }
                    // Reset
                    prev = null;
                }
            }
        }
    }
}
