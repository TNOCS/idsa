package nl.tno.idsa.viewer.eventsettings;

import nl.tno.idsa.framework.semantics_impl.variables.*;

/**
 * Created by jongsd on 3-9-15.
 */
// TODO Document class.
public class VariablePromptRendererFactory {

    private static final VariablePromptRendererFactory instance = new VariablePromptRendererFactory();

    public static VariablePromptRendererFactory getInstance() {
        return instance;
    }

    @SuppressWarnings("unchecked")
    public EventParameterRendererComponent getRenderer(String variableDescription, Variable variable) {
        if (variable instanceof IntegerVariable) {
            return new IntegerVariablePromptRenderer(variableDescription, (IntegerVariable) variable);
        } else if (variable instanceof DoubleVariable) {
            return new DoubleVariablePromptRenderer(variableDescription, (DoubleVariable) variable);
        } else if (variable instanceof LocationVariable) {
            return new LocationVariablePromptRenderer(variableDescription, (LocationVariable) variable);
        } else if (variable instanceof RoleVariable || variable instanceof ClassVariable) {
            return new ClassVariablePromptRenderer(variableDescription, variable);    // This is unchecked. Says java.
        } else if (variable instanceof ModelVariable) {
            return new ModelVariablePromptRenderer(variableDescription, (ModelVariable) variable);
        } else {
            return null;
        }
    }
}
