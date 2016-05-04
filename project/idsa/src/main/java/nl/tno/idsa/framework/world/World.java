package nl.tno.idsa.framework.world;

import com.badlogic.gdx.ai.pfa.Connection;
import com.badlogic.gdx.ai.pfa.DefaultGraphPath;
import com.badlogic.gdx.ai.pfa.GraphPath;
import com.badlogic.gdx.ai.pfa.Heuristic;
import com.badlogic.gdx.ai.pfa.indexed.DefaultIndexedGraph;
import com.badlogic.gdx.ai.pfa.indexed.IndexedAStarPathFinder;
import com.badlogic.gdx.utils.Array;
import nl.tno.idsa.framework.semantics_impl.locations.LocationFunction;
import nl.tno.idsa.framework.semantics_impl.variables.LocationVariable;
import nl.tno.idsa.framework.utils.GeometryUtils;
import nl.tno.idsa.framework.utils.RandomNumber;
import nl.tno.idsa.framework.utils.Tuple;

import java.util.*;


// TODO Class combines many functions.

// TODO Document class.
// TODO World package seems rather dense, with lots of different things in one package and a few very multifunctional classes.

public class World {

    private class EuclideanDistance implements Heuristic<Vertex> {
        @Override
        public float estimate(Vertex node, Vertex endNode) {
            return (float) Vertex.euclideanDistance(node, endNode);
        }
    }

    protected class VertexGraph extends DefaultIndexedGraph<Vertex> {

        private List<Edge> edges;

        /**
         * Creates an {@code IndexedGraph} with no nodes.
         */
        public VertexGraph() {
            super();
            this.edges = new ArrayList<>();
        }

        /**
         * Creates an {@code IndexedGraph} with the given capacity and no nodes.
         */
        public VertexGraph(int capacity) {
            super(capacity);
            this.edges = new ArrayList<>(capacity);
        }

        /**
         * Creates an {@code IndexedGraph} with the given nodes.
         */
        public VertexGraph(Array<Vertex> nodes) {
            super(nodes);
        }

        private void addVertex(Vertex vertex) {
            this.nodes.add(vertex);
        }

        private void addConnection(Edge e) {
            Vertex v1 = e.getSource();
            Vertex v2 = e.getTarget();
            v1.addConnection(v2);
            v2.addConnection(v1);
            this.edges.add(e);
        }

        public List<Edge> getEdges() {
            return edges;
        }

        private void removeVertex(Vertex v) {
            for (Connection<Vertex> c : v.getConnections()) {
                c.getToNode().removeConnectionTo(v);
            }
            this.nodes.removeValue(v, true);
        }
    }

    private VertexGraph graph;
    private IndexedAStarPathFinder<Vertex> pathFinder;
    private HashMap<Point, Vertex> vertices;
    private HashMap<Long, Area> areas;
    private Point utmRoot;
    private Heuristic<Vertex> euclideanDistance;
    private Grid grid;

    public World() {
        this.graph = new VertexGraph();
        this.vertices = new HashMap<Point, Vertex>();
        this.areas = new HashMap<Long, Area>();
        this.pathFinder = null;
        this.euclideanDistance = new EuclideanDistance();
        this.utmRoot = null;
        this.grid = null;
    }

    public VertexGraph getGraph() {
        return graph;
    }

    public Vertex getVertex(Point point) {
        return vertices.get(point);
    }

    public Area getArea(long areaId) {
        return areas.get(areaId);
    }

    void addVertex(Vertex v1) {
        this.vertices.put(v1.getPoint(), v1);
    }

    boolean removeVertex(Vertex v) {
        return this.vertices.remove(v.getPoint()) != null;
    }

    void addArea(Area area) {
        areas.put(area.getId(), area);
        // Add to static objects grid
        this.grid.addStaticObject(area);
    }

    Vertex addVertexToGraph(Vertex source) {
        Vertex result = source;
        if (source.getIndex() == -1) {
            source.setIndex(graph.getNodeCount());
            graph.addVertex(result);
        }
        return result;
    }

    void addConnection(Edge e) {
        graph.addConnection(e);
    }

