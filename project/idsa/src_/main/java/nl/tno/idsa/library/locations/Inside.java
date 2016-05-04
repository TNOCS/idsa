package nl.tno.idsa.library.locations;

import nl.tno.idsa.framework.semantics_base.objects.ParameterId;
import nl.tno.idsa.framework.semantics_impl.locations.LocationFunction;
import nl.tno.idsa.framework.semantics_impl.variables.Variable;

import java.util.Map;

/**
 * Created by jongsd on 11-8-15.
 */
public final class Inside extends LocationFunction {
    @Override
    @SuppressWarnings("unchecked")
    protected Class<? extends LocationFunction>[] getSuperclassArray() {
        return new Class[]{Anywhere.class};
    }

    @Override
    protected void addOwnParameters(Map<ParameterId, Variable> parameterMap) {
        super.addOwnParameters(parameterMap);
    }
}
