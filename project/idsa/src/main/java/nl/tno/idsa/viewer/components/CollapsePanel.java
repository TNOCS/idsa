package nl.tno.idsa.viewer.components;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by jongsd on 27-10-15.
 */
// TODO Document class.
public class CollapsePanel extends JPanel {

    private static HashMap<Side, java.util.List<CollapsePanel>> panelsOnSameSide = new HashMap<>(); // All collapse and expand at the same time.

    private final Side side;

    private final JButton btnCollapse;
    private final JScrollPane mainScrollPanel;

    private JPanel toolbar;
    private JPanel mainPanel;

    public static enum Side {

        LEFT(">>", "<<", true), RIGHT("<<", ">>", true), TOP("\\/", "/\\", false), BOTTOM("/\\", "\\/", false);

        private final String expandedCaption;
        private final String collapsedCaption;
        private final boolean isHorizontal;

        Side(String collapsedCaption, String expandedCaption, boolean isHorizontal) {
            this.collapsedCaption = collapsedCaption;
            this.expandedCaption = expandedCaption;
            this.isHorizontal = isHorizontal;
        }
    }

    public CollapsePanel(final Side side) {

        this.side = side;

        List<CollapsePanel> collapsePanels = panelsOnSameSide.get(side);
        if (collapsePanels == null) {
            collapsePanels = new ArrayList<>();
            panelsOnSameSide.put(side, collapsePanels);
        }
        collapsePanels.add(this);

        setLayout(new BorderLayout(0, 0));
        setBorder(new LineBorder(SystemColor.controlDkShadow, 1));

        final CollapsePanel me = this;
        btnCollapse = new JButton(new AbstractAction(side.expandedCaption) {
            @Override
            public void actionPerformed(ActionEvent e) {
                List<CollapsePanel> onSameSide = panelsOnSameSide.get(side);
                if (btnCollapse.getText().equals(side.expandedCaption)) {
                    for (CollapsePanel collapsePanel : onSameSide) {
                        if (collapsePanel.getParent() != null && collapsePanel.getParent().equals(getParent())) {
                            collapsePanel.collapse();
                        }
                    }
                } else {
                    for (CollapsePanel collapsePanel : onSameSide) {
                        if (collapsePanel.getParent() != null && collapsePanel.getParent().equals(getParent())) {
                            collapsePanel.expand();
                        }
                    }
                }
            }
        });
        btnCollapse.setFocusPainted(false);

        toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 3, 3));
        toolbar.setBorder(new EmptyBorder(3, 3, 3, 3));
        toolbar.add(btnCollapse);
        add(toolbar, BorderLayout.NORTH);

        mainPanel = new JPanel(new BorderLayout(0, 0));
        mainPanel.setBorder(new EmptyBorder(3, 3, 3, 3));

        mainScrollPanel = new JScrollPane(mainPanel, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        mainScrollPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, SystemColor.controlDkShadow));
        add(mainScrollPanel, BorderLayout.CENTER);

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                collapse();
            }
        });
    }

    public JPanel getToolbar() {
        return toolbar;
    }

    public JPanel getMainPanel() {
        return mainPanel;
    }

    protected void expand() {
        btnCollapse.setText(side.expandedCaption);
        if (side.isHorizontal) {
            changeWidth(300);
        } else {
            changeHeight(300);
        }
        mainScrollPanel.setVisible(true);
        mainScrollPanel.invalidate();
    }

    protected void collapse() {
        btnCollapse.setText(side.collapsedCaption);
        if (side.isHorizontal) {
            changeWidth(btnCollapse.getWidth() + 12);
        } else {
            changeHeight(btnCollapse.getHeight() + 12);
        }
        mainScrollPanel.setVisible(false);
    }

    private void changeHeight(int height) {
        setMinimumSize(new Dimension(300, height));
        setMaximumSize(new Dimension(Integer.MAX_VALUE, height));
        setPreferredSize(new Dimension(1000, height));
    }

    private void changeWidth(int width) {
        setMinimumSize(new Dimension(width, 300));
        setMaximumSize(new Dimension(width, Integer.MAX_VALUE));
        setPreferredSize(new Dimension(width, 1000));
    }

}
