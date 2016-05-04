package nl.tno.idsa.framework.messaging;

/**
 * Simple interface for message receiving.
 */
public interface IMessageObserver {
    public void receive(String message);
}