    @SuppressWarnings("unchecked")
    public ClosestFirstIterator getClosestFirstIterator(Vertex vertex) {
        return new ClosestFirstIterator(this, vertex);
    }

    // TODO What?
    public void applyGeoRoot(double utmMinX, double utmMinY, double widthWorld, double heightWorld) {
        this.utmRoot = new Point(utmMinX, utmMinY);
        this.grid = new Grid(widthWorld, heightWorld);
    }

    public Area getArea(Point p) {
        return this.grid.getStaticObject(p);
    }

    public List<Vertex> getVertices() {
        return new ArrayList<Vertex>(vertices.values());
    }

    public List<Edge> getConnections() {
        return graph.getEdges();
    }

    @SuppressWarnings("unchecked")
    public Vertex getClosestVertex(Point origin, Class<? extends LocationFunction> desiredFunction) {
        return getClosestVertex(origin, true, desiredFunction);
    }

    @SuppressWarnings("unchecked")
    public Vertex getClosestVertex(Point origin, boolean anyFunction, Class<? extends LocationFunction>... desiredFunctions) {
        Set<Class<? extends LocationFunction>> desiredFunctionSet = new HashSet<>(desiredFunctions.length);
        desiredFunctionSet.addAll(Arrays.asList(desiredFunctions));
        List<Vertex> suitableVertices = getVertices(desiredFunctionSet, anyFunction);
        double minLength = Double.MAX_VALUE;
        Vertex minVertex = null;
        for (Vertex spawnPoint : suitableVertices) {
            // TODO: can run out of memory!
            double pathLength = getPathLengthInM(origin, spawnPoint.getPoint());
            if (minVertex == null || pathLength < minLength) {
                minVertex = spawnPoint;
                minLength = pathLength;
            }
        }
        return minVertex;
    }

    private static boolean useCache = false; // TODO Setting. Path cache is not kept clean, so memory may run out. It also does not seem to speed things up.
    private HashMap<Vertex, HashMap<Vertex, Path>> cachedPaths = new HashMap<>();

    public Path getPath(Vertex v1, Vertex v2) {
        if (useCache && cachedPaths.get(v1) != null) {
            Path cachedPath = cachedPaths.get(v1).get(v2);
            if (cachedPath != null) {
                return cachedPath;
            }
        }
        if (this.pathFinder == null) {
            this.pathFinder = new IndexedAStarPathFinder<Vertex>(graph);
        }
        GraphPath<Vertex> path = new DefaultGraphPath<Vertex>();
        this.pathFinder.searchNodePath(v1, v2, euclideanDistance, path);
        List<Point> result = new ArrayList<>();
        for (int i = 0; i < path.getCount() - 1; ++i) {
            result.add(path.get(i).getPoint());
        }
        Path newPath = new Path(result, false);
        if (useCache) {
            putPath(v1, v2, newPath);
            Path newPathReverse = newPath.reverse();
            putPath(v2, v1, newPathReverse);
        }
        return newPath;
    }

    private void putPath(Vertex v1, Vertex v2, Path newPath) {
        HashMap<Vertex, Path> cachedPathsForV1 = cachedPaths.get(v1);
        if (cachedPathsForV1 == null) {
            cachedPathsForV1 = new HashMap<>();
            cachedPathsForV1.put(v2, newPath);
            cachedPaths.put(v1, cachedPathsForV1);
        }
    }

    public Path getPath(Point source, Point target) {
        return getPath(source, target, true);
    }

