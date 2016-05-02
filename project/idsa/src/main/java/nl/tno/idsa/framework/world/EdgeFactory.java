package nl.tno.idsa.framework.world;

// TODO Document class. See Edge. This seems like a workaround.
public class EdgeFactory implements org.jgrapht.EdgeFactory<Vertex, Edge> {

    private static final EdgeFactory instance = new EdgeFactory();

    public static EdgeFactory getInstance() {
        return instance;
    }

    private EdgeFactory() {
    }

    @Override
    public Edge createEdge(Vertex sourceVertex, Vertex targetVertex) {
        return new Edge(sourceVertex, targetVertex, sourceVertex.euclideanDistanceTo(targetVertex));
    }
}
