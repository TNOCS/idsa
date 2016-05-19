package nl.tno.idsa.viewer.utils;

import nl.tno.idsa.framework.world.Area;
import nl.tno.idsa.library.locations.*;

import java.awt.*;

/**
 * Utility to give areas a color given their current status.
 */
public class AreaColorUtil {
    private static final Color water = new Color(0x80b1d3);
    private static final Color vegetation = new Color(0xb3de69);
    private static final Color commercial = new Color(0xfb8072);
    private static final Color recreation = new Color(0xffffb3);
    private static final Color education = new Color(0xbebada);
    private static final Color work = new Color(0xfdb462);

    public static Color getAreaColor(Area a) {
        // TODO Improve: use a nice color palette.
        Color result = Color.LIGHT_GRAY;
        if (a.hasFunction(Water.class)) {
            result = water;
        } else if (a.hasFunction(Park.class)) {
            result = vegetation;
        } else if (a.hasFunction(Shop.class) || a.hasFunction(ShoppingArea.class)) {
            result = commercial;
        } else if (a.hasFunction(Restaurant.class) || a.hasFunction(Theater.class) || a.hasFunction(SportsField.class)) {
            result = recreation;
        } else if (a.hasFunction(School.class) || a.hasFunction(Playground.class)) {
            result = education;
        } else if (a.hasFunction(Workplace.class)) {
            result = work;
        } else if (a.hasFunction(Outside.class)) {
            result = Color.GREEN;
        }
        return result;
    }
}
