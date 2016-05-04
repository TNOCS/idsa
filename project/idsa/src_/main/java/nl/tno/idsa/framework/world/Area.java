package nl.tno.idsa.framework.world;

import nl.tno.idsa.framework.semantics_base.SemanticLibrary;
import nl.tno.idsa.framework.semantics_impl.locations.LocationFunction;

import java.util.ArrayList;
import java.util.List;

// TODO Document class.

public class Area {

    private final long id;

    private final List<LocationFunction> functions;
    private final List<Vertex> vertices;
    private final Polygon area;
    private final String type;

    public Area(long id, Polygon area, String type) {
        this.area = area;
        this.id = id;
        this.type = type;
        this.vertices = new ArrayList<>();
        this.functions = new ArrayList<>();
    }

    public String getType() {
        return type;
    }

    public Polygon getPolygon() {
        return area;
    }

    public boolean contains(Point p) {
        return area.contains(p);
    }

    public List<Vertex> getVertices() {
        return vertices;
    }

    public List<LocationFunction> getFunctions(Class<? extends LocationFunction> function) {
        ArrayList<LocationFunction> result = new ArrayList<>();
        for (LocationFunction ownFunction : functions) {
            if (SemanticLibrary.getInstance().isSemanticSubclass(function, ownFunction.getClass())) {
                result.add(ownFunction);
            }
        }
        return result;
    }

    public List<LocationFunction> getFunctions() {
        return functions;
    }

    public boolean addFunction(LocationFunction function) {
        if (functions.contains(function)) {
            return false;
        }
        functions.add(function);
        return true;
    }

    public LocationFunction getFunction(Class<? extends LocationFunction> functionBaseClass) {
        for (LocationFunction ownFunction : functions) {
            // A shop is a shopping area, but a shopping area is not a shop.
            if (SemanticLibrary.getInstance().isSemanticSubclass(functionBaseClass, ownFunction.getClass())) {
                return ownFunction;
            }
        }
        return null;
    }

    public boolean hasAnyFunction() {
        return !functions.isEmpty();
    }

    @SafeVarargs
    public final boolean hasAnyFunction(Class<? extends LocationFunction>... requiredFunctions) {
        if (requiredFunctions == null || requiredFunctions.length == 0) {
            return true;
        }
        for (Class<? extends LocationFunction> function : requiredFunctions) {
            if (hasFunction(function)) {
                return true;
            }
        }
        return false;
    }

    @SafeVarargs
    public final boolean hasAllFunctions(Class<? extends LocationFunction>... requiredFunctions) {
        if (requiredFunctions == null || requiredFunctions.length == 0) {
            return true;
        }
        for (Class<? extends LocationFunction> function : requiredFunctions) {
            if (!hasFunction(function)) {
                return false;
            }
        }
        return true;
    }

    public boolean hasFunction(Class<? extends LocationFunction> function) {
        for (LocationFunction ownFunction : functions) {
            // A shop is a shopping area, but a shopping area is not a shop.
            if (SemanticLibrary.getInstance().isSemanticSubclass(function, ownFunction.getClass())) {
                return true;
            }
        }
        return false;
    }

    public long getId() {
        return id;
    }

    public Point get(int i) {
        return area.get(i);
    }

    public int numberOfVertices() {
        return area.size();
    }

    public void addVertex(Vertex v) {
        assert !vertices.contains(v);
        this.vertices.add(v);
    }

    public double getSurface() {
        return area.getSurface();
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 37 * hash + (int) (this.id ^ (this.id >>> 32));
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Area other = (Area) obj;
        if (this.id != other.id) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        String output = "";
        for (LocationFunction f : functions) {
            output += f.toString() + ", ";
        }
        return output;
    }
}
