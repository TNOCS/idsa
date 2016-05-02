package nl.tno.idsa.framework.messaging;

import nl.tno.idsa.framework.agents.Agent;
import nl.tno.idsa.framework.world.Environment;

import java.util.ArrayList;
import java.util.List;

/**
 * Simple message bus that roughly follows the observer pattern, without the annoyance of implementing a Java observer.
 */
public class Messenger {

    private static Environment environment;
    private static List<IMessageObserver> receivers;

    private static ConsoleMessagePrinter consoleMessagePrinter;

    public static void setEnvironment(Environment environment) {
        Messenger.environment = environment;
    }

    public static void enableMirrorToConsole(boolean value) {
        if (value) {
            if (consoleMessagePrinter == null) {
                consoleMessagePrinter = new ConsoleMessagePrinter();
            }
            addReceiver(consoleMessagePrinter);
        } else {
            removeReceiver(consoleMessagePrinter);
        }
    }

    public static void addReceiver(IMessageObserver receiver) {
        if (receivers == null) {
            receivers = new ArrayList<>();
        }
        if (!receivers.contains(receiver)) {
            receivers.add(receiver);
        }
    }

    public static void removeReceiver(IMessageObserver receiver) {
        if (receivers != null && receiver != null) {
            receivers.remove(receiver);
        }
    }

    public static void broadcast(String status) {
        String time = environment != null ? "" + environment.getTime() : "?";
        String message = String.format("[%s] %s", time, status);
        if (receivers != null) {
            for (IMessageObserver receiver : receivers) {
                receiver.receive(message);
            }
        }
    }

    public static void broadcast(String statusFormat, Object... args) {
        broadcast(String.format(statusFormat, args));
    }

    public static void broadcast(Agent agent, String status) {
        broadcast("%s reports: %s", agent, status);
    }

    public static void broadcast(Agent agent, String statusFormat, Object... args) {
        Object[] newArgs = new Object[args.length + 1];
        newArgs[0] = agent;
        System.arraycopy(args, 0, newArgs, 1, args.length);
        broadcast("%s reports: " + statusFormat, newArgs);
    }

    private static class ConsoleMessagePrinter implements IMessageObserver {
        @Override
        public void receive(String message) {
            System.out.println(message);
        }
    }
}
