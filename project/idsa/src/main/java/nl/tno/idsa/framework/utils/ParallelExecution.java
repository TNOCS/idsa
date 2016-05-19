package nl.tno.idsa.framework.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

// TODO Document class.

public abstract class ParallelExecution<I> {

    public static void main(String[] args) {
        ParallelExecution<Integer> parallelExecution = new ParallelExecution<Integer>() {
            @Override
            public void runOn(Integer input) {
                System.out.println(input);
            }
        };
        List<Integer> inputs = new ArrayList<>(100);
        for (int i = 0; i < 100; i++) {
            inputs.add(i);
        }
        parallelExecution.run(inputs);
        System.out.println("Finished");  // TODO This one terminates, but if we use it in the agenda planner, it does not, although all inputs are processed.
    }

    public boolean run(List<I> inputs) {
        try {
            ExecutorService taskExecutor = Executors.newCachedThreadPool();
            for (int i = 0; i < inputs.size(); i++) {
                taskExecutor.execute(new ParallelRunnable<>(this, inputs.get(i)));
            }
            taskExecutor.shutdown();
            taskExecutor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
            return true;
        } catch (Exception e) {
            e.printStackTrace(); // TODO More graceful error throwing :)
            for (int i = 0; i < inputs.size(); i++) {
                ParallelRunnable<I> runnable = new ParallelRunnable<>(this, inputs.get(i));
                runnable.run(); // In sequence.
            }
            return false;
        }
    }

    public abstract void runOn(I input);
}

class ParallelRunnable<I> implements Runnable {

    private ParallelExecution<I> parent;
    private final I input;

    public ParallelRunnable(ParallelExecution<I> parent, I input) {
        this.parent = parent;
        this.input = input;
    }

    @Override
    public void run() {
        // System.out.println("call " + input); // TODO Remove.
        parent.runOn(input);
    }
}
