package nl.tno.idsa.viewer.components;

import nl.tno.idsa.framework.messaging.IProgressObserver;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Show progress.
 */
public class ProgressDialog extends JDialog implements IProgressObserver {

    // One often sees this dialog before anything else.
    static {
        try {
            javax.swing.UIManager.setLookAndFeel(javax.swing.UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            // Ok, apparently not.
        }
    }

    private final JLabel status;
    private final JProgressBar progressBar;

    public ProgressDialog(JFrame owner) {
        super(owner, "Progress");
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);

        JPanel contentPane = new JPanel(new BorderLayout(3, 3));
        contentPane.setBorder(new EmptyBorder(10, 10, 10, 10));
        setContentPane(contentPane);

        status = new JLabel();
        contentPane.add(status, BorderLayout.NORTH);

        progressBar = new JProgressBar(0, 100);
        progressBar.setMinimumSize(new Dimension(400, 10));
        progressBar.setPreferredSize(new Dimension(400, 10));
        contentPane.add(progressBar, BorderLayout.SOUTH);

        setSize(new Dimension(450, 100));
        setLocationRelativeTo(owner);
    }

    @Override
    public void notifyShowProgress(boolean showProgress) {
        setVisible(showProgress);
    }

    @Override
    public void notifyProgress(final double percentage) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                progressBar.setIndeterminate(false);
                int newValue = (int) (100 * percentage);
                if (progressBar.getValue() != newValue) {
                    progressBar.setValue(newValue);
                    progressBar.invalidate();
                }
            }
        });
    }

    @Override
    public void notifyUnknownProgress() {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                progressBar.setIndeterminate(true);
            }
        });
    }

    @Override
    public void notifyProgressMessage(final String message) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                status.setText(message);
            }
        });
    }
}
