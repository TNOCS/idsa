package nl.tno.idsa.framework.semantics_base;

import nl.tno.idsa.framework.semantics_base.objects.ParameterId;
import nl.tno.idsa.framework.semantics_base.objects.SemanticObject;
import nl.tno.idsa.framework.semantics_base.objects.SemanticSingleton;
import nl.tno.idsa.framework.semantics_base.relations.SemanticRelation;
import nl.tno.idsa.framework.semantics_impl.variables.Variable;
import nl.tno.idsa.library.locations.ShoppingArea;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by jongsd on 11-8-15.
 */

// TODO Document class.

public enum SemanticLibrary {
    INSTANCE;

    @SuppressWarnings("unchecked")
    public static SemanticLibrary getInstance() {
        return INSTANCE;
    }

    private HashMap<Class<? extends SemanticObject>, SemanticObject> classToInstance = new HashMap<>();
    private HashMap<Class<? extends SemanticObject>, Set<Class<? extends SemanticObject>>> classToSuperclasses = new HashMap<>();

    private Set<SemanticRelation> relations = new HashSet<>(); // For now we do a linear search here.

    private HashMap<Integer, Object> cache = new HashMap<>(); // TODO Start removing cache items if we run out of memory.
    private int cacheHits;

    @SuppressWarnings("unchecked")
    private SemanticLibrary() {

        // Already done?
        if (classToInstance.size() > 0) {
            return;
        }

        // No, do it.
        Set<Class<? extends SemanticObject>> classes = JavaSubclassFinder.listSubclasses(SemanticObject.class);
        for (Class<? extends SemanticObject> clazz : classes) {
            try {

                // Instance of class.
                SemanticObject instance = newInstance(clazz);
                classToInstance.put(clazz, instance);

                // Relations are kept separately.
                if (instance instanceof SemanticRelation) {
                    relations.add((SemanticRelation) instance);
                }
            } catch (InstantiationException e) {
                // Ok, so this class cannot be instantiated. It is probably abstract.
            }
        }

        // Superclass relations.
        for (Class<? extends SemanticObject> clazz : classes) {
            SemanticObject instance = classToInstance.get(clazz);
            if (instance != null) {
                Set instanceSuperclasses = instance.getSemanticSuperclasses(); // Semantic object method call.
                classToSuperclasses.put(instance.getClass(), instanceSuperclasses);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private <T extends SemanticObject> Set<T> createSubclassInstances(Class<? extends T> baseClass) {
        HashSet<T> instances = new HashSet<T>();
        Set classes = JavaSubclassFinder.listSubclasses(baseClass); // Should be Set<Class<? extends T>>, but Java and generics...
        for (Object clazz : classes) {
            try {
                T instance = (T) newInstance((Class) clazz); // And now we need two unchecked casts. Thank you, Java.
                instances.add(instance);
            } catch (Exception e) {
                // Yeah, no.
            }
        }
        return instances;
    }

    /**
     * Returns a prototype instance for the given semantic class. Note that this does not create a new instance,
     * it merely returns the default-constructor instance that was created when the library was filled.
     */
    @SuppressWarnings("unchecked")
    public <T extends SemanticObject> T getSemanticPrototypeInstance(Class<? extends T> semanticClass) throws InstantiationException {
        T instance = (T) classToInstance.get(semanticClass);
        if (instance == null) {
            throw new InstantiationException("Cannot return instance of semantic class " + semanticClass.getSimpleName());
        }
        return instance;
    }

    /**
     * Returns a new instance for the given semantic class. For singletons, this throws an exception.
     */
    @SuppressWarnings("unchecked")
    public <T extends SemanticObject> T createSemanticInstance(Class<? extends T> semanticClass) throws InstantiationException {
        return newInstance(semanticClass);
    }

    // Helper method. Centralized here because we might later change e.g. the default constructor of semantic objects.
    private <T extends SemanticObject> T newInstance(Class<? extends T> semanticClass) throws InstantiationException {
        if (SemanticSingleton.class.isAssignableFrom(semanticClass) && classToInstance.get(semanticClass) != null) {
            throw new InstantiationException("Cannot instantiate singleton twice!");
        }
        try {
            return semanticClass.newInstance();
        } catch (Exception e) {
            throw new InstantiationException("Cannot instantiate semantic class '" + semanticClass.getSimpleName() + "': " + e.getMessage());
        }
    }

    /**
     * Lists the parameters for a given class.
     */
    public Set<ParameterId> getParameters(Class<? extends SemanticObject> semanticClass) {
        SemanticObject semanticObject = classToInstance.get(semanticClass);
        if (semanticObject != null) {
            return semanticObject.getParameters().keySet();
        }
        return null;
    }

    /**
     * Gets a default parameter value. This returns a value iff the semantic class, or any superclass, defines the
     * parameter and assigns a value to it in that definition.
     */
    @SuppressWarnings("unchecked")
    public Object getDefaultParameterValue(Class<? extends SemanticObject> semanticClass, ParameterId parameterId) {
        SemanticObject semanticObject = classToInstance.get(semanticClass);
        if (semanticObject != null) {
            Variable variable = semanticObject.getParameters().get(parameterId);
            if (variable != null && variable.getValue() != null) {
                return variable.getValue();
            } else {
                // Recurse to superclasses.
                Set superclasses = semanticObject.getSemanticSuperclasses();
                if (superclasses != null) {
                    for (Object o : superclasses) {
                        Object superValue = getDefaultParameterValue((Class<? extends SemanticObject>) o, parameterId);  // unchecked cast
                        if (superValue != null) {
                            return superValue;
                        }
                    }
                }
            }
        }
        return null;
    }

    /**
     * List all superclasses of the semantic class.
     */
    public <T extends SemanticObject> Set<Class<? extends T>> listSemanticSuperclasses(Class<? extends T> semanticClass) {
        Set<Class<? extends T>> result = new HashSet<>();
        listSemanticSuperclasses(semanticClass, result);
        return result;
    }

    @SuppressWarnings("unchecked")
    private <T extends SemanticObject> void listSemanticSuperclasses(Class<? extends T> semanticClass, Set<Class<? extends T>> into) {
        try {
            SemanticObject<T> semanticPrototypeInstance = getSemanticPrototypeInstance(semanticClass); // Unchecked.
            Set<Class<? extends T>> semanticSuperclasses = semanticPrototypeInstance.getSemanticSuperclasses();
            if (semanticSuperclasses != null) {
                for (Class<? extends T> semanticSuperclass : semanticSuperclasses) {
                    if (!into.contains(semanticSuperclass)) {
                        into.add(semanticSuperclass);
                        listSemanticSuperclasses(semanticSuperclass, into);
                    }
                }
            }
        } catch (Exception e) {
            // Do nothing.
        }
    }

    /**
     * List all subclasses of the semantic base class.
     */
    @SuppressWarnings("unchecked")
    public <B extends SemanticObject> Set<Class<? extends B>> listSemanticSubclasses(Class<? extends B> semanticBaseClass) {
        // List all subclasses.
        Class clz = getJavaAncestor(semanticBaseClass); // Without this, the code does not compile. Java generics are really, utterly stupid.
        Set<Class<? extends B>> subclasses = new HashSet<>();
        subclasses.add(semanticBaseClass);
        Set<Class<? extends B>> allClasses = JavaSubclassFinder.listSubclasses(clz); // This is an unchecked cast because of the clz variable.
        for (Class<? extends B> cls : allClasses) {
            if (SemanticLibrary.getInstance().isSemanticSubclass(semanticBaseClass, cls)) {
                subclasses.add(cls);
            }
        }
        return subclasses;
    }

    /**
     * Returns the topmost class (below SemanticObject) that the given class inherits from in Java.
     */
    public Class getJavaAncestor(Class<? extends SemanticObject> semanticBaseClass) {
        // Limit the search space a bit.
        Class javaBaseClass = semanticBaseClass;
        while (!javaBaseClass.getSuperclass().equals(SemanticObject.class)) {
            javaBaseClass = javaBaseClass.getSuperclass();
            if (javaBaseClass == null) {
                return null;
            }
        }
        return javaBaseClass;
    }

    /**
     * Returns true if the potentialSuperclass is indeed a superclass of the potentialSubclass. Note, we do not mean in Java object
     * hierarchy, but rather by means of the getSuperclasses method in ISemanticObject, which allows for multiple
     * inheritance. Semantic objects should not be inherited according to Java 'extends' constructs.
     */
    public boolean isSemanticSubclass(Class potentialSuperclass, Class potentialSubclass) {

        boolean isSemanticSuperclass = isSemanticSuperclass(potentialSubclass, potentialSuperclass);

        if (potentialSubclass == ShoppingArea.class) { // TODO REMOVE!
            System.out.println("SUPER " + potentialSuperclass + " SUB " + potentialSubclass + "? " + isSemanticSuperclass);
        }

        return isSemanticSuperclass;
    }

    /**
     * Returns true if the potential subclass is indeed a subclass of the potential superclass.
     * Note, we do not mean in Java object hierarchy, but rather by means of the getSuperClasses method
     * in ISemanticObject, which allows for multiple inheritance. Semantic objects should not be
     * inherited according to Java 'extends' constructs.
     */
    @SuppressWarnings("unchecked")
    public boolean isSemanticSuperclass(Class potentialSubclass, Class potentialSuperclass) {

        // Null is invalid.
        if (potentialSuperclass == null || potentialSubclass == null) {
            return false;
        }

        // Some classes may use Java hierarchy.
        if (potentialSuperclass.isAssignableFrom(potentialSubclass)) {
            return true;
        }

        // Cache lookup.
        Object cached = getCachedValue(potentialSubclass, potentialSuperclass);
        if (cached != null) {
            return (Boolean) cached;
        }

        boolean returnValue;

        // Check whether we know this class.
        SemanticObject instance = classToInstance.get(potentialSubclass);
        SemanticObject potentialSuperInstance = classToInstance.get(potentialSuperclass);
        if (instance == null || potentialSuperInstance == null) {
            returnValue = false;
        } else {
            // Return whether the instances are (in-)directly related as sub-/super-type.
            returnValue = isSemanticSuperclassR(potentialSubclass, potentialSuperclass);
        }

        // Cache.
        addToCache(returnValue, potentialSubclass, potentialSuperclass);

        // Return.
        return returnValue;
    }

    // Recursive search through the class hierarchy.
    private boolean isSemanticSuperclassR(Class<? extends SemanticObject> clazz, Class<? extends SemanticObject> potentialSuperClass) {

        // Termination condition for recursive method.
        if (clazz.equals(potentialSuperClass)) {
            return true;
        }

        // Determine the superclass instances of the instance.
        Set<Class<? extends SemanticObject>> superClasses = classToSuperclasses.get(clazz);
        if (superClasses == null) {
            return false;
        }

        // Recursion on the superclasses, search towards the potential superclass.
        for (Class<? extends SemanticObject> superClass : superClasses) {
            if (isSemanticSuperclass(superClass, potentialSuperClass)) {
                return true;
            }
        }

        // Nothing was found here or further below.
        return false;
    }

    public int getInheritanceDistance(Class<? extends SemanticObject> superclass, Class<? extends SemanticObject> subclass) {
        return getInheritanceDistanceR(superclass, subclass);
    }

    @SuppressWarnings("unchecked")
    private int getInheritanceDistanceR(Class<? extends SemanticObject> superclass, Class<? extends SemanticObject> subclass) {
        if (superclass == null || subclass == null) {
            return Integer.MAX_VALUE;
        }
        if (superclass.equals(subclass)) {
            return 0;
        }
        SemanticObject subInstance = classToInstance.get(subclass);
        if (subInstance == null) {
            throw new Error("Unknown class " + subclass.getSimpleName() + ".");
        }
        Set<Class<? extends SemanticObject>> superclasses = subInstance.getSemanticSuperclasses(); // This is an unchecked conversion.
        if (superclasses != null) {
            int minDist = Integer.MAX_VALUE;
            for (Class<? extends SemanticObject> sup : superclasses) {
                int dist = getInheritanceDistanceR(superclass, sup);
                if (dist < minDist) {
                    minDist = dist;
                }
            }
            if (minDist == Integer.MAX_VALUE) {
                minDist--; // Make sure we can add one without this becoming negative.
            }
            return minDist + 1;
        }
        return Integer.MAX_VALUE;
    }


    @SuppressWarnings("unchecked")
    public <F, T> Set<SemanticRelation<F, T>> getSemanticRelationsFrom(Class<? extends F> origin, Class<? extends SemanticRelation<F, T>> relationClass) {

        // Cached?
        Object cachedValue = getCachedValue(origin, relationClass);
        if (cachedValue != null) {
            return (Set<SemanticRelation<F, T>>) cachedValue;
        }

        // Create set.
        HashSet<SemanticRelation<F, T>> result = new HashSet<>();
        for (SemanticRelation semanticRelation : relations) {
            if (isSemanticSuperclass(semanticRelation.getClass(), relationClass)) {
                if (isSemanticSubclass(semanticRelation.getFrom(), origin)) {
                    result.add((SemanticRelation<F, T>) semanticRelation); // Unchecked cast.
                }
            }
        }

        // Cache and return.
        addToCache(result, origin, relationClass);
        return result;
    }

    @SuppressWarnings("unchecked")
    public <F, T> Set<SemanticRelation<F, T>> getSemanticRelationsTo(Class<? extends SemanticRelation<F, T>> relationClass, Class<? extends T> target) {

        // Cached?
        Object cachedValue = getCachedValue(relationClass, target);
        if (cachedValue != null) {
            return (Set<SemanticRelation<F, T>>) cachedValue;
        }

        HashSet<SemanticRelation<F, T>> result = new HashSet<>();
        for (SemanticRelation semanticRelation : relations) {
            if (isSemanticSuperclass(semanticRelation.getClass(), relationClass)) {
                if (isSemanticSuperclass(semanticRelation.getTo(), target)) {
                    result.add((SemanticRelation<F, T>) semanticRelation); // Unchecked cast.
                }
            }
        }

        // Cache and return.
        addToCache(result, relationClass, target);
        return result;
    }

    // TODO The library has a rudimentary cache mechanism to (theoretically) speed up repeated look-ups. ...
    // At the moment, only the library itself uses it, saving ~80K look-ups on a small plan plus sampling.
    // We can use the cache elsewhere (i.e. for operations AROUND the library), but also, we need to make
    // sure that the cache does not become far too big (or far too slow due to its size).

    private int createCacheKey(Object key1, Object... objects) {
        int result = key1.hashCode();
        for (Object o : objects) {
            long temp = Double.doubleToLongBits(o.hashCode());
            result = 31 * result + (int) (temp ^ (temp >>> 32));
        }
        return result;
    }

    public Object getCachedValue(Object key1, Object... key) {
        Object o = cache.get(createCacheKey(key1, key));
        if (o != null) {
            cacheHits++;
        }
        return o;
    }

    public void addToCache(Object data, Object key1, Object... key) {
        cache.put(createCacheKey(key1, key), data);
    }

    public int getCacheHits() {
        return cacheHits;
    }
}