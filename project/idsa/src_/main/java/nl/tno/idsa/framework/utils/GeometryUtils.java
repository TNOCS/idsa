package nl.tno.idsa.framework.utils;

import nl.tno.idsa.framework.world.IGeometry;
import nl.tno.idsa.framework.world.Point;
import nl.tno.idsa.framework.world.PolyLine;

import java.awt.geom.Rectangle2D;

// TODO Document class.

public class GeometryUtils {

    public static double pointProjectedOnLine(Point point, Point t1, Point t2) {
        return ((point.getX() - t1.getX()) * (t2.getX() - t1.getX()) + (point.getY() - t1.getY()) * (t2.getY() - t1.getY())) / t2.minus(t1).getSquareLength();
    }

    /**
     * Distance to line segment and proportion of the intersection point (0-1).
     */
    public static Tuple<Double, Double> distanceToLineSegment(Point point, Point t1, Point t2) {
        double factor = Math.max(0.0, Math.min(1.0, pointProjectedOnLine(point, t1, t2)));
        Point intersectionPoint = t1.plus(t2.minus(t1).times(factor));
        return new Tuple<Double, Double>((point.minus(intersectionPoint)).getLength(), factor);
    }

    public static Point getClosestIntersectionPoint(Point sourceLocation, IGeometry targetLocation) {
        Point c = null;
        if (targetLocation instanceof Point) {
            c = (Point) targetLocation;
        } else if (targetLocation instanceof PolyLine) {
            c = getClosestIntersectionPointOnPolyLine(sourceLocation, (PolyLine) targetLocation).getFirst();
        }
        return c;
    }


    public static Point getClosestIntersectionPointOnLineSegment(Point point, Point t1, Point t2) {
        double factor = Math.max(0.0, Math.min(1.0, pointProjectedOnLine(point, t1, t2)));
        return t1.plus(t2.minus(t1).times(factor));
    }

    /**
     * Returns the intersection point and the index of the point on the path *after* which the intersection
     * takes place.
     */
    public static Tuple<Point, Integer> getClosestIntersectionPointOnPolyLine(Point point, PolyLine path) {
        double minDist = Double.MAX_VALUE;
        Tuple<Point, Integer> minPoint = null;
        for (int i = 0; i < path.getPoints().length - 1; i++) {
            Point p = getClosestIntersectionPointOnLineSegment(point, path.getPoints()[i], path.getPoints()[i + 1]);
            double dist = p.euclideanDistanceTo(point);
            if (dist < minDist) {
                minDist = dist;
                minPoint = new Tuple<>(p, i);
            }
        }
        return minPoint;
    }

    public static double distanceToInfiniteLine(Point point, Point t1, Point t2) {
        double factor = pointProjectedOnLine(point, t1, t2);
        Point intersectionPoint = t1.plus(t2.minus(t1).times(factor));
        return (point.minus(intersectionPoint)).getLength();
    }

    public static boolean isOnLine(Point point, Point source, Point target) {
        return isOnLineT(point, source, target).getFirst();
    }

    public static Tuple<Boolean, Double> isOnLineT(Point point, Point source, Point target) {
        Tuple<Double, Double> dtls = distanceToLineSegment(point, source, target);
        double factor = dtls.getSecond();
        return new Tuple<Boolean, Double>(dtls.getFirst() < 0.1, factor);
    }

    public static Rectangle2D getBoundingBox(IGeometry geometry) {
        double minX = Double.MAX_VALUE;
        double maxX = -Double.MAX_VALUE;
        double minY = Double.MAX_VALUE;
        double maxY = -Double.MAX_VALUE;

        Point[] points = geometry.getPoints();

        for (int i = 0; i < points.length; i += 2) {
            double x = points[i].getX();
            double y = points[i].getY();
            minX = Math.min(minX, x);
            maxX = Math.max(maxX, x);
            minY = Math.min(minY, y);
            maxY = Math.max(maxY, y);
        }

        return new Rectangle2D.Double(minX, minY, maxX, maxY);
    }
}
