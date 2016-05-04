package nl.tno.idsa.viewer.components;

import nl.tno.idsa.framework.world.Time;

import javax.swing.*;
import java.awt.*;

/**
 * Created by jongsd on 3-9-15.
 */
// TODO Document class.
public class TimeSetterPanel extends JPanel {

    private final JSpinner hourSpinner, minuteSpinner, secondSpinner;

    public TimeSetterPanel(Time currentTime) {
        setLayout(new GridLayout(1, 3, 3, 3));

        SpinnerNumberModel hourModel = new SpinnerNumberModel(currentTime.getHour(), 0, 23, 1);
        hourSpinner = new JSpinner(hourModel);
        add(hourSpinner);

        SpinnerNumberModel minuteModel = new SpinnerNumberModel(currentTime.getMinute(), 0, 59, 1);
        minuteSpinner = new JSpinner(minuteModel);
        add(minuteSpinner);

        SpinnerNumberModel secondModel = new SpinnerNumberModel(currentTime.getSecond(), 0, 59, 1);
        secondSpinner = new JSpinner(secondModel);
        add(secondSpinner);
    }

    public Time getValue() {
        return new Time((int) hourSpinner.getValue(), (int) minuteSpinner.getValue(), (int) secondSpinner.getValue());
    }
}
