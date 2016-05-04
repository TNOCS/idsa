package nl.tno.idsa.framework.world;

// TODO Document class.
// TODO Should this be in "world" package? Same question for quite a lot of classes here.
public interface IGeometry {
    public GeometryType getGeometryType();

    public Point[] getPoints();

    public Point getFirstPoint();

    public Point getCenterPoint();

    public Point getLastPoint();

    public boolean contains(Point point);

    public IGeometry translate(Point pointRelativeToOrigin);

}
