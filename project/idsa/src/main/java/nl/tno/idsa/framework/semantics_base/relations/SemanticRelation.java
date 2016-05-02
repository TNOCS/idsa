package nl.tno.idsa.framework.semantics_base.relations;

import nl.tno.idsa.framework.semantics_base.objects.SemanticObject;

import java.util.Set;

/**
 * Created by jongsd on 24-8-15.
 */
public abstract class SemanticRelation<F, T> extends SemanticObject<SemanticRelation<F, T>> {

    private Class<? extends F> from;
    private Class<? extends T> to;
    private boolean isSymmetrical = false;

    public SemanticRelation(Class<? extends F> from, Class<? extends T> to, boolean isSymmetrical) {
        this.from = from;
        this.to = to;
        this.isSymmetrical = isSymmetrical;
    }

    public Class<? extends F> getFrom() {
        return from;
    }

    public Class<? extends T> getTo() {
        return to;
    }

    public boolean isSymmetrical() {
        return isSymmetrical;
    }

    // TODO Think; at the moment, relations between two objects cannot be sub-classed into a more specific relation. ...
    // If we want this to be implemented, the lookup in SemanticLibrary needs to change, because we need to list not
    // only e.g. all instances of the "ActionEnablesRole" relation (using Java reflection), but we also need to look
    // at all semantic subclasses of a relation and return all these instances as well.
    @Override
    public final Set<Class<? extends SemanticRelation<F, T>>> getSemanticSuperclasses() {
        return null;
    }

    @Override
    public String toString() {
        return "SemanticRelation{" +
                "from=" + from +
                ", to=" + to +
                ", isSymmetrical=" + isSymmetrical +
                '}';
    }
}
