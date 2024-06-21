package org.alex.service;

import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;

public class MyFixedThreadPool {

    private volatile boolean isShuttingDown;

    private Queue<Runnable> tasks = new LinkedBlockingQueue<>();

    public MyFixedThreadPool(int threadsCount) {
        for (int i = 0; i < threadsCount; i++) {
            new Thread(new TaskExecutor(tasks), "my-pool-" + (i + 1)).start();
        }
    }

    public void execute(Runnable task) {
        if (isShuttingDown) {
            throw new RejectedExecutionException("Shutting down");
        }
        tasks.add(task);
    }


    public void shutdown() {
        isShuttingDown = true;
    }


    public boolean isShutdown() {
        return isShuttingDown;
    }


    static class TaskExecutor implements Runnable {

        private final Queue<Runnable> tasks;

        public TaskExecutor(Queue<Runnable> tasks) {
            this.tasks = tasks;
        }

        @Override
        public void run() {
            while (true) {
                Runnable task = tasks.poll();
                if (task != null) {
                    task.run();
                }
            }
        }
    }

}
