package nl.tno.idsa.framework.world;

public class PolyLine implements IGeometry {

    private Point[] points;

    public PolyLine(Point[] points) {
        this.points = points;
    }

    public PolyLine(Path path) {
        this(path.getPoints().toArray(new Point[]{}));
    }

    @Override
    public Point[] getPoints() {
        return points;
    }

    public boolean contains(Point point) {
        // NOT IMPLEMENTED
        return false;
    }

    public int size() {
        return points.length;
    }

    public Point get(int i) {
        return points[i];
    }

    @Override
    public GeometryType getGeometryType() {
        return GeometryType.POLYLINE;
    }

    @Override
    public Point getFirstPoint() {
        return (points != null && points.length > 0) ? points[0] : null;
    }

    @Override
    public Point getCenterPoint() {
        return (points != null && points.length > 0) ? points[points.length / 2] : null;
    }

    @Override
    public Point getLastPoint() {
        return (points != null && points.length > 0) ? points[points.length - 1] : null;
    }

    @Override
    public PolyLine translate(Point pointRelativeToOrigin) {
        Point[] newPoints = new Point[points.length];
        for (int p = 0; p < points.length; p++) {
            newPoints[p] = points[p].translate(pointRelativeToOrigin);
        }
        return new PolyLine(newPoints);
    }

    @Override
    public String toString() {
        String ret = "(";
        for (Point p : points) {
            ret += p.toString() + ", ";
        }
        if (points.length > 0) {
            ret = ret.substring(0, ret.length() - 2);
        }
        return ret + ")";
    }
}
