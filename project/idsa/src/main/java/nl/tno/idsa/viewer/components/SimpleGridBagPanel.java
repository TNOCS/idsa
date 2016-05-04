package nl.tno.idsa.viewer.components;

import javax.swing.*;
import java.awt.*;

/**
 * This class implements a convenient JPanel which can be used to add
 * components (JPanels) in a 1*n or n*1 (i.e. one row OR one column)
 * 'matrix' without requiring all these components to have the same size.
 * The added components will be sized in width (one row) or height (one
 * column) automatically when added. Optionally you can specify that the
 * last component in a row or column should be stretched to fill the container.
 *
 * @author Steven de Jong, steven.dejong@tno.nl
 */
public class SimpleGridBagPanel extends JPanel {
    private JPanel gridbags;
    private JPanel bottom;

    private GridBagLayout grid;
    private GridBagConstraints c;

    /**
     * Use a row layout.
     * TODO Replace by an enum.
     */
    public static final int GRID_ROWS = 0;

    /**
     * Use a column layout.
     */
    public static final int GRID_COLUMNS = 1;

    private boolean allowAddLast;

    /**
     * Construct a panel with GRID_ROWS orientation.
     */
    public SimpleGridBagPanel() {
        this(0);
    }

    /**
     * Construct a panel with given orientation (GRID_ROWS or GRID_COLUMNS).
     */
    public SimpleGridBagPanel(int orientation) {
        this(orientation, false);
    }

    /**
     * Construct a panel with given orientation and optionally stretch
     * the last component to fill the row or column completely.
     */
    public SimpleGridBagPanel(int orientation, boolean fill) {
        super();

        allowAddLast = fill;

        grid = new GridBagLayout();
        c = new GridBagConstraints();

        if (orientation == GRID_ROWS) {
            c.fill = GridBagConstraints.HORIZONTAL;
            c.gridwidth = GridBagConstraints.REMAINDER;
            c.weightx = 1.0;
        } else {
            c.fill = GridBagConstraints.VERTICAL;
            c.gridheight = GridBagConstraints.REMAINDER;
            c.weighty = 1.0;
        }

        if (allowAddLast) {
            setLayout(new BorderLayout());

            gridbags = new JPanel(grid);
            add(gridbags, (orientation == GRID_ROWS) ? BorderLayout.NORTH
                    : BorderLayout.WEST);

            bottom = new JPanel(new GridLayout(1, 1, 0, 0));
            add(bottom, BorderLayout.CENTER);
        } else {
            super.setLayout(grid);
        }
    }

    /**
     * Add a component to the panel. Refer to JPanel for details.
     */
    public Component add(Component comp) {
        grid.setConstraints(comp, c);

        // Normally just add everything.
        if (!allowAddLast)
            return super.add(comp);

        // Otherwise add the new component to the bottom/right compartment.
        if (bottom.getComponents().length > 0) {
            Component c1 = bottom.getComponent(0);
            bottom.remove(c1);
            gridbags.add(c1); // Move existing bottom/right up/left.
        }

        Component c2 = bottom.add(comp);

        validate();
        repaint();

        return c2;
    }

    /**
     * Remove a component from the panel.
     */
    public void remove(Component comp) {
        // Normal procedure.
        if (!allowAddLast) super.remove(comp);

        // Just remove everywhere.
        gridbags.remove(comp);
        bottom.remove(comp);

        // Move the last component in the gridbags to the bottom, if any.
        int i = gridbags.getComponents().length;
        if (i > 0) {
            Component c = gridbags.getComponent(i - 1);
            gridbags.remove(c);
            bottom.add(c);
        }

        validate();
        repaint();
    }
}

