package nl.tno.idsa.framework.simulator;

import nl.tno.idsa.framework.messaging.Messenger;
import nl.tno.idsa.framework.world.Environment;
import nl.tno.idsa.framework.world.Time;

import java.util.List;

/**
 * Main simulator loop code.
 */
// TODO Prefer if this is not a singleton. We could run multiple simulators, right?
public class Sim {

    private static Sim instance;

    // TODO Somehow the sim clock rate needs to be adaptive so slower systems or bigger environments get run at the right real time factor and not slower.
    private static final double SIM_HERTZ = 5;
    private static final long TIME_BETWEEN_UPDATES = (long) (Time.NANO_SECOND / SIM_HERTZ);

    private Environment env;

    private double maxXRealTime = 10.0;
    private double actualXRealTime;
    private long simTime;
    private boolean done;
    private boolean requestPause;
    private boolean isPaused;
    private boolean isRunning;

    public static Sim getInstance() {
        if (instance == null) {
            instance = new Sim();
        }
        return instance;
    }

    public void init(Environment env) {
        this.env = env;
        this.done = false;
        this.isRunning = false;
        this.requestPause = false;

        // Sim time is incremental time since environment starting time
        this.simTime = 0;
    }

    public Environment getEnvironment() {
        return env;
    }

    public void start() {
        // Sleeping background thread to increase timer accuracy
        new Thread("Background Sleeper") {
            @Override
            public void run() {
                try {
                    Thread.sleep(Long.MAX_VALUE);
                } catch (Exception e) {
                    // TODO Handle exception?
                }
            }
        }.start();
        // Start update loop
        // NOTE: running this on the main thread
        runFixedFrameRate();
    }

    private void runFixedFrameRate() {
        this.isRunning = true;
        System.out.format("\n[SIM] Events occur in %.1f x real-time%n", maxXRealTime);  // TODO To logger?
        // Calculate real-time period of a frame, based on real-time factor
        long prev = System.nanoTime();
        // Last time update notification in real-time nano's
        long lastNotify = 0;
        long lastSimTime = 0;
        this.actualXRealTime = maxXRealTime;
        double frame = TIME_BETWEEN_UPDATES / this.maxXRealTime;
        while (!done) {
            isPaused = false;
            long now = System.nanoTime();
            // Do X updates as required by our fixed framerate
            while (!requestPause && (now - prev) > frame) {
                step(TIME_BETWEEN_UPDATES);
                simTime += TIME_BETWEEN_UPDATES;
                env.getTime().increment(TIME_BETWEEN_UPDATES);
                prev += frame;
                now = System.nanoTime();
                // Check if we need to notify about the passed time
                if ((now - lastNotify) > Time.NANO_SECOND) {
                    this.actualXRealTime = (simTime - lastSimTime) / (double) (now - lastNotify);
                    lastNotify = now;
                    lastSimTime = simTime;
                    env.notifyTimeUpdated();
                }
            }
            // Wait for next update time
            while ((isPaused = requestPause) || (now = System.nanoTime()) - prev < frame) {
                Thread.yield();
                try {
                    Thread.sleep(1);
                } catch (Exception e) {
                    // TODO Handle exception?
                }
                if (isPaused) {
                    // Do not catch up if isPaused
                    prev = now;
                }
            }
        }
        this.isRunning = false;
    }

    public boolean isPaused() {
        return isPaused;
    }

    public void setPause(boolean requestPause) {
        this.requestPause = requestPause;
    }

    public void togglePause() {
        this.requestPause = !this.requestPause;
    }

    private void step(double elapsed) {

        final double durationInSeconds = elapsed / Time.NANO_SECOND;

// TODO Investigate whether we can get parallel execution to work in the simulator.
//        // Parallel version: crashes :(
//        ParallelExecution<ISimulatedObject> parallelExecution = new ParallelExecution<ISimulatedObject>() {
//            @Override
//            public void runOn(ISimulatedObject simulatedObject) {
//                if (simulatedObject.hasNextStep()) {
//                    simulatedObject.nextStep(durationInSeconds);
//                }
//            }
//        } ;
//        parallelExecution.run(env.getSimulatedObjects());

        // Serial version
        List<ISimulatedObject> simulatedObjects = env.getSimulatedObjects();
        for (int i = 0; i < simulatedObjects.size(); ++i) {
            ISimulatedObject simulatedObject = simulatedObjects.get(i);
            if (simulatedObject.hasNextStep()) {
                simulatedObject.nextStep(durationInSeconds);
            }
        }
    }

    public boolean isRunning() {
        return isRunning;
    }

    public double getMaxXRealTime() {
        return maxXRealTime;
    }

    public void setMaxXRealTime(double maxXRealTime) {
        System.out.println("[SIM] Maximum real time factor set to " + maxXRealTime);  // TODO Output to logger.
        this.maxXRealTime = maxXRealTime;
    }

    public boolean isDone() {
        return done;
    }

    public void setDone(boolean done) {
        this.done = done;
    }

    public static double getHz() {
        return SIM_HERTZ;
    }

    public double getActualXRealTime() {
        return actualXRealTime;
    }
}
