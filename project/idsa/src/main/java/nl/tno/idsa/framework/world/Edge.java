package nl.tno.idsa.framework.world;

import org.jgrapht.graph.DefaultWeightedEdge;

// TODO See the TODO below. This is a hack.
public class Edge extends DefaultWeightedEdge {

    // TODO According to JGRAPHT documentation, we should not have to subclass DefaultWeightedEdge, but we don't get this to work otherwise. Ugly.

    private final Vertex source;
    private final Vertex target;
    private double weight;

    public Edge(Vertex source, Vertex target) {
        this.source = source;
        this.target = target;
    }

    public Edge(Vertex source, Vertex target, double weight) {
        this.source = source;
        this.target = target;
        this.weight = weight;
    }

    @Override
    public Vertex getSource() {
        return source;
    }

    @Override
    public Vertex getTarget() {
        return target;
    }

    @Override
    public double getWeight() {
        return weight;
    }

    @Override
    public String toString() {
        return "Edge{" + "source=" + source + ", target=" + target + ", weight=" + weight + '}';
    }
}
