package nl.tno.idsa.framework.world;

import com.badlogic.gdx.ai.pfa.Connection;

import java.util.HashMap;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.PriorityQueue;

// TODO Document class.
// TODO This iterator is a lot more expensive than e.g. a breadth first iterator. Implement a breadth first iterator and compare speed vs. quality.

public class ClosestFirstIterator implements Iterator<Vertex> {

    private final World.VertexGraph graph;
    private final PriorityQueue<Node> heap;
    private final HashMap<Vertex, Double> seenVertices;
    private double currentPathLengthInM;

    public ClosestFirstIterator(World world, Vertex start) {
        graph = world.getGraph();
        heap = new PriorityQueue<>();
        seenVertices = new HashMap<>();
        currentPathLengthInM = 0;
        seenVertices.put(start, 0.0);
        heap.add(new Node(start, 0.0));
    }

    public boolean hasSeen(Vertex v) {
        return seenVertices.containsKey(v);
    }

    public double getPathLength(Vertex vertex) {
        return seenVertices.get(vertex);
    }

    @Override
    public boolean hasNext() {
        return !heap.isEmpty();
    }

    @Override
    public Vertex next() {
        Node next = heap.poll();
        if (next == null) {
            throw new NoSuchElementException();
        }
        currentPathLengthInM = next.pathLengthInM;
        AddNewVerticesToHeap(next);
        return next.vertex;
    }

    private void AddNewVerticesToHeap(Node next) {
        for (Connection<Vertex> connection : graph.getConnections(next.vertex)) {
            Vertex neighbour = connection.getToNode();
            if (!seenVertices.containsKey(neighbour)) {
                double pathLength = next.pathLengthInM + connection.getCost();
                seenVertices.put(neighbour, pathLength);
                heap.add(new Node(neighbour, pathLength));
            }
        }
    }

    public double getCurrentPathLengthInM() {
        return currentPathLengthInM;
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }

    private class Node implements Comparable {
        private Vertex vertex;
        private Double pathLengthInM;

        public Node(Vertex vertex, Double pathLengthInM) {
            this.vertex = vertex;
            this.pathLengthInM = pathLengthInM;
        }

        @Override
        public int compareTo(Object o) {
            Node other = (Node) o;
            return pathLengthInM.compareTo(other.pathLengthInM);
        }
    }
}