    // TODO None of the paths are checked for accessibility constraints, e.g. they may go straight through buildings.
    public Path getPath(Point source, Point target, boolean customize) {

        List<Point> path = new ArrayList<>();

        // Quickly return paths with no length.
        if (source.equals(target)) {
            path.add(source);
            path.add(target);
            return new Path(path);
        }

        // Compute a path that uses the closest vertex. This may be a rather crooked path.
        path.add(source);
        Vertex startVertex = getClosestVertex(source);
        if (!source.equals(startVertex.getPoint())) {
            path.add(startVertex.getPoint());
        }
        Vertex endVertex = getClosestVertex(target);

        // Intermission: if the path does not use any vertices, return a straight line.
        if (endVertex.equals(startVertex)) {
            path.clear();
            path.add(source);
            path.add(target);
            return new Path(path);
        }

        // Add the intermediate path and the target point.
        path.addAll(getPath(startVertex, endVertex).getPoints());
        if (!target.equals(endVertex.getPoint())) {
            path.add(endVertex.getPoint());
        }
        path.add(target);

        // Now, refine the path by finding the closest route from the start and end point to this path.
        Path ppath = new Path(path); // Annoying conversions are needed here between a list of points and a poly line.
        Tuple<Point, Integer> sourceIntersection = GeometryUtils.getClosestIntersectionPointOnPolyLine(source, ppath.getPolyLine());
        Tuple<Point, Integer> targetIntersection = GeometryUtils.getClosestIntersectionPointOnPolyLine(target, ppath.getPolyLine());

        // Strip path segments after the interception point to the end point. Then, add the interception point.
        while (path.size() > targetIntersection.getSecond() + 1) {
            path.remove(path.size() - 1);
        }
        path.add(targetIntersection.getFirst());
        path.add(target);

        // Also strip path segments before the interception point to the start point, then add this interception point too.
        Point newFirstPoint = path.get(sourceIntersection.getSecond() + 1);
        while (!path.get(0).equals(newFirstPoint)) {
            path.remove(0);
        }
        path.add(0, sourceIntersection.getFirst());
        path.add(0, source);

        // Create a smoother path that has some randomness.
        return customize ? new Path(path).generateCustomPath(5.0, 15.0) : new Path(path);
    }

    public double getPathLengthInM(Point source, Point destination) {
        return getPath(source, destination).lengthInM();
    }

    public Path samplePathWithLength(Point center, double desiredPathLength) {
        return samplePathWithLength(center, desiredPathLength, null);
    }

    public Path samplePathWithLength(Point center, double desiredPathLength, Class<? extends LocationFunction> desiredDestinationFunction) {
        // TODO IMPORTANT: Paths should start/stop on edges rather than only vertices. Better still: anywhere the agent(s) can walk.
        // TODO IMPORTANT: Take some initial direction into account if not null. (Walk towards that.)
        // TODO Facilitate sampling really short paths?
        int sampleAttempts = 0;
        double minLengthFactor = 0.8; // TODO Magic number.
        int maxSamples = 30;          // TODO Magic number.
        while (++sampleAttempts < maxSamples) {
            List<Vertex> suitableVertices =
                    desiredDestinationFunction != null
                            ? getVertices(desiredDestinationFunction)
                            : getVertices();
            Vertex vertex = RandomNumber.randomElement(suitableVertices);
            Path path = getPath(center, vertex.getPoint());
            double currentPathLength = path.lengthInM();
            boolean found = currentPathLength <= desiredPathLength && currentPathLength > minLengthFactor * desiredPathLength;
            if (currentPathLength > desiredPathLength) {
                while (!found && path.size() > 1) {
                    path.remove(path.size() - 1);
                    currentPathLength = path.lengthInM();
                    found = currentPathLength <= desiredPathLength && currentPathLength > minLengthFactor * desiredPathLength;
                }
            } else if (currentPathLength < minLengthFactor * desiredPathLength) {
                // Just do a resample.
            }
            if (found) {
                return path;
            }
        }
        return null;
    }

    public Point getUtmRoot() {
        return utmRoot;
    }

    public Vertex getClosestVertex(Point point) {
        Vertex result = null;
        double minDistance = Double.MAX_VALUE;
        for (Vertex v : vertices.values()) {
            double distance = v.euclideanDistanceTo(point);
            if (distance < minDistance) {
                result = v;
                minDistance = distance;
            }
        }
        return result;
    }

    public Collection<Area> getAreas() {
        return areas.values();
    }

    public List<Vertex> getVertices(Class<? extends LocationFunction> desiredFunction) {
        HashSet<Class<? extends LocationFunction>> desiredFunctions = new HashSet<>(1);
        desiredFunctions.add(desiredFunction);
        return getVertices(desiredFunctions, true);
    }

