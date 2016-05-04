package nl.tno.idsa.framework.world;

import nl.tno.idsa.framework.utils.RandomNumber;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

// TODO Document class.
public class Path {
    private final List<Point> points;
    private int pathIndex;

    public Path(PolyLine polyLine) {
        this(Arrays.asList(polyLine.getPoints()));
    }

    public Path(List<Point> points) {
        this(points, true);
    }

    public Path(List<Point> points, boolean deepCopy) {
        if (deepCopy) {
            this.points = new ArrayList<>(points.size());
            for (int i = 0; i < points.size(); ++i) {
                add(points.get(i));
            }
        } else {
            this.points = points;
        }
        this.pathIndex = 0;
    }

    public Path reverse() {
        Path path = new Path(points);
        Collections.reverse(path.points);
        return path;
    }

    public boolean endReached() {
        return pathIndex >= points.size();
    }

    public Point get(int index) {
        return this.points.get(index);
    }

    public Point remove(int index) {
        return this.points.remove(index);
    }

    public Point getTarget() {
        return this.points.get(Math.min(points.size() - 1, pathIndex));
    }

    public Point getFinalTarget() {
        return this.points.get(points.size() - 1);
    }

    public boolean nextTarget() {
        ++this.pathIndex;
        return endReached();
    }

    public Point moveAlong(Point currentLocation, double distance) {
        double newX = currentLocation.getX();
        double newY = currentLocation.getY();
        double edgeFactor = 0.0;
        if (!endReached()) {
            // Start at currentLocation
            Point t1 = currentLocation;
            // End of first segment
            Point t2 = getTarget();
            // Distance to travel
            double currentDistance = distance;
            while (currentDistance > 0 && !endReached()) {
                double edgeLength = t1.euclideanDistanceTo(t2);
                if (currentDistance - edgeLength > 0.0) {
                    // Continue to next edge
                    currentDistance -= edgeLength;
                    boolean endReached = nextTarget();
                    if (!endReached) {
                        t1 = t2;
                        t2 = getTarget();
                    } else {
                        // End of path reached, halt at destination
                        edgeFactor = 1.0;
                    }
                } else {
                    edgeFactor = currentDistance / edgeLength;
                    // Travelled the distance
                    currentDistance = 0.0;
                }
            }
            // Update coordinates based on final (t1, t2) pair
            newX = t1.getX() + Math.max(0.0, Math.min(1.0, edgeFactor)) * (t2.getX() - t1.getX());
            newY = t1.getY() + Math.max(0.0, Math.min(1.0, edgeFactor)) * (t2.getY() - t1.getY());
        }
        return new Point(newX, newY);
    }

    public int size() {
        return this.points.size();
    }

    public double lengthInM() {
        double result = 0.0;
        for (int i = 0; i < points.size() - 1; ++i) {
            result += points.get(i).euclideanDistanceTo(points.get(i + 1));
        }
        return result;
    }

    public void append(Point[] points) {
        for (int i = 0; i < points.length; ++i) {
            add(points[i]);
        }
    }

    private boolean add(Point p) {
        boolean result = size() == 0 || !this.points.get(this.points.size() - 1).equals(p);
        if (result) {
            this.points.add(p);
        }
        return result;
    }

    public List<Point> getPoints() {
        return this.points;
    }

    public Path generateCustomPath(double range, double maxRange) {
        if (range <= 0) {
            range = 0.1; // 0 crashes this method
        }
        List<Point> track = new ArrayList<>();
        double segmentLength = 1.5 * range;
        double maxLength = 1000.0;
        double angleFactor = 0.75;
        int numPoints = size();
        // Origin point
        track.add(get(0));
        for (int i = 0; i < numPoints - 1; ++i) {
            int aIndex = Math.min(numPoints - 2, i);
            int bIndex = Math.min(numPoints - 1, i + 1);
            int cIndex = Math.min(numPoints - 1, i + 2);
            Point a = get(aIndex);
            Point b = get(bIndex);
            Point c = get(cIndex);
            double angle = bearing(a, b);
            double angleNext = bIndex != cIndex ? bearing(b, c) : angle;
            double length = a.euclideanDistanceTo(b);
            double curRange = range;
            // For all successive points after first segment
            if (i > 0) {
                // Scale divergence
                curRange *= RandomNumber.nextDouble(1.0 + ((maxRange / range) - 1.0) * (length / maxLength));
                // Flip side
                curRange *= RandomNumber.nextDouble() < 0.5 ? -1.0 : 1.0;
            }
            int numSegments = Math.max(1, (int) Math.floor(length / segmentLength));
            if (length > 0.0) {
                if (numSegments > 1) {
                    for (int k = 1; k <= numSegments; ++k) {
                        Point cur, from, to;
                        from = get(i);
                        double step = length * ((double) k / (numSegments));
                        // Translate to intermediate segment point
                        cur = translatePoint(from, angle, step);
                        // Arc range -1.0 .. 1.0						
                        double rf = 2.0 * (step / length) - 1.0;
                        rf *= rf;
                        // Current offset range
                        double r = curRange * Math.max(0.0, Math.sqrt(1.0 - rf));
                        // Offset to side
                        // Take into account local curvature of route
                        double angleStep = Math.max(0.0, step - angleFactor * length) / ((1.0 - angleFactor) * length);
                        double curAngle = (1.0 - angleStep) * angle + angleStep * angleNext;
                        to = translatePoint(cur, curAngle + 0.5 * Math.PI, r);
                        track.add(to);
                    }
                } else {
                    // Just add the original points (for very short segments, such as curves)
                    track.add(get(i));
                }
            }
        }
        // Destination point
        track.add(get(numPoints - 1));
        return new Path(track);
    }

    private double bearing(Point a, Point b) {
        return angle(b.minus(a));
    }

    private Point translatePoint(Point from, double angle, double step) {
        double x = from.getX() + step * Math.cos(angle);
        double y = from.getY() + step * Math.sin(angle);
        return new Point(x, y);
    }

    private double angle(Point minus) {
        Point norm = minus.normalize();
        double ang = Math.acos(norm.getX());
        if (norm.getY() < 0) {
            ang = 2 * Math.PI - ang;
        }
        return ang;
    }

    public PolyLine getPolyLine() {
        return new PolyLine(points.toArray(new Point[points.size()]));
    }

    @Override
    public String toString() {
        return getPolyLine().toString();
    }
}
