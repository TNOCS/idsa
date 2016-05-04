package nl.tno.idsa.framework.messaging;

import java.util.ArrayList;

/**
 * Simple progress notification that roughly follows the observer pattern.
 */
public class ProgressNotifier {

    private static final ArrayList<IProgressObserver> observers = new ArrayList<>();

    public static void addObserver(IProgressObserver observer) {
        observers.add(observer);
    }

    public static void removeObserver(IProgressObserver observer) {
        observers.remove(observer);
    }

    public static void removeObservers() {
        observers.clear();
    }

    public static void notifyShowProgress(boolean showProgress) {
        for (IProgressObserver observer : observers) {
            observer.notifyShowProgress(showProgress);
        }
    }

    public static void notifyProgress(double percentage) {
        for (IProgressObserver observer : observers) {
            observer.notifyProgress(percentage);
        }

    }

    public static void notifyProgressMessage(String message) {
        for (IProgressObserver observer : observers) {
            observer.notifyProgressMessage(message);
        }
    }
}
