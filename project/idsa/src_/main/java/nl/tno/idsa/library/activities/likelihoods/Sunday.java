package nl.tno.idsa.library.activities.likelihoods;

import nl.tno.idsa.framework.behavior.activities.possible.PossibleTimeIntervals;
import nl.tno.idsa.framework.behavior.likelihoods.DayOfWeek;
import nl.tno.idsa.framework.world.Day;
import nl.tno.idsa.framework.world.Time;
import nl.tno.idsa.library.activities.possible.*;

@SuppressWarnings("unused")
public class Sunday extends DayOfWeek {

    public static final Day PROTODAY = new Day(8, 11, 2015);
    private static final int INDEX = PROTODAY.getDayOfWeek(); // This is a Sunday.

    @Override
    public Day getPrototypeDay() {
        return PROTODAY;
    }

    @Override
    public int getIndex() {
        return INDEX;
    }

    @Override
    protected void makeActivities() {

        // Park
        PossibleTimeIntervals time = new PossibleTimeIntervals(new Time(10, 0, 00), new Time(17, 00, 00), 30, 180);
        put(new PossibleBeAtPark(time), 0.05);

        // Sport
        time = new PossibleTimeIntervals(new Time(9, 0, 00), new Time(17, 00, 00), 30, 120);
        put(new PossibleBeAtSportsField(time), 0.1);

        // Playground
        time = new PossibleTimeIntervals(new Time(11, 0, 00), new Time(14, 00, 00), 20, 90);
        put(new PossibleBeAtPlayground(time), 0.1);

        // Groceries
        time = new PossibleTimeIntervals(new Time(12, 0, 00), new Time(18, 00, 00), 10, 60);
        put(new PossibleBeShopping(time), 0.15);

        // Dinner
        put(new PossibleHaveDinnerAtHome(), 0.8);
    }
}