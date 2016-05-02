package nl.tno.idsa.viewer.utils;

import nl.tno.idsa.framework.agents.Agent;
import nl.tno.idsa.framework.semantics_base.SemanticLibrary;
import nl.tno.idsa.framework.semantics_impl.roles.Role;

import java.awt.*;

/**
 * Created by jongsd on 9-10-15.
 */
// TODO Document class.
public class AgentColorUtil {

    private static final Color COLOR_IN_EVENT = new Color(128, 0, 128, 255);

    private static final Color COLOR_CIV_STANDING = new Color(156, 156, 156, 128);

    public static Color getAgentColor(Agent a) {
        Color source = COLOR_CIV_STANDING;
        if (a.getRole() != null) {
            Object classParameterValue = SemanticLibrary.getInstance().getDefaultParameterValue(a.getRole(), Role.Parameters.COLOR);
            if (classParameterValue != null) {
                source = new Color((int) classParameterValue); // TODO Alpha
            }
        } else if (a.isPartOfEvent()) {
            source = COLOR_IN_EVENT;
        }
        return source;
    }

}
