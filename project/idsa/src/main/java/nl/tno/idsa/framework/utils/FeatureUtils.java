package nl.tno.idsa.framework.utils;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.MultiPolygon;
import nl.tno.idsa.framework.world.Point;
import nl.tno.idsa.framework.world.Polygon;
import org.geotools.data.FileDataStore;
import org.geotools.data.FileDataStoreFinder;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureSource;
import org.opengis.feature.Feature;
import org.opengis.feature.GeometryAttribute;
import org.opengis.feature.Property;

import java.io.File;
import java.io.IOException;

public class FeatureUtils {

    public static FileDataStore loadFeatures(String shpFilename) {
        File file = new File(shpFilename);
        FileDataStore store = null;
        try {
            store = FileDataStoreFinder.getDataStore(file);
        } catch (IOException e) {
            System.out.format("IOException while loading %s: %s", shpFilename, e.getMessage());       // TODO logger.
        }
        return store;
    }

    public static SimpleFeatureCollection getFeatures(FileDataStore store) {
        SimpleFeatureCollection features = null;
        if (store != null) {
            try {
                SimpleFeatureSource featureSource = store.getFeatureSource();
                features = featureSource.getFeatures();
            } catch (IOException e) {
                System.out.format("IOException while loading %s: %s", store, e.getMessage());    // TODO Logger.
            }
        }
        return features;
    }

    @SuppressWarnings("unchecked")
    public static <T> T getFieldValue(Feature feature, String fieldName) {
        T result = null;
        Property p = feature.getProperty(fieldName);
        if (p != null) {
            Object o = p.getValue();
            if (o != null) {
                try {
                    result = (T) o; // Unchecked.
                } catch (ClassCastException c) {
                    result = null;
                }
            }
        }
        return result;
    }

    public static Polygon convertPolygon(Feature feature, Point root) {
        GeometryAttribute attribute = feature.getDefaultGeometryProperty();
        MultiPolygon multiPolygon = (MultiPolygon) attribute.getValue();
        Coordinate[] coordinates = multiPolygon.getCoordinates();
        Point[] points = new Point[coordinates.length];
        for (int i = 0; i < coordinates.length; ++i) {
            Coordinate c = coordinates[i];
            Point point = new Point(c.x - root.getX(), c.y - root.getY());
            points[i] = point;
        }
        return new Polygon(points);
    }

    private FeatureUtils() {
        // No instances
    }
}
