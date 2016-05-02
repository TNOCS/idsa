package nl.tno.idsa.framework.world;

// JDK imports

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPolygon;
import nl.tno.idsa.framework.utils.FeatureUtils;
import org.geotools.data.FileDataStore;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.opengis.feature.Feature;
import org.opengis.feature.GeometryAttribute;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

// 3rdParty imports
// Project imports

// TODO Document class.
// TODO Document the expected inputs.
public class WorldGenerator {

    public static World generateWorld(WorldModel model, String networkFile, String buildingFile, String areaFile, String areaFunctionFile, String areaVertexConnectionFile) {
        World world = new World();
        parseNetwork(world, model, networkFile);
        parseAreas(model, buildingFile);
        parseAreas(model, areaFile);
        parseLocationsLinkedToAreas(world, model, areaVertexConnectionFile);
        parseFunctions(world, model, areaFunctionFile);
        classifyUnclassified(world, model);
        // Apply parsed data model        
        // Create nav graph
        for (Edge e : model.graph.edgeSet()) {
            Vertex v1 = world.addVertexToGraph(e.getSource());
            Vertex v2 = world.addVertexToGraph(e.getTarget());
            world.addConnection(new Edge(v1, v2));
        }
        return world;
    }

    private static void parseNetwork(World world, WorldModel model, String networkFile) {
        FileDataStore store = FeatureUtils.loadFeatures(networkFile);
        SimpleFeatureCollection features = FeatureUtils.getFeatures(store);
        if (features != null) {
            FeatureIterator iterator = features.features();
            List<Point> points = new ArrayList<>();
            while (iterator.hasNext()) {
                Feature feature = iterator.next();
                GeometryAttribute attribute = feature.getDefaultGeometryProperty();
                MultiLineString multiLineString = (MultiLineString) attribute.getValue();
                Coordinate[] coordinates = multiLineString.getCoordinates();
                if (coordinates.length > 2) {
                    System.out.format("More than two points in input line %d%n", coordinates.length);      // TODO Log to logger.
                }
                Point p1 = new Point(coordinates[0].x, coordinates[0].y);
                Point p2 = new Point(coordinates[1].x, coordinates[1].y);
                // Determine geo bounding box
                model.geoMinX = Math.min(model.geoMinX, p1.getX());
                model.geoMinY = Math.min(model.geoMinY, p1.getY());
                model.geoMaxX = Math.max(model.geoMaxX, p1.getX());
                model.geoMaxY = Math.max(model.geoMaxY, p1.getY());
                model.geoMinX = Math.min(model.geoMinX, p1.getX());
                model.geoMinY = Math.min(model.geoMinY, p1.getY());
                model.geoMaxX = Math.max(model.geoMaxX, p1.getX());
                model.geoMaxY = Math.max(model.geoMaxY, p1.getY());
                points.add(p1);
                points.add(p2);
            }
            // Determine geo root (inc. safety margin)
            // NOTE: assumes coordinates in meters!
            model.geoMinX -= 50;
            model.geoMinY -= 50;
            model.geoMaxX += 50;
            model.geoMaxY += 50;
            world.applyGeoRoot(model.geoMinX, model.geoMinY, model.geoMaxX - model.geoMinX, model.geoMaxY - model.geoMinY);

            for (int i = 0; i < points.size() - 1; i += 2) {
                Vertex v1 = new Vertex(new Point(points.get(i + 0).getX() - model.geoMinX, points.get(i + 0).getY() - model.geoMinY), null);
                Vertex v2 = new Vertex(new Point(points.get(i + 1).getX() - model.geoMinX, points.get(i + 1).getY() - model.geoMinY), null);
                v1 = addVertex(world, model, v1);
                v2 = addVertex(world, model, v2);
                if (!v1.equals(v2)) {
                    model.connectVertices(v1, v2);
                    // System.err.format("Could not connect %s and %s in graph.%n", v1, v2);
                }
            }
            // Clean up and release file
            iterator.close();
            store.dispose();
        }
    }

    private static Vertex addVertex(World world, WorldModel model, Vertex v) {
        Vertex result = v;
        boolean vAdded = model.addVertex(v);
        if (vAdded) {
            world.addVertex(v);
        } else {
            result = world.getVertex(v.getPoint());
        }
        return result;
    }

