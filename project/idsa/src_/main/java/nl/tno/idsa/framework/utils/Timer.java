package nl.tno.idsa.framework.utils;

// TODO: no longer used

public class Timer {
    private static long time;

    public static void set() {
        time = System.currentTimeMillis();
    }

    public static long get() {
        return System.currentTimeMillis() - time;
    }
}
