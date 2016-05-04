package nl.tno.idsa.library.activities.likelihoods;

import nl.tno.idsa.framework.behavior.activities.possible.PossibleTimeIntervals;
import nl.tno.idsa.framework.behavior.likelihoods.DayOfWeek;
import nl.tno.idsa.framework.world.Day;
import nl.tno.idsa.framework.world.Time;
import nl.tno.idsa.library.activities.possible.*;

@SuppressWarnings("unused")
public class Monday extends DayOfWeek {

    public static final Day PROTODAY = new Day(26, 10, 2015);
    private static final int INDEX = PROTODAY.getDayOfWeek(); // This is a Monday.

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

        //work
        PossibleTimeIntervals time = new PossibleTimeIntervals(new Time(7, 00, 00), new Time(18, 00, 00), 180, 600);
        put(new PossibleBeAtWork(time), 0.9);

        //school
        time = new PossibleTimeIntervals(new Time(8, 30, 00), new Time(15, 15, 00), 300, 405);
        put(new PossibleBeAtSchool(time), 1d);

        //Dinner
        put(new PossibleHaveDinnerAtHome(), 0.85); //TODO No time interval here.

        //groceries
        time = new PossibleTimeIntervals(new Time(8, 0, 0), new Time(20, 0), 5, 2 * 60);
        put(new PossibleBeShopping(time), 0.5);

        //sport
        time = new PossibleTimeIntervals(new Time(8, 0, 0), new Time(20, 0, 0), 15, 3 * 60);
        put(new PossibleBeAtSportsField(time), 0.1);

        // Hang around at square
        time = new PossibleTimeIntervals(new Time(10, 0, 00), new Time(16, 00, 00), 30, 120);
        put(new PossibleHangAroundOnSquare(time), 0.25);

        //play at a playground
        time = new PossibleTimeIntervals(new Time(8, 0, 0), new Time(20, 0, 0), 15, 3 * 60);
        put(new PossibleBeAtPlayground(time), 0.2);
    }
}
