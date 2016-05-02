package nl.tno.idsa.framework.world;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

// TODO Document class.

public enum GeometryType {
    POINT(1), POLYLINE(2), POLYGON(3);

    private final int level;

    GeometryType(int level) {
        this.level = level;
    }

    public GeometryType leastRestrictive(GeometryType other) {
        return other.level > level ? other : this;
    }

    public static Set<GeometryType> asSet(GeometryType... types) {
        return new HashSet<GeometryType>(Arrays.asList(types));
    }
}
