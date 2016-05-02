package nl.tno.idsa.framework.semantics_base.relations;

/**
 * Created by jongsd on 24-8-15.
 */
// TODO Document.
public abstract class EnablingRelation<F, T> extends SemanticRelation<F, T> {

    public EnablingRelation(Class<? extends F> enabler, Class<? extends T> enabled) {
        super(enabler, enabled, false);
    }

    public Class<? extends F> getEnabler() {
        return getFrom();
    }

    public Class<? extends T> getEnabled() {
        return getTo();
    }

    @Override
    public String toString() {
        return "EnablingRelation{" +
                "enabler=" + getEnabler().getSimpleName() +
                ", enabled=" + getEnabled().getSimpleName() +
                '}';
    }

}
