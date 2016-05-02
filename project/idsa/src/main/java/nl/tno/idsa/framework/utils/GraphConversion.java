package nl.tno.idsa.framework.utils;

import nl.tno.idsa.framework.world.Edge;
import nl.tno.idsa.framework.world.Point;
import nl.tno.idsa.framework.world.PolyLine;

import java.util.List;

// TODO: no longer used

public class GraphConversion {

    public static PolyLine getPolyLine(List<Edge> path) {
        if (path == null || path.size() == 0) {
            return null;
        }
        Point[] points = new Point[path.size() + 1];
        points[0] = path.get(0).getSource().getPoint();
        for (int i = 0; i < path.size(); i++) {
            points[i + 1] = path.get(i).getTarget().getPoint();
        }
        return new PolyLine(points);
    }
}
