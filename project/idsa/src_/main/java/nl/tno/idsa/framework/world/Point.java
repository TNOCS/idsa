package nl.tno.idsa.framework.world;

public class Point implements IGeometry {
    private final double x;
    private final double y;

    public Point(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public Point(com.vividsolutions.jts.geom.Point point) {
        this.x = point.getX();
        this.y = point.getY();
    }

    public Point minus(Point other) {
        return new Point(x - other.x, y - other.y);
    }

    public Point plus(Point other) {
        return new Point(x + other.x, y + other.y);
    }

    public Point times(double factor) {
        return new Point(x * factor, y * factor);
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    private static final double TOLERANCE = 0.001; 

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Point point = (Point) o;
        return Math.abs(point.x - x) < TOLERANCE && Math.abs(point.y - y) < TOLERANCE;
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        temp = Double.doubleToLongBits(x);
        result = (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(y);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        return result;
    }

    public double euclideanDistanceTo(Point other) {
        double distance;
        double dx = x - other.getX();
        double dy = y - other.getY();
        double x2 = dx * dx;
        double y2 = dy * dy;
        distance = Math.sqrt(x2 + y2);
        return distance;
    }

    public double manhattanDistanceTo(Point other) {
        double distance;
        double dx = Math.abs(x - other.getX());
        double dy = Math.abs(y - other.getY());
        distance = dx + dy;
        return distance;
    }

    @Override
    public String toString() {
        return "(" + Math.round(x * 100) / 100.0 + "," + Math.round(y * 100) / 100.0 + ")";
    }

    @Override
    public GeometryType getGeometryType() {
        return GeometryType.POINT;
    }

    @Override
    public Point[] getPoints() {
        return new Point[]{this};
    }

    @Override
    public Point getFirstPoint() {
        return this;
    }

    @Override
    public Point getCenterPoint() {
        return this;
    }

    @Override
    public Point getLastPoint() {
        return this;
    }

    public boolean contains(Point point) {
        return this.equals(point);
    }

    public Double getLength() {
        return Math.sqrt(x * x + y * y);
    }

    public double getSquareLength() {
        return x * x + y * y;
    }

    @Override
    public Point translate(Point pointRelativeToOrigin) {
        return this.plus(pointRelativeToOrigin);
    }

    public Point normalize() {
        double length = this.getLength();
        double x = this.x;
        double y = this.y;
        if (length > 0) {
            double scale = 1.0 / length;
            x *= scale;
            y *= scale;
        }
        return new Point(x, y);
    }
}
