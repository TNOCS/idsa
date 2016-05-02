package nl.tno.idsa.framework.world;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;

/**
 * Wrapper class around the Java Calendar class.
 */
// TODO Document class.
public class Day {

    private final Calendar calendar;
    private final SimpleDateFormat sdf = new SimpleDateFormat("EEEE dd MMMM yyyy", Locale.US);

    public Day(int day, int month, int year) {
        this.calendar = new GregorianCalendar(Locale.US);
        this.calendar.set(Calendar.YEAR, year);
        this.calendar.set(Calendar.DAY_OF_MONTH, day);
        this.calendar.set(Calendar.MONTH, month - 1);
        this.calendar.setFirstDayOfWeek(Calendar.MONDAY);
    }

    public int getDayOfWeek() {
        return this.calendar.get(Calendar.DAY_OF_WEEK);
    }

    public int getDay() {
        return this.calendar.get(Calendar.DAY_OF_MONTH);
    }

    public int getMonth() {
        return this.calendar.get(Calendar.MONTH);
    }

    public int getYear() {
        return this.calendar.get(Calendar.YEAR);
    }

    @Override
    public String toString() {
        return sdf.format(calendar.getTime());
    }
}
