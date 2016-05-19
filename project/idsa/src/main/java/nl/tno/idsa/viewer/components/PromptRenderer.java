package nl.tno.idsa.viewer.components;

import javax.swing.*;

/**
 * Super-interface for components that can render certain types of parameters and provide GUI
 * elements (i.e. a prompt) to change those parameters.
 */
public interface PromptRenderer<T> {
    public JComponent getLabelComponent();
    public JComponent getUserInputComponent();
    public T getValue();
}
