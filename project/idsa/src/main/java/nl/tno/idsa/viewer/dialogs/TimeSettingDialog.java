package nl.tno.idsa.viewer.dialogs;

import nl.tno.idsa.framework.world.Environment;
import nl.tno.idsa.viewer.components.TimeSetterPanel;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;

/**
 * Dialog to set a time value for a given environment.
 */
public class TimeSettingDialog extends JDialog {
    public TimeSettingDialog(JFrame owner, final Environment environment) {
        super(owner, "Set new current time");
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE); // For consistency with other dialogs.

        JPanel contentPane = new JPanel(new BorderLayout(3, 3));
        contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
        setContentPane(contentPane);

        JPanel top = new JPanel(new BorderLayout(3, 3));
        JLabel timeLabel = new JLabel("New current time");
        top.add(timeLabel, BorderLayout.WEST);
        final TimeSetterPanel timeSetterPanel = new TimeSetterPanel(environment.getTime());
        top.add(timeSetterPanel, BorderLayout.CENTER);
        contentPane.add(top, BorderLayout.CENTER);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.CENTER, 3, 3));
        JButton okButton = new JButton(new AbstractAction("OK") {
            @Override
            public void actionPerformed(ActionEvent e) {
                environment.setTime(timeSetterPanel.getValue());
                dispose();
            }
        });
        bottom.add(okButton);
        JButton cancelButton = new JButton(new AbstractAction("Cancel") {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });
        bottom.add(cancelButton);
        contentPane.add(bottom, BorderLayout.SOUTH);

        setLocationRelativeTo(getParent());
        pack();

        setVisible(true);
    }
}
