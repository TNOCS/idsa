package nl.tno.idsa.viewer.eventsettings;

import javax.swing.*;

/**
 * Created by jongsd on 3-9-15.
 */
// TODO Document class.
public interface EventParameterRendererComponent<T> {
    public JComponent getLabelComponent();

    public JComponent getUserInputComponent();

    public T getValue();
}