    private static void parseAreas(WorldModel model, String areaFile) {
        FileDataStore store = FeatureUtils.loadFeatures(areaFile);
        SimpleFeatureCollection features = FeatureUtils.getFeatures(store);
        if (features != null) {
            FeatureIterator iterator = features.features();
            while (iterator.hasNext()) {
                Feature feature = iterator.next();
                GeometryAttribute attribute = feature.getDefaultGeometryProperty();
                MultiPolygon multiPolygon = (MultiPolygon) attribute.getValue();
                Coordinate[] coordinates = multiPolygon.getCoordinates();
                Point[] points = new Point[coordinates.length];
                for (int i = 0; i < coordinates.length; ++i) {
                    points[i] = new Point(coordinates[i].x - model.geoMinX, coordinates[i].y - model.geoMinY);
                }
                Polygon poly = new Polygon(points);
                long areaId = model.getObjectIdFromField(feature);
                String buildingType = model.getBuildingTypeFromField(feature);
                Area area = new Area(areaId, poly, buildingType);
                model.buildingTypes.put(areaId, buildingType);
                model.areas.put(areaId, area);
            }
            // Clean up and release file
            iterator.close();
            store.dispose();
        }
    }

    private static void parseLocationsLinkedToAreas(World world, WorldModel model, String areaFile) {
        FileDataStore store = FeatureUtils.loadFeatures(areaFile);
        SimpleFeatureCollection features = FeatureUtils.getFeatures(store);
        if (features != null) {
            FeatureIterator iterator = features.features();
            while (iterator.hasNext()) {
                Feature feature = iterator.next();
                GeometryAttribute attribute = feature.getDefaultGeometryProperty();
                com.vividsolutions.jts.geom.Point mp = (com.vividsolutions.jts.geom.Point) attribute.getValue();
                Coordinate[] coordinates = mp.getCoordinates();
                Point p = new Point(coordinates[0].x - model.geoMinX, coordinates[0].y - model.geoMinY);
                long areaId = model.getObjectIdFromField(feature);
                // Link existing vertex in the network with the found location
                if (model.areas.containsKey(areaId)) {
                    Area area = model.areas.get(areaId);
                    Vertex v = world.getVertex(p);
                    if (v != null) {
                        v.setArea(area);
                        area.addVertex(v);
                    } else {
                        System.out.format("Could not find vertex %s for area %d%n", p, areaId);    // TODO logger
                    }
                }
            }
            // Clean up and release file
            iterator.close();
            store.dispose();
        }
        // Now add all areas to the world, removing any unconnected areas (no vertices to access it)
        for (Iterator<Map.Entry<Long, Area>> it = model.areas.entrySet().iterator(); it.hasNext(); ) {
            Map.Entry<Long, Area> entry = it.next();
            if (!entry.getValue().getVertices().isEmpty()) {
                world.addArea(entry.getValue());
            } else {
                it.remove();
            }
        }
        System.out.println(world.toString());
    }

    private static void parseFunctions(World world, WorldModel model, String file) {
        FileDataStore store = FeatureUtils.loadFeatures(file);
        SimpleFeatureCollection features = FeatureUtils.getFeatures(store);
        if (features != null) {
            FeatureIterator iterator = features.features();
            while (iterator.hasNext()) {
                Feature feature = iterator.next();
                Long id = model.getObjectIdFromField(feature);
                Area area = world.getArea(id);
                if (area == null) {
                    continue;
                }
                String function = model.getBuildingFunctionFromField(feature);
                Integer surface = model.getBuildingSurfaceFromField(feature);
                model.addFunctionsBasedOnBuildingFunction(area, function, surface);
            }
            // Clean up and release file
            iterator.close();
            store.dispose();
        }
    }

    private static void classifyUnclassified(World world, WorldModel model) {
        for (Area area : model.areas.values()) {
            if (!area.hasAnyFunction()) {
                if (model.buildingTypes.containsKey(area.getId())) {
                    String buildingType = model.buildingTypes.get(area.getId());
                    if (buildingType != null && buildingType.length() > 0) {
                        // Handle areas that are not in the BAG dataset, such as parks
                        model.addFunctionsBasedOnBuildingType(area, buildingType);
                    }
                }
                // In case the getSurface has no assignable function, either in BAG or in its type field, remove it
                if (!area.hasAnyFunction()) {
                    world.getAreas().remove(area);
                }
            }
        }
    }
}
