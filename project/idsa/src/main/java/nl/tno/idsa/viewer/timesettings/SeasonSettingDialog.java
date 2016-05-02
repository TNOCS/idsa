package nl.tno.idsa.viewer.timesettings;

import nl.tno.idsa.framework.agents.Agent;
import nl.tno.idsa.framework.behavior.likelihoods.DayOfWeek;
import nl.tno.idsa.framework.behavior.multipliers.ISeason;
import nl.tno.idsa.framework.behavior.multipliers.ITimeOfYear;
import nl.tno.idsa.framework.semantics_base.enumerations.RuntimeEnum;
import nl.tno.idsa.framework.utils.TextUtils;
import nl.tno.idsa.framework.world.Day;
import nl.tno.idsa.framework.world.Environment;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.Vector;
import nl.tno.idsa.framework.behavior.likelihoods.ActivityLikelihoodMap;

// TODO Document class.
public class SeasonSettingDialog extends JDialog {
    public SeasonSettingDialog(JFrame owner, final Environment environment) {
        super(owner, "Set season and time of the year");

        boolean promptMakesSense = false;

        JPanel contentPane = new JPanel(new BorderLayout(3, 3));
        contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
        setContentPane(contentPane);

        JPanel top = new JPanel(new BorderLayout(3, 3));
        JPanel topLeft = new JPanel(new GridLayout(0, 1, 3, 3));
        top.add(topLeft, BorderLayout.WEST);
        JPanel topRight = new JPanel(new GridLayout(0, 1, 3, 3));
        top.add(topRight, BorderLayout.CENTER);
        contentPane.add(top, BorderLayout.CENTER);

        Vector<DayOfWeek> days = new Vector<>(RuntimeEnum.getInstance(DayOfWeek.class).listOptions());
        final JComboBox<DayOfWeek> daySelector = new JComboBox<>(days);
        daySelector.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                String representation = TextUtils.camelCaseToText(value.getClass().getSimpleName());
                return super.getListCellRendererComponent(list, representation, index, isSelected, cellHasFocus);
            }
        });
        if (days.size() > 1) {
            topLeft.add(new JLabel("Day"));
            topRight.add(daySelector);
            promptMakesSense = true;
        }

        Vector<ISeason> seasons = new Vector<>(RuntimeEnum.getInstance(ISeason.class).listOptions());
        UnspecifiedSeason unspecifiedSeason = new UnspecifiedSeason();
        seasons.set(0, unspecifiedSeason);
        final JComboBox<ISeason> seasonSelector = new JComboBox<>(seasons);
        seasonSelector.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                String representation = TextUtils.camelCaseToText(value.getClass().getSimpleName());
                return super.getListCellRendererComponent(list, representation, index, isSelected, cellHasFocus);
            }
        });
        seasonSelector.setSelectedItem(unspecifiedSeason);
        if (seasons.size() > 1) {
            topLeft.add(new JLabel("Season"));
            topRight.add(seasonSelector);
            promptMakesSense = true;
        }

        Vector<ITimeOfYear> timesOfYear = new Vector<>(RuntimeEnum.getInstance(ITimeOfYear.class).listOptions());
        UnspecifiedTimeOfYear unspecifiedTimeOfTheYear = new UnspecifiedTimeOfYear();
        timesOfYear.set(0, unspecifiedTimeOfTheYear);
        final JComboBox<ITimeOfYear> timesOfYearSelector = new JComboBox<>(timesOfYear);
        timesOfYearSelector.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                String representation = TextUtils.camelCaseToText(value.getClass().getSimpleName());
                return super.getListCellRendererComponent(list, representation, index, isSelected, cellHasFocus);
            }
        });
        timesOfYearSelector.setSelectedItem(unspecifiedTimeOfTheYear);
        if (timesOfYear.size() > 1) {
            topLeft.add(new JLabel("Time of the year"));
            topRight.add(timesOfYearSelector);
            promptMakesSense = true;
        }

        if (!promptMakesSense) {
            JOptionPane.showMessageDialog(getParent(), "There are no seasons or times of the year in the library.", "Unsupported", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.CENTER, 3, 3));
        JButton okButton = new JButton(new AbstractAction("OK") {
            @Override
            public void actionPerformed(ActionEvent e) {
                DayOfWeek day = (DayOfWeek) daySelector.getSelectedItem();
                Day dday = (day != null) ? day.getPrototypeDay() : environment.getDay();
                ISeason season = (ISeason) seasonSelector.getSelectedItem();
                if (season.getClass() == UnspecifiedSeason.class) {
                    season = null;
                }
                ITimeOfYear timeOfTheYear = (ITimeOfYear) timesOfYearSelector.getSelectedItem();
                if (timeOfTheYear.getClass() == UnspecifiedTimeOfYear.class) {
                    timeOfTheYear = null;
                }
                environment.initializePopulation(season, timeOfTheYear,
                        dday, environment.getTime(), true);
                dispose();
                // TODO Add some kind of waiting dialog box.
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

    private class UnspecifiedSeason implements ISeason {
        @Override
        public void applyMultipliers(Agent agent, ActivityLikelihoodMap agentPossibilities) {

        }

        @Override
        public int getIndex() {
            return -1;
        }
    }

    private class UnspecifiedTimeOfYear implements ITimeOfYear {
        @Override
        public void applyMultipliers(Agent agent, ActivityLikelihoodMap agentPossibilities) {

        }

        @Override
        public int getIndex() {
            return -1;
        }
    }
}