    public List<Vertex> getVertices(Set<Class<? extends LocationFunction>> desiredFunctions, boolean anyFunction) {
        if (desiredFunctions == null || desiredFunctions.isEmpty()) {
            return getVertices();
        }
        List<Vertex> result = new ArrayList<Vertex>();
        if (anyFunction || desiredFunctions.size() == 1) {
            // OR case
            for (Vertex v : vertices.values()) {
                for (Class<? extends LocationFunction> desiredFunction : desiredFunctions) {
                    if (v.hasFunction(desiredFunction)) {
                        result.add(v);
                        break;
                    }
                }
            }
        } else {
            // AND case
            for (Vertex v : vertices.values()) {
                boolean suitable = true;
                for (Class<? extends LocationFunction> desiredFunction : desiredFunctions) {
                    if (!v.hasFunction(desiredFunction)) {
                        suitable = false;
                        break;
                    }
                }
                if (suitable) {
                    result.add(v);
                }
            }
        }
        return result;
    }

    public Vertex sampleVertexCloserTo(LocationVariable source, Point target, double improvementBound) {
        final Set<Class<? extends LocationFunction>> desiredFunctions = source.getAllowedFunctions();
        // FIXME: providing false to this statement makes functions checked with AND logic, comment below claims otherwise!
        final List<Vertex> vs = getVertices(desiredFunctions, false); // The functions are meant as an OR, not an AND.
        return sampleVertexCloserTo(vs, source.getValue().getLocation().getLastPoint(), target, improvementBound);
    }

    public Vertex sampleVertexCloserTo(Point source, Point target, double improvementBound) {
        return sampleVertexCloserTo(getVertices(), source, target, improvementBound);
    }

    private Vertex sampleVertexCloserTo(List<Vertex> candidates, Point source, Point target, double improvementBound) {
        Vertex result = null;
        // Find a location that is closer to target than source location and respects the desired function et cetera.
        double originalDistance = getPathLengthInM(source, target);
        // Find all suitable vertices
        if (candidates.size() > 0) {
            int numSamples = 0;
            final int maxSamples = 10000;
            boolean found = false;
            // Random select
            while (!found && numSamples < maxSamples) {
                int index = RandomNumber.nextInt(candidates.size());
                // TODO Is planning over graph a lot slower?
                double distance = candidates.get(index).euclideanDistanceTo(target); // TODO-> SLOW code: getPathLengthInM(candidates.get(index).getPoint(), target); F: Probably not needed...
                double requiredMaxDistance = originalDistance;
                if (numSamples > (3 * maxSamples / 4)) {
                    requiredMaxDistance *= 1 - ((1 - improvementBound) / 4);
                } else if (numSamples > (maxSamples / 2)) {
                    requiredMaxDistance *= 1 - ((1 - improvementBound) / 2);
                } else {
                    requiredMaxDistance *= improvementBound;
                }
                if (distance < requiredMaxDistance) {
                    result = candidates.get(index);
                    found = true;
                    //System.out.println("Distance shortened; original distance is " + originalDistance + " and the new distance is " + distance); //todo: set in logger.
                }
                ++numSamples;
            }
            // Linear search if maximum number of samples reached
            if (!found) {
                //System.out.println("Starting linear search sampling for a location. The incident is difficult to realize."); //todo: decide on printing this. Logger/refine print?.
                for (int j = 0; !found && j < candidates.size(); ++j) {
                    double distance = candidates.get(j).euclideanDistanceTo(target);
                    if (distance < originalDistance) {
                        result = candidates.get(j);
                        found = true;
                        //todo: set next printstatement in logger.
                        //System.out.println("Distance shortened; original distance is " + originalDistance + " and the new distance is " + distance);
                    }
                }
                if (!found) {
                    //System.out.println("No better location available, error!, the current distance is " + originalDistance);
                } else {
                    //todo: set in logger
                    //System.out.println("Needed " + numSamples + " iterations for resampling a location.");
                }
            } else {
                //todo: set in logger
                //System.out.println("Needed " + numSamples + " iterations for resampling a location.");
            }
        }
        return result;
    }

    @Override
    public String toString() {
        return "World{" + "utmRoot=" + utmRoot + ", grid=" + grid + '}';
    }
}
