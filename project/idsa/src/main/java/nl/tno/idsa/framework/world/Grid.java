package nl.tno.idsa.framework.world;

import nl.tno.idsa.library.locations.Public;

import java.util.ArrayList;
import java.util.List;

// TODO Do all grid-like things use this class? E.g. occupancy grid in the GUI? Or is this a specific class for something specific (and should it be called something else than 'Grid'?)
// TODO Document class.

public final class Grid {
    private static final double CELL_SIZE = 20.0;//m
    private final List[] staticObjects;  // Cannot be generic because we cannot make a generic array. Sigh.
    private final int widthCells;  // TODO Is this numCellsWidth?
    private final int heightCells;

    public Grid(double worldWidth, double worldHeight) {
        this.widthCells = (int) Math.ceil(worldWidth / CELL_SIZE);
        this.heightCells = (int) Math.ceil(worldHeight / CELL_SIZE);
        this.staticObjects = new List[this.widthCells * this.heightCells];
        for (int i = 0; i < this.staticObjects.length; ++i) {
            this.staticObjects[i] = new ArrayList();
        }
    }

    public Area getStaticObject(Point p) {
        List areas = getStaticObjects(p);
        Area result = null;
        boolean found = false;
        for (int i = 0; !found && i < areas.size(); ++i) {
            Area a = (Area) areas.get(i);
            if (a.contains(p)) {
                found = !a.hasFunction(Public.class);
                result = a;
            }
        }
        return result;
    }

    private List getStaticObjects(Point p) {  // TODO Generic list?
        return getStaticObjects(p.getX(), p.getY());
    }

    private List getStaticObjects(double wX, double wY) {
        return getStaticObjects(Math.max(0, Math.min(this.widthCells - 1, (int) (wX / CELL_SIZE))), Math.max(0, Math.min(this.heightCells - 1, (int) (wY / CELL_SIZE))));
    }

    private List getStaticObjects(int gX, int gY) {
        return this.staticObjects[gY * this.widthCells + gX];
    }

    @SuppressWarnings("unchecked") // Only because staticObjects cannot be generically typed.
    public void addStaticObject(Area a) {
        double minX = Double.POSITIVE_INFINITY;
        double minY = Double.POSITIVE_INFINITY;
        double maxX = Double.NEGATIVE_INFINITY;
        double maxY = Double.NEGATIVE_INFINITY;
        for (int i = 0; i < a.getPolygon().getPoints().length; ++i) {
            Point p = a.getPolygon().getPoints()[i];
            minX = Math.min(minX, p.getX());
            minY = Math.min(minY, p.getY());
            maxX = Math.max(maxX, p.getX());
            maxY = Math.max(maxY, p.getY());
        }
        // Store getSurface in all cells that overlap with its bounding box
        int gsX = Math.max(0, Math.min(this.widthCells - 1, (int) (minX / CELL_SIZE)));
        int gsY = Math.max(0, Math.min(this.heightCells - 1, (int) (minY / CELL_SIZE)));
        int geX = Math.max(0, Math.min(this.widthCells - 1, (int) (maxX / CELL_SIZE)));
        int geY = Math.max(0, Math.min(this.heightCells - 1, (int) (maxY / CELL_SIZE)));
        for (int j = gsY; j <= geY; ++j) {
            for (int i = gsX; i <= geX; ++i) {
                this.staticObjects[j * this.widthCells + i].add(a);  // TODO Unchecked.
            }
        }
    }

    @Override
    public String toString() {
        int maxLength = 0;
        for (int i = 0; i < this.staticObjects.length; ++i) {
            maxLength = Math.max(this.staticObjects[i].size(), maxLength);
        }
        return "Grid{" + "staticObjects.maxLength=" + maxLength + ", widthCells=" + widthCells + ", heightCells=" + heightCells + '}';
    }
}
