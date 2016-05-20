package nl.tno.idsa.viewer.dialogs;

import nl.tno.idsa.framework.utils.DataSourceFinder;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.List;
import java.util.Vector;

/**
 * Dialog allowing choice between the different data sources.
 */
public class DataSourceSelectionDialog extends JDialog {

    private static final String CAPTION = "Pick data source";

    private boolean dataSourcesPresent;
    private boolean cancelled;
    private DataSourceFinder.DataSource selectedDataSource;

    public DataSourceSelectionDialog(JFrame owner) {
        super(owner, CAPTION, ModalityType.APPLICATION_MODAL);
        createDialog();
    }

    public DataSourceSelectionDialog(JDialog owner) {
        super(owner, CAPTION, ModalityType.APPLICATION_MODAL);
        createDialog();
    }

    private void createDialog() {
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);

        List<DataSourceFinder.DataSource> dataSources;
        DataSourceFinder.DataSource defaultDataSource;
        try {
            dataSources = DataSourceFinder.listDataSources();
            defaultDataSource = dataSources.get(0);
            dataSourcesPresent = true;
        }
        catch(Exception e) {
            dataSourcesPresent = false;
            return;
        }

        JPanel contentPane = new JPanel(new BorderLayout(3, 3));
        contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
        setContentPane(contentPane);

        JPanel top = new JPanel(new BorderLayout(3, 3));
        final JList<DataSourceFinder.DataSource> jListDataSources = new JList<>();
        jListDataSources.setMinimumSize(new Dimension(300,150));
        jListDataSources.setListData(new Vector<>(dataSources)); // Java legacy APIs...
        jListDataSources.setSelectedValue(defaultDataSource, true);
        top.add(new JScrollPane(jListDataSources));
        contentPane.add(top, BorderLayout.CENTER);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.CENTER, 3, 3));
        JButton okButton = new JButton(new AbstractAction("OK") {
            @Override
            public void actionPerformed(ActionEvent e) {
                cancelled = false;
                selectedDataSource = jListDataSources.getSelectedValue();
                dispose();
            }
        });
        bottom.add(okButton);
        JButton cancelButton = new JButton(new AbstractAction("Cancel") {
            @Override
            public void actionPerformed(ActionEvent e) {
                cancelled = true;
                dispose();
            }
        });
        bottom.add(cancelButton);
        contentPane.add(bottom, BorderLayout.SOUTH);

        setLocationRelativeTo(getParent());
        pack();

        setVisible(true);
    }

    public boolean isCancelled() {
        return cancelled;
    }

    public boolean areDataSourcesPresent() {
        return dataSourcesPresent;
    }

    public DataSourceFinder.DataSource getSelectedDataSource() {
        return selectedDataSource;
    }
}
