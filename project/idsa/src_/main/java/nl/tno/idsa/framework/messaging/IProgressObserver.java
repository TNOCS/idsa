package nl.tno.idsa.framework.messaging;

/**
 * Simple interface for progress message receiving.
 */
public interface IProgressObserver {
    public void notifyShowProgress(boolean showProgress);

    public void notifyProgress(double percentage);

    public void notifyProgressMessage(String message);
}
