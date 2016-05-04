package nl.tno.idsa.framework.world;

import com.badlogic.gdx.ai.pfa.Connection;
import com.badlogic.gdx.ai.pfa.DefaultConnection;
import com.badlogic.gdx.ai.pfa.indexed.IndexedNode;
import com.badlogic.gdx.utils.Array;
import nl.tno.idsa.framework.semantics_impl.locations.LocationFunction;
import nl.tno.idsa.library.locations.Outside;


// TODO Document class.

public class Vertex implements IndexedNode<Vertex> {

    private final Point point;
    private int index;
    private Array<Connection<Vertex>> connections;
    private Area area;

    public Vertex(Point point, Area givesEntranceTo) {
        this.index = -1;
        this.point = point;
        this.area = givesEntranceTo;
        this.connections = null;
    }

    public static double euclideanDistance(Vertex a, Vertex b) {
        return a.euclideanDistanceTo(b);
    }

    public double euclideanDistanceTo(Vertex other) {
        return point.euclideanDistanceTo(other.getPoint());
    }

    public double euclideanDistanceTo(Point p) {
        return point.euclideanDistanceTo(p);
    }

    public Point getPoint() {
        return point;
    }

    public Area getArea() {
        return area;
    }

    public void setArea(Area area) {
        this.area = area;
    }

    public boolean hasArea() {
        return area != null;
    }

    public double getX() {
        return point.getX();
    }

    public double getY() {
        return point.getY();
    }

    public void setIndex(int index) {
        this.index = index;
    }

    @Override
    public int getIndex() {
        return this.index;
    }

    @Override
    public Array<Connection<Vertex>> getConnections() {
        return this.connections;
    }

    public void addConnection(Vertex v2) {
        if (this.connections == null) {
            this.connections = new Array<>();
        }
        this.connections.add(new VertexConnection(this, v2));
    }

    public void removeConnectionTo(Vertex v) {
        if (this.connections != null) {
            int j = -1;
            for (int i = 0; j == -1 && i < this.connections.size; ++i) {
                if (this.connections.get(i).getToNode().equals(v)) {
                    j = i;
                }
            }
            if (j != -1) {
                this.connections.removeIndex(j);
            }
        }
    }

    public boolean hasFunction(Class<? extends LocationFunction> desiredFunction) {
        return (desiredFunction.equals(Outside.class) && !hasArea()) || (hasArea() && getArea().hasFunction(desiredFunction));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Vertex vertex = (Vertex) o;

        return point.equals(vertex.point);

    }

    @Override
    public int hashCode() {
        return point.hashCode();
    }

    @Override
    public String toString() {
        return "Vertex{" + "point=" + point + ", area=" + area + '}';
    }

    private class VertexConnection extends DefaultConnection<Vertex> {
        public VertexConnection(Vertex fromNode, Vertex toNode) {
            super(fromNode, toNode);
        }

        @Override
        public float getCost() {
            return (float) fromNode.euclideanDistanceTo(toNode);
        }
    }
}
