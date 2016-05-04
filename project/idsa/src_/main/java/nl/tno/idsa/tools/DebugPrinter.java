package nl.tno.idsa.tools;

/**
 * Very simple debug logger.
 */
public class DebugPrinter {
    private static boolean DEBUG = false;

    public static void println(String format, Object... args) {
        if (DEBUG) {
            System.out.println(String.format(format, args));
        }
    }

    public static void println(String x) {
        if (DEBUG) {
            System.out.println(x);
        }
    }
}
