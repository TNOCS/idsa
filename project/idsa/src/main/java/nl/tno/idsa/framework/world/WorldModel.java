package nl.tno.idsa.framework.world;

import java.util.HashMap;
import java.util.Map;
import org.jgrapht.graph.SimpleWeightedGraph;
import org.opengis.feature.Feature;

public abstract class WorldModel {

    double geoMinX;
    double geoMinY;
    double geoMaxX;
    double geoMaxY;
    SimpleWeightedGraph<Vertex, Edge> graph;
    Map<Long, Area> areas;
    Map<Long, String> buildingTypes;

    public WorldModel() {
        this.geoMinX = Double.POSITIVE_INFINITY;
        this.geoMinY = Double.POSITIVE_INFINITY;
        this.geoMaxX = Double.NEGATIVE_INFINITY;
        this.geoMaxY = Double.NEGATIVE_INFINITY;
        this.graph = new SimpleWeightedGraph<>(EdgeFactory.getInstance());
        this.areas = new HashMap<>();
        this.buildingTypes = new HashMap<>();
    }

    public boolean addVertex(Vertex vertex) {
        return graph.addVertex(vertex);
    }

    public boolean connectVertices(Vertex v1, Vertex v2) {
        Edge edge = graph.addEdge(v1, v2);
        if (edge != null) {
            graph.setEdgeWeight(edge, v1.euclideanDistanceTo(v2));
        }
        return edge != null;
    }

    public boolean removeVertex(Vertex v) {
        return graph.removeVertex(v);
    }

    public abstract long getObjectIdFromField(Feature feature);

    public abstract String getBuildingTypeFromField(Feature feature);

    public abstract String getBuildingFunctionFromField(Feature feature);

    public abstract Integer getBuildingSurfaceFromField(Feature feature);

    public abstract void addFunctionsBasedOnBuildingFunction(Area area, String function, Integer surface);

    public abstract void addFunctionsBasedOnBuildingType(Area area, String buildingType);
}
