package nl.tno.idsa.framework.world;

public class Polygon implements IGeometry {

    private final Point[] points;
    private final double xMin;
    private final double yMin;
    private final double xMax;
    private final double yMax;
    private final Point centroid;

    public Polygon(Point[] points) {
        this.points = points;
        double xMin = Double.POSITIVE_INFINITY;
        double yMin = Double.POSITIVE_INFINITY;
        double xMax = Double.NEGATIVE_INFINITY;
        double yMax = Double.NEGATIVE_INFINITY;
        double sumX = 0, sumY = 0;
        for (int i = 0; i < points.length; ++i) {
            xMin = Math.min(xMin, points[i].getX());
            yMin = Math.min(yMin, points[i].getY());
            xMax = Math.max(xMax, points[i].getX());
            yMax = Math.max(yMax, points[i].getY());
            sumX += points[i].getX();
            sumY += points[i].getY();
        }
        this.centroid = new Point(sumX / points.length, sumY / points.length);
        this.xMin = xMin;
        this.yMin = yMin;
        this.xMax = xMax;
        this.yMax = yMax;
    }

    @Override
    public Point[] getPoints() {
        return points;
    }

    public int size() {
        return points.length;
    }

    public Point get(int i) {
        return points[i];
    }

    @Override
    public GeometryType getGeometryType() {
        return GeometryType.POLYGON;
    }

    @Override
    public Point getFirstPoint() {
        return getPoint();
    }

    @Override
    public Point getCenterPoint() {
        return getPoint();
    }

    @Override
    public Point getLastPoint() {
        return getPoint();
    }

    private Point getPoint() {
        return centroid;
    }

    @Override
    public boolean contains(Point point) {
        return contains(point.getX(), point.getY());
    }

    private boolean contains(double x, double y) {
        boolean result = false;
        // Bounding box check
        if (x >= this.xMin && x <= this.xMax && y >= this.yMin && y <= this.yMax) {
            int i, j = points.length - 1;
            for (i = 0; i < points.length; ++i) {
                if (points[i].getY() < y && points[j].getY() >= y || points[j].getY() < y && points[i].getY() >= y) {
                    if (points[i].getX() + (y - points[i].getY()) / (points[j].getY() - points[i].getY()) * (points[j].getX() - points[i].getX()) < x) {
                        result = !result;
                    }
                }
                j = i;
            }
        }
        return result;
    }

    public double getSurface() {
        double area = 0.0;
        for (int i = 0; i < points.length; ++i) {
            int j = (i + 1) % points.length;
            // (x2-x1) (y2+y1)
            area += points[i].getX() * points[j].getY();
            area -= points[j].getX() * points[i].getY();
        }
        area *= 0.5;
        return Math.abs(area);
    }

    @Override
    public Polygon translate(Point pointRelativeToOrigin) {
        Point[] newPoints = new Point[points.length];
        for (int p = 0; p < points.length; p++) {
            newPoints[p] = (Point) points[p].translate(pointRelativeToOrigin);
        }
        return new Polygon(newPoints);
    }

    @Override
    public String toString() {
        if (points.length == 0) {
            return "()";
        }
        String ret = "(";
        for (Point p : points) {
            ret += p.toString() + ", ";
        }
        return ret + points[0] + ")";
    }
}
